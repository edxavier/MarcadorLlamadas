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
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(notificationId)

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
                Log.e("EDER", "collectCallEvents")
                when (state) {
                    Call.STATE_DISCONNECTED -> {
                        currentStatus = state
                        delay(1000)
                        binding.callStatus.text = "LLamada finalizada"
                        finish()
                    }
                    Call.STATE_ACTIVE -> {
                        binding.callStatus.text = "Activa"
                        binding.containerAnswer.visibility = View.GONE
                        currentStatus = state
                        Log.e("EDER", "Conectado")
                        starTimer()
                    }
                    Call.STATE_CONNECTING -> {
                        binding.callStatus.text = "Conectando"
                        binding.containerAnswer.visibility = View.GONE
                    }
                    Call.STATE_DIALING -> {
                        binding.callStatus.text = "Marcando"
                        binding.containerAnswer.visibility = View.GONE
                    }
                    Call.STATE_RINGING -> {
                        binding.callStatus.text = "LLamada entrante"
                    }
                    Call.STATE_HOLDING -> {
                        binding.callStatus.text = "En espera"
                        currentStatus = state
                    }
                }
            }
        }
    }

    private fun starTimer(){
        launch {
            binding.callStatus.text = seconds.toString()
            while (true){
                delay(1000)
                if (currentStatus == Call.STATE_ACTIVE){
                    seconds += 1
                    binding.callStatus.text = seconds.toString()
                }else{
                    break
                }
            }
        }
    }
}