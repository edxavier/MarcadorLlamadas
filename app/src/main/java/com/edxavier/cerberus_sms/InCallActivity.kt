package com.edxavier.cerberus_sms

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.Call
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.helpers.AnalyticsLogger
import com.edxavier.cerberus_sms.helpers.CallNotificationHelper
import com.edxavier.cerberus_sms.helpers.FlowEventBus
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import com.edxavier.cerberus_sms.ui.screens.incall.CallMainButtons
import com.edxavier.cerberus_sms.ui.screens.incall.InCallViewModel
import com.edxavier.cerberus_sms.ui.screens.incall.OnGoingCalls
import com.edxavier.cerberus_sms.ui.ui.theme.InCallTheme
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InCallActivity : ComponentActivity() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null
    private lateinit var powerManager: PowerManager
    lateinit var viewModel: InCallViewModel

    private var proximityListener: SensorEventListener? = null
    private var flowSubscription: Job? = null

    companion object {
        fun start(context: Context, call: Call) {
            Intent(context, InCallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call.details.handle)
                .let(context::startActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSensors()
        viewModel = InCallViewModel(RepoContact(this))

        MyCallsManager.inCallUiShown = true
        MyCallsManager.adSize = getAdSize()
        setupScreenBehaviour()

        handleNotification()
        flowSubscription = lifecycleScope.launch {
            FlowEventBus.publish(MyCallsManager.getCalls())
            FlowEventBus.subscribe<MutableList<Call>> { inCalls ->
                if (MyCallsManager.thereIsIncomingCall() && MyCallsManager.getCalls().size > 1) {
                    playIncomingCallDTMFTone()
                }
                if (inCalls.isEmpty()) {
                    MyCallsManager.getCalls().lastOrNull()?.let { lastCall ->
                        val duration = if (lastCall.details.connectTimeMillis > 0)
                            ((System.currentTimeMillis() - lastCall.details.connectTimeMillis) / 1000).toInt()
                        else 0
                        AnalyticsLogger.callEnded(duration)
                    }
                    MyCallsManager.inCallUiShown = false
                    MyCallsManager.speakerOn = false
                    MyCallsManager.micOff = false
                    MyCallsManager.paused = false
                    finishAndRemoveTask()
                }
            }
        }
        setContent {
            val largeRadialGradient = object : ShaderBrush() {
                override fun createShader(size: Size): Shader {
                    val biggerDimension = maxOf(size.height, size.width)
                    return RadialGradientShader(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                        center = size.center,
                        radius = biggerDimension / 2,
                        colorStops = listOf(0f, 0.95f)
                    )
                }
            }
            InCallTheme {
                Surface(
                    modifier = Modifier
                        .background(largeRadialGradient)
                        .fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OnGoingCalls(viewModel)
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CallMainButtons(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        proximityListener?.let { mSensorManager?.unregisterListener(it) }

        proximityListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] < 5) {
                        wakeLock?.apply { if (!isHeld) acquire(600000) }
                    } else {
                        wakeLock?.apply { if (isHeld) release() }
                    }
                }
            }
        }
        mSensorManager?.registerListener(
            proximityListener, mProximity, SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onStop() {
        super.onStop()
        proximityListener?.let { mSensorManager?.unregisterListener(it) }
        proximityListener = null

        wakeLock?.apply { if (isHeld) release() }

        MyCallsManager.inCallUiShown = false
        if (MyCallsManager.getCalls().isNotEmpty()) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.cancel(MyCallsManager.inCallNotificationId)
            CallNotificationHelper.showInCallNotification(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        flowSubscription?.cancel()
    }

    private fun setupScreenBehaviour() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun initSensors() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "com.edxavier.cerberus_sms:wakelog"
        )
    }

    private fun handleNotification() {
        val callNotificationId = intent.getIntExtra("callNotificationId", 0)
        val autoAnswer = intent.getIntExtra("autoAnswer", 0)
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (MyCallsManager.inCallNotificationId != -1) {
            mgr.cancel(callNotificationId)
            mgr.cancel(5)
        }
        if (autoAnswer == 1) {
            MyCallsManager.answerRingingCall()
        }
    }

    private fun playIncomingCallDTMFTone() {
        lifecycleScope.launch {
            val dtmfGenerator = ToneGenerator(0, ToneGenerator.MAX_VOLUME)
            while (MyCallsManager.thereIsIncomingCall() && MyCallsManager.getCalls().size > 1) {
                dtmfGenerator.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 2000)
                delay(400)
                dtmfGenerator.stopTone()
                delay(500)
                dtmfGenerator.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 1000)
                delay(400)
                dtmfGenerator.stopTone()
                delay(2000)
            }
        }
    }

    private fun getAdSize(): AdSize {
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }
}
