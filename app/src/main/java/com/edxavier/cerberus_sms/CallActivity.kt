package com.edxavier.cerberus_sms

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.Call
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.BlurTransformation
import coil.transform.RoundedCornersTransformation
import com.edxavier.cerberus_sms.adapters.SessionCallsAdapter
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.ActivityCallBinding
import com.edxavier.cerberus_sms.databinding.AdNativeInCallBinding
import com.edxavier.cerberus_sms.databinding.AdNativeLayoutBinding
import com.edxavier.cerberus_sms.helpers.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class CallActivity : ScopeActivity(), SensorEventListener {

    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var powerManager: PowerManager
    private var field: Int = 0x00000020
    private lateinit var binding: ActivityCallBinding
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null

    lateinit var adapter: SessionCallsAdapter

    companion object {
        private var currentStatus = 0
        private var muteMic = false
        private var speakerOn = false
        private var holdCall = false
        private var showDial = false

        fun start(context: Context, call: Call) {

            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CallStateManager.callActivityShown = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "com.edxavier.cerberus_sms")

        adapter = SessionCallsAdapter(this, this)
        //setContentView(R.layout.activity_call)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadNativeAd()
        val notificationId = intent.getIntExtra("notificationId", 0)
        val autoAnswer = intent.getIntExtra("autoAnswer", 0)
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(CallStateManager.activeCallNotificationId)
        if(notificationId != 0) {
            mgr.cancel(notificationId)
        }
        if(autoAnswer == 1) {
            CallStateManager.newCall?.let { newCall ->
                val callIndex = CallStateManager.getCallIndex(newCall)
                if(callIndex>=0)
                    CallStateManager.callList[callIndex].answer()
            }
            //CallStateManager.answer()
        }
        setupClickListeners()
        collectCallEvents()

        binding.recyclerSessionCalls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerSessionCalls.adapter = adapter
        binding.recyclerSessionCalls.setHasFixedSize(true)
        launch {
            CallStateManager.updateUi.collect {
                //Log.e("EDER", "UPDATE UI $it")
                if (it>0) {
                    adapter.submitList(CallStateManager.callList)
                    binding.recyclerSessionCalls.adapter = adapter
                }
            }
        }
        /*binding.backgroundImage.load(R.drawable.lift){
            placeholder(R.drawable.lift)
            transformations(BlurTransformation(this@CallActivity))
        }
         */
        //setupBanner()
    }

    private fun setupClickListeners() {

        if(speakerOn) {
            binding.iconSpeaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_speaker))
            ImageViewCompat.setImageTintList(binding.iconSpeaker,
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
        }

        if(muteMic) {
            binding.iconMic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_mic_off))
            ImageViewCompat.setImageTintList(binding.iconMic,
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
        }
        if(holdCall) {
            if(CallStateManager.callList.size==1)
                ImageViewCompat.setImageTintList(binding.iconHold, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
        }

        binding.fabAnswer.setOnClickListener {
            when (CallStateManager.callList.size) {
                2 -> {
                    //Log.e("EDER", "RESPONDER 2da LLAMADA ${CallStateManager.callList[1].call?.getPhoneNumber()}")
                    //Log.e("EDER", "RETENER 1era LLAMADA ${CallStateManager.callList[0].call?.getPhoneNumber()}")
                    CallStateManager.callList[1].answer()
                    CallStateManager.callList[0].hold()
                }
                1 -> {
                    //Log.e("EDER", "RESPONDER UNICA LLAMADA ")
                    CallStateManager.callList[0].answer()
                }
                else -> CallStateManager.callList[CallStateManager.getCallIndex(CallStateManager.newCall!!)].answer()
            }

        }
        binding.fabHangup.setOnClickListener {
            //CallStateManager.hangup()
            if(currentStatus == Call.STATE_RINGING) {
                if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    CallStateManager.newCall?.reject(Call.REJECT_REASON_DECLINED)
                }else {
                    CallStateManager.newCall?.disconnect()
                }

            }else {
                when {
                    CallStateManager.callList.size > 1 -> {
                        //Log.e("EDER", "COLGAR LLAMADA ACTIVA EN MAS DE 1")
                        CallStateManager.callList[CallStateManager.getActiveCallIndex()].call?.getPhoneNumber()?.let { it1 -> Log.e("EDER", it1) }
                        CallStateManager.callList[CallStateManager.getActiveCallIndex()].hangup()
                    }
                    CallStateManager.callList.size == 1 -> {
                        CallStateManager.callList[0].hangup()
                    }
                    CallStateManager.callList.size == 0 -> {
                        finishAndRemoveTask()
                    }
                }
            }
        }

        binding.cardMic.setOnClickListener {
            //CallStateManager.newCall?.playDtmfTone('1')
            //CallStateManager.newCall?.stopDtmfTone()
            muteMic = !muteMic
            CallStateManager.muteMicrophone(muteMic)
            if(muteMic) {
                binding.iconMic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_mic_off))
                ImageViewCompat.setImageTintList(binding.iconMic,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
            }else {
                binding.iconMic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_mic_off))
                ImageViewCompat.setImageTintList(binding.iconMic,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_white_1000_60)))
            }
        }
        binding.cardHold.setOnClickListener {
            holdCall = !holdCall
            if(holdCall) {
                if(CallStateManager.callList.size==1)
                    ImageViewCompat.setImageTintList(binding.iconHold, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
                CallStateManager.hold()
            }else {
                ImageViewCompat.setImageTintList(binding.iconHold,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_white_1000_60)))
                if (CallStateManager.callList.size>1)
                    CallStateManager.hold()
                else
                    CallStateManager.unHold()
            }
        }

        binding.cardSpeaker.setOnClickListener {
            speakerOn = !speakerOn
            if(speakerOn) {
                CallStateManager.setSpeakerOn()
                binding.iconSpeaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_speaker))
                ImageViewCompat.setImageTintList(binding.iconSpeaker,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
            }else{
                CallStateManager.setSpeakerOff()
                binding.iconSpeaker.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_speaker))
                ImageViewCompat.setImageTintList(binding.iconSpeaker,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_white_1000_60)))
            }

            /*val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_CALL
            speakerOn = !speakerOn
            Log.e("EDER_SPEAK", audioManager.isSpeakerphoneOn.toString())
            audioManager.isSpeakerphoneOn = true
            
            Log.e("EDER_SPEAK", audioManager.isSpeakerphoneOn.toString())
            if(speakerOn)
                ImageViewCompat.setImageTintList(binding.iconSpeaker, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
            else
                ImageViewCompat.setImageTintList(binding.iconSpeaker, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_white_1000_60)))
             */
        }

        with(binding){
            dialPad.setOnClickListener {
                showDial = !showDial
                if (showDial)
                   showDialPad()
                else
                    hideDialPad()
            }
            dial0.setOnClickListener { playDTMFTone('0') }
            dial1.setOnClickListener { playDTMFTone('1') }
            dial2.setOnClickListener { playDTMFTone('2') }
            dial3.setOnClickListener { playDTMFTone('3') }
            dial4.setOnClickListener { playDTMFTone('4') }
            dial5.setOnClickListener { playDTMFTone('5') }
            dial6.setOnClickListener { playDTMFTone('6') }
            dial7.setOnClickListener { playDTMFTone('7') }
            dial8.setOnClickListener { playDTMFTone('8') }
            dial9.setOnClickListener { playDTMFTone('9') }
            dialAsterisk.setOnClickListener { playDTMFTone('*') }
            dialHash.setOnClickListener { playDTMFTone('#') }
        }

    }

    private fun playDTMFTone(digit: Char){
        val i = CallStateManager.getActiveCallIndex()
        if(i>=0) {
            CallStateManager.callList[i].call?.playDtmfTone(digit)
            CallStateManager.callList[i].call?.stopDtmfTone()
        }
    }

    //Tono de aviso de llamada entrante mientras existe ya una llamada en curso
    private fun playIncomingCallDTMFTone(){
        launch {
            val dtmfGenerator = ToneGenerator(0, ToneGenerator.MAX_VOLUME)
            while(currentStatus == Call.STATE_RINGING) {
                dtmfGenerator.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 1000) // all types of tones are available...
                delay(1000)
                dtmfGenerator.stopTone()
                delay(2500)
            }
        }
    }

    private fun collectCallEvents(){
        launch {
            CallStateManager.callState.filter { it.call!=null }.collect { callState ->
                //Log.e("EDER_STATE", "----------collect--------------------")
                val state = callState.state
                //Log.e("EDER_STATE", state.stateToString())
                callState.call?.let {
                    val index = CallStateManager.getCallIndex(callState.call)
                    val repoC = RepoContact.getInstance(this@CallActivity)
                    if(CallStateManager.callList.size>1)
                        binding.iconHold.setImageDrawable(ContextCompat.getDrawable(this@CallActivity, R.drawable.ic_baseline_swap_calls))
                    else {
                        binding.iconHold.setImageDrawable(ContextCompat.getDrawable(this@CallActivity, R.drawable.ic_baseline_pause))
                        val contact = repoC.getPhoneContact(it.getPhoneNumber())
                        if (contact.photo.isNotBlank())
                            binding.backgroundImage.load(Uri.parse(contact.photo)){
                                placeholder(R.drawable.diente_leon)
                                transformations(BlurTransformation(this@CallActivity))
                            }
                    }
                    //Log.e("EDER_STATE", it.getPhoneNumber())
                    when (state) {
                        Call.STATE_DISCONNECTED -> {
                            currentStatus = state
                            //Comparar con 0 ya que antes de llegar aqui la llamada es removida
                            if (CallStateManager.callList.size == 1) {
                                if (CallStateManager.callList[0].call?.state == Call.STATE_DISCONNECTED) {
                                    delay(1000)
                                    CallStateManager.callList.removeAt(0)
                                    speakerOn = false
                                    muteMic = false
                                    finishAndRemoveTask()
                                } else {
                                    Toast.makeText(this@CallActivity, "Llamada continua mostrar botones de llamada", Toast.LENGTH_LONG).show()
                                    showActiveCallButtons()
                                }
                            } else if (CallStateManager.callList.size > 1) {
                                showActiveCallButtons()
                            } else {
                                speakerOn = false
                                muteMic = false
                                finishAndRemoveTask()
                            }
                        }
                        Call.STATE_ACTIVE -> {
                            currentStatus = state
                            showActiveCallButtons()
                            if (CallStateManager.callList[index].seconds == 0 && !CallStateManager.callList[index].timerStarted) {
                                //Log.e("EDER", "Start obj timer ${CallStateManager.callList[index].call?.getPhoneNumber()} " + "${CallStateManager.callList[index].timerStarted}")
                                CallStateManager.callList[index].startTimer()
                            }
                        }
                        Call.STATE_CONNECTING -> {
                            currentStatus = state
                            binding.containerAnswer.visibility = View.GONE
                            binding.containerMic.visibility = View.GONE
                            binding.containerHold.visibility = View.GONE
                            binding.containerSpeaker.visibility = View.VISIBLE
                            binding.containerDialpad.visibility = View.VISIBLE
                        }
                        Call.STATE_DIALING -> {
                            currentStatus = state
                            binding.containerAnswer.visibility = View.GONE
                            binding.containerMic.visibility = View.GONE
                            binding.containerHold.visibility = View.GONE
                            binding.containerSpeaker.visibility = View.VISIBLE
                            binding.containerDialpad.visibility = View.VISIBLE
                        }
                        Call.STATE_RINGING -> {
                            showIncomingCallButtons()
                            currentStatus = state
                            if(CallStateManager.callList.size>=1)
                                playIncomingCallDTMFTone()
                        }
                        Call.STATE_HOLDING -> {
                            currentStatus = state
                        }
                    }
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        CallStateManager.callActivityShown = false
        if(CallStateManager.callList.size>0) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.cancel(CallStateManager.activeCallNotificationId)
            CallNotificationHelper.showInCallNotification(this)
        }
    }

    private fun showActiveCallButtons(){
        binding.containerAnswer.visibility = View.GONE
        binding.containerMic.visibility = View.VISIBLE
        binding.containerHold.visibility = View.VISIBLE
        binding.containerSpeaker.visibility = View.VISIBLE
        binding.containerDialpad.visibility = View.VISIBLE
    }

    private fun showIncomingCallButtons(){
        binding.containerAnswer.visibility = View.VISIBLE
        binding.containerMic.visibility = View.GONE
        binding.containerHold.visibility = View.GONE
        binding.containerSpeaker.visibility = View.GONE
        binding.containerDialpad.visibility = View.GONE
        hideDialPad()
    }

    private fun showDialPad(){
        val animSlideUp: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val animSlideDown: Animation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        with(binding){
            row1.startAnimation(animSlideUp)
            row2.startAnimation(animSlideUp)
            row3.startAnimation(animSlideUp)
            row4.startAnimation(animSlideUp)
            //row5.startAnimation(animSlideDown)
            row1.visible()
            row2.visible()
            row3.visible()
            row4.visible()
            row5.invisible()
        }
    }
    private fun hideDialPad(){

        binding.row1.invisible()
        binding.row2.invisible()
        binding.row3.invisible()
        binding.row4.invisible()
        binding.row5.visible()
    }
    private fun setupBanner() {
        val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(arrayOf(
                        "AC5F34885B0FE7EF03A409EB12A0F949",
                        AdRequest.DEVICE_ID_EMULATOR
                ).toList())
                .build()
        MobileAds.setRequestConfiguration(requestConfig)

        val adRequest = AdRequest.Builder()
                .build()

        val adView =  AdView(this)
        binding.adViewContainer.addView(adView)

        adView.adSize = getAdSize()
        adView.adUnitId = getString(R.string.BANNER_FLOTANTE)

        adView.loadAd(adRequest)
        //nav_view.menu.findItem(R.id.destino_ocultar_publicidad).isVisible = false
    }

    private fun getAdSize(): AdSize {
        //Determine the screen width to use for the ad width.
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density

        //you can also pass your selected width here in dp
        val adWidth = (widthPixels / density).toInt()

        //return the optimal size depends on your orientation (landscape or portrait)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }


    override fun onResume() {
        super.onResume()
        mSensorManager?.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager?.unregisterListener(this)
    }

    @SuppressLint("WakelockTimeout")
    override fun onSensorChanged(p0: SensorEvent?) {
        p0?.let {
            if (it.values.isNotEmpty()){
                if(it.values[0]<5){
                    //Apagar pantalla
                    wakeLock?.apply {
                        if(!isHeld)
                            acquire()
                    }
                }else{
                    //Encender pantalla
                    wakeLock?.apply {
                        if(isHeld)
                            release()
                    }
                }
            }
        }

    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    @SuppressLint("InflateParams")
    private fun loadNativeAd(){
        val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(arrayOf(
                        "AC5F34885B0FE7EF03A409EB12A0F949",
                        AdRequest.DEVICE_ID_EMULATOR
                ).toList())
                .build()
        MobileAds.setRequestConfiguration(requestConfig)
        val nativeCode = "ca-app-pub-9964109306515647/3495890674"
        val builder = AdLoader.Builder(this, nativeCode)

        builder.forNativeAd { nativeAd ->
            val adBinding = AdNativeInCallBinding.inflate(layoutInflater)
            //val nativeAdview = AdNativeLayoutBinding.inflate(layoutInflater).root
            binding.nativeAdFrameLayout.removeAllViews()
            binding.nativeAdFrameLayout.addView(populateNativeAd(nativeAd, adBinding))
        }

        val adLoader = builder.withAdListener(object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError?) {
                super.onAdFailedToLoad(p0)
                Log.e("EDER", "${p0?.message}: ${p0?.cause.toString()}")
            }
        }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAd(nativeAd: NativeAd, adView: AdNativeInCallBinding): NativeAdView {
        val nativeAdView = adView.root
        with(adView){
            adHeadline.text = nativeAd.headline
            nativeAdView.headlineView = adHeadline
            nativeAd.advertiser?.let {
                adAdvertiser.text = it
                nativeAdView.advertiserView = adAdvertiser
            }
            nativeAd.icon?.let {
                //adIcon.setImageDrawable(it.drawable)
                //adIcon.load(it.drawable){transformations(RoundedCornersTransformation(radius = 8f))}
                adIcon.visible()
                nativeAdView.iconView = adIcon
                (nativeAdView.iconView as ImageView).load(it.drawable){transformations(RoundedCornersTransformation(radius = 8f))}
            }
            nativeAd.starRating?.let {
                adStartRating.rating = it.toFloat()
                adStartRating.visible()
                nativeAdView.starRatingView = adStartRating
            }
            nativeAd.callToAction?.let {
                adBtnCallToAction.text = it
                nativeAdView.callToActionView = adBtnCallToAction
            }
            nativeAd.body?.let {
                adBodyText.text = it
                nativeAdView.bodyView = adBodyText
            }
        }
        nativeAdView.setNativeAd(nativeAd)
        return nativeAdView
    }


}