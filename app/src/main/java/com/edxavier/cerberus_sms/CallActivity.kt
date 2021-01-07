package com.edxavier.cerberus_sms

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.edxavier.cerberus_sms.databinding.ActivityCallBinding
import com.edxavier.cerberus_sms.helpers.CallStateManager
import com.edxavier.cerberus_sms.helpers.stateToString
import com.edxavier.cerberus_sms.helpers.timeFormat
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@ExperimentalCoroutinesApi
class CallActivity : ScopeActivity() {

    private lateinit var binding: ActivityCallBinding
    private lateinit var number: String

    private var seconds = 0
    private var currentStatus = 0
    private var muteMic = false
    private var speakerOn = false
    private var holdCall = false


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

        //setContentView(R.layout.activity_call)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationId = intent.getIntExtra("notificationId", 0)
        val autoAnswer = intent.getIntExtra("autoAnswer", 0)
        if(notificationId != 0) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.cancel(notificationId)
        }
        if(autoAnswer == 1)
            CallStateManager.answer()
        setupClickListeners()
        CallStateManager.call?.let {
            binding.displayContact.text = it.details.handle.schemeSpecificPart
        }
    }

    private fun setupClickListeners() {
        binding.fabAnswer.setOnClickListener {
            CallStateManager.answer()
        }
        binding.fabHangup.setOnClickListener {
            CallStateManager.hangup()
        }

        binding.cardMic.setOnClickListener {
            CallStateManager.call?.playDtmfTone('1')
            CallStateManager.call?.stopDtmfTone()
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
                ImageViewCompat.setImageTintList(binding.iconHold,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_blue_500)))
                CallStateManager.hold()
            }else {
                ImageViewCompat.setImageTintList(binding.iconHold,
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.md_white_1000_60)))
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
            CallStateManager.callState.collect { state ->
                binding.callStatus.text = state.stateToString()

                when (state) {
                    Call.STATE_DISCONNECTED -> {
                        currentStatus = state
                        delay(1000)
                        finishAndRemoveTask()
                    }
                    Call.STATE_ACTIVE -> {
                        binding.containerAnswer.visibility = View.GONE
                        binding.containerMic.visibility = View.VISIBLE
                        binding.containerHold.visibility = View.VISIBLE
                        binding.containerSpeaker.visibility = View.VISIBLE
                        binding.containerDialpad.visibility = View.VISIBLE
                        currentStatus = state
                        if(seconds == 0)
                            starTimer()
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
                    }
                    Call.STATE_HOLDING -> {
                        currentStatus = state
                    }
                }
            }
        }
    }

    private fun starTimer(){
        launch {
            binding.callStatus.text = seconds.timeFormat()
            while (true){
                delay(1000)
                if (currentStatus == Call.STATE_ACTIVE || currentStatus == Call.STATE_HOLDING){
                    seconds += 1
                    if( currentStatus == Call.STATE_ACTIVE )
                        binding.callStatus.text = seconds.timeFormat()
                }else{
                    break
                }
            }
            Log.e("EDER", "FIN LAUNCH")
        }
    }
}