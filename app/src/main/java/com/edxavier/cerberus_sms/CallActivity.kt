package com.edxavier.cerberus_sms

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.BlurTransformation
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.databinding.ActivityCallBinding
import com.edxavier.cerberus_sms.helpers.*
import com.google.android.gms.ads.*
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class CallActivity : ScopeActivity() {

    private lateinit var binding: ActivityCallBinding

    private var seconds = 0
    private var currentStatus = 0
    private var muteMic = false
    private var speakerOn = false
    private var holdCall = false
    private var showDial = false

    lateinit var adapter: SessionCallsAdapter

    companion object {
        fun start(context: Context, call: Call) {


            Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }

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
        adapter = SessionCallsAdapter(this, this)
        //setContentView(R.layout.activity_call)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        setupBanner()
    }

    private fun setupClickListeners() {
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
                if (CallStateManager.callList.size > 1) {
                    //Log.e("EDER", "COLGAR LLAMADA ACTIVA EN MAS DE 1")
                    CallStateManager.callList[CallStateManager.getActiveCallIndex()].call?.getPhoneNumber()?.let { it1 -> Log.e("EDER", it1) }
                    CallStateManager.callList[CallStateManager.getActiveCallIndex()].hangup()
                }else if (CallStateManager.callList.size == 1) {
                    CallStateManager.callList[0].hangup()
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
                if (showDial){
                    row1.visible()
                    row2.visible()
                    row3.visible()
                    row4.visible()
                    row5.invisible()
                }else{
                    row1.invisible()
                    row2.invisible()
                    row3.invisible()
                    row4.invisible()
                    row5.visible()
                }
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

    private fun playDTMFTone(digit:Char){
        val i = CallStateManager.getActiveCallIndex()
        if(i>=0) {
            CallStateManager.callList[i].call?.playDtmfTone(digit)
            CallStateManager.callList[i].call?.stopDtmfTone()
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
                            if(CallStateManager.callList.size==1) {
                                if(CallStateManager.callList[0].call?.state == Call.STATE_DISCONNECTED) {
                                    delay(1000)
                                    CallStateManager.callList.removeAt(0)
                                    finishAndRemoveTask()
                                }else{
                                    showActiveCallButtons()
                                }
                            }else if(CallStateManager.callList.size>1){
                                showActiveCallButtons()
                            }else{
                                finishAndRemoveTask()
                            }

                        }
                        Call.STATE_ACTIVE -> {
                            currentStatus = state
                            showActiveCallButtons()
                            if(CallStateManager.callList[index].seconds == 0 && !CallStateManager.callList[index].timerStarted) {
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
                            binding.containerAnswer.visibility = View.VISIBLE
                            binding.containerMic.visibility = View.GONE
                            binding.containerHold.visibility = View.GONE
                            binding.containerSpeaker.visibility = View.GONE
                            binding.containerDialpad.visibility = View.GONE
                            currentStatus = state
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

}