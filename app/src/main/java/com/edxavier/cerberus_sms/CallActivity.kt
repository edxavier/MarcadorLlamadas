package com.edxavier.cerberus_sms

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.edxavier.cerberus_sms.databinding.ActivityCallBinding
import com.edxavier.cerberus_sms.helpers.CallStateManager
import com.edxavier.cerberus_sms.helpers.stateToString
import com.edxavier.cerberus_sms.helpers.timeFormat
import com.nicrosoft.consumoelectrico.ScopeActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*


@ExperimentalCoroutinesApi
class CallActivity : ScopeActivity() {

    private lateinit var binding: ActivityCallBinding
    private lateinit var number: String

    private var seconds = 0
    private var currentStatus = 0

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
        Log.e("EDER", autoAnswer.toString())
        if(notificationId != 0) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.cancel(notificationId)
        }
        if(autoAnswer == 1)
            CallStateManager.answer()

        binding.fabAnswer.setOnClickListener {
            CallStateManager.answer()
        }
        binding.fabHangup.setOnClickListener {
            CallStateManager.hangup()
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
                        finish()
                    }
                    Call.STATE_ACTIVE -> {
                        binding.containerAnswer.visibility = View.GONE
                        currentStatus = state
                        starTimer()
                    }
                    Call.STATE_CONNECTING -> {
                        binding.containerAnswer.visibility = View.GONE
                    }
                    Call.STATE_DIALING -> {
                        binding.containerAnswer.visibility = View.GONE
                    }
                    Call.STATE_RINGING -> {
                        //binding.callStatus.text = "LLamada entrante"
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
                if (currentStatus == Call.STATE_ACTIVE){
                    seconds += 1
                    binding.callStatus.text = seconds.timeFormat()
                }else{
                    break
                }
            }
        }
    }
}