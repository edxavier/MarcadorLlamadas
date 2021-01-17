package com.edxavier.cerberus_sms

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.edxavier.cerberus_sms.databinding.ActivityCallBinding
import com.edxavier.cerberus_sms.helpers.*
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
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
        adapter = SessionCallsAdapter(this)
        //setContentView(R.layout.activity_call)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationId = intent.getIntExtra("notificationId", 0)
        val autoAnswer = intent.getIntExtra("autoAnswer", 0)
        if(notificationId != 0) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
    }

    private fun setupClickListeners() {
        binding.fabAnswer.setOnClickListener {
            CallStateManager.callList[CallStateManager.getCallIndex(CallStateManager.newCall!!)].answer()
        }
        binding.fabHangup.setOnClickListener {
            //CallStateManager.hangup()
            if(currentStatus == Call.STATE_RINGING) {
                CallStateManager.newCall?.disconnect()
                Log.e("EDER", "COLGAR LLAMADA ENTRANTE EN PANTALLA")
            }else {
                Log.e("EDER", "COLGAR")
                if (CallStateManager.callList.size > 1) {
                    Log.e("EDER", "COLGAR LLAMADA ACTIVA EN MAS DE 1")
                    CallStateManager.callList[CallStateManager.getActiveCallIndex()].call?.getPhoneNumber()?.let { it1 -> Log.e("EDER", it1) }
                    CallStateManager.callList[CallStateManager.getActiveCallIndex()].hangup()
                }else if (CallStateManager.callList.size == 1) {
                    Log.e("EDER", "COLGAR UNICA LLAMADA ACTIVA")
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

    }


    override fun onStart() {
        super.onStart()
        collectCallEvents()
    }

    private fun collectCallEvents(){
        launch {
            CallStateManager.callState.filter { it.call!=null }.collect { callState ->
                Log.e("EDER_STATE", "----------collect--------------------")
                val state = callState.state
                Log.e("EDER_STATE", state.stateToString())
                callState.call?.let {
                    val index = CallStateManager.getCallIndex(callState.call)
                    if(CallStateManager.callList.size>1)
                        binding.iconHold.setImageDrawable(ContextCompat.getDrawable(this@CallActivity, R.drawable.ic_baseline_swap_calls))
                    else
                        binding.iconHold.setImageDrawable(ContextCompat.getDrawable(this@CallActivity, R.drawable.ic_baseline_pause))
                    Log.e("EDER_STATE", it.getPhoneNumber())
                    when (state) {
                        Call.STATE_DISCONNECTED -> {
                            delay(500)
                            //Comparar con 0 ya que antes de llegar aqui la llamada es removida
                            if(CallStateManager.callList.size==0)
                                finishAndRemoveTask()
                            else{
                                binding.containerAnswer.visibility = View.GONE
                                binding.containerMic.visibility = View.VISIBLE
                                binding.containerHold.visibility = View.VISIBLE
                                binding.containerSpeaker.visibility = View.VISIBLE
                                binding.containerDialpad.visibility = View.VISIBLE
                            }
                        }
                        Call.STATE_ACTIVE -> {
                            binding.containerAnswer.visibility = View.GONE
                            binding.containerMic.visibility = View.VISIBLE
                            binding.containerHold.visibility = View.VISIBLE
                            binding.containerSpeaker.visibility = View.VISIBLE
                            binding.containerDialpad.visibility = View.VISIBLE
                            if(CallStateManager.callList[index].seconds == 0 && !CallStateManager.callList[index].timerStarted) {
                                //Log.e("EDER", "Start obj timer ${CallStateManager.callList[index].call?.getPhoneNumber()} " + "${CallStateManager.callList[index].timerStarted}")
                                CallStateManager.callList[index].startTimer()
                            }
                        }
                        Call.STATE_CONNECTING -> {
                            binding.containerAnswer.visibility = View.GONE
                            binding.containerMic.visibility = View.GONE
                            binding.containerHold.visibility = View.GONE
                            binding.containerSpeaker.visibility = View.VISIBLE
                            binding.containerDialpad.visibility = View.VISIBLE
                        }
                        Call.STATE_DIALING -> {
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

    override fun onDestroy() {
        super.onDestroy()
        CallStateManager.callActivityShown = false
    }
}