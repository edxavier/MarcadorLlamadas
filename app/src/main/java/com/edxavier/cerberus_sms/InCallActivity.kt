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
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.helpers.CallNotificationHelper
import com.edxavier.cerberus_sms.helpers.CallStateManager
import com.edxavier.cerberus_sms.helpers.FlowEventBus
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import com.edxavier.cerberus_sms.ui.calls.AppViewModel
import com.edxavier.cerberus_sms.ui.screens.incall.CallMainButtons
import com.edxavier.cerberus_sms.ui.screens.incall.InCallViewModel
import com.edxavier.cerberus_sms.ui.screens.incall.OnGoingCalls
import com.edxavier.cerberus_sms.ui.ui.theme.AppTheme
import com.edxavier.cerberus_sms.ui.ui.theme.InCallTheme
import com.edxavier.cerberus_sms.ui.ui.theme.red_700
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InCallActivity : ComponentActivity(){
    private var wakeLock: PowerManager.WakeLock? = null
    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null
    private lateinit var powerManager: PowerManager
    lateinit var viewModel: InCallViewModel

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
        lifecycleScope.launch {
            FlowEventBus.publish(MyCallsManager.getCalls())
            FlowEventBus.subscribe<MutableList<Call>> { inCalls ->
                // If no calls finish activity
                if(MyCallsManager.thereIsIncomingCall() && MyCallsManager.getCalls().size>1){
                    playIncomingCallDTMFTone()
                }
                if(inCalls.isEmpty()){
                    MyCallsManager.inCallUiShown = false
                    MyCallsManager.speakerOn = false
                    MyCallsManager.micOff = false
                    MyCallsManager.paused = false
                    finishAndRemoveTask()
                }
            }
        }
        setContent {
            val proximitySensorEventListener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

                // on below line we are creating a sensor on sensor changed
                override fun onSensorChanged(event: SensorEvent) {
                    // check if the sensor type is proximity sensor.
                    if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                        if(event.values[0]<5){
                            // Turn off Screen
                            wakeLock?.apply {
                                if(!isHeld) {
                                    acquire(600000)
                                }
                            }
                        }else{
                            //Turn on Screen
                            wakeLock?.apply {
                                if(isHeld)
                                    release()
                            }
                        }
                    }
                }
            }

            // on below line we are registering listener for our sensor manager.
            mSensorManager?.registerListener(
                // on below line we are passing
                // proximity sensor event listener
                proximitySensorEventListener,

                // on below line we are
                // setting proximity sensor.
                mProximity,
                // on below line we are specifying
                // sensor manager as delay normal
                SensorManager.SENSOR_DELAY_NORMAL
            )

            val largeRadialGradient = object : ShaderBrush() {
                override fun createShader(size: Size): Shader {
                    val biggerDimension = maxOf(size.height, size.width)
                    return RadialGradientShader(
                        colors = listOf( Color(0xFF0488EC), Color(0xFF045DE9)),
                        center = size.center,
                        radius = biggerDimension/2,
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        ) {
                           OnGoingCalls(viewModel)
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CallMainButtons(viewModel = viewModel)
                        }
                    }

                }
            }
        }
    }

    private fun setupScreenBehaviour(){
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
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }
    private fun initSensors(){
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "com.edxavier.cerberus_sms:wakelog")
    }

    private fun handleNotification(){
        val callNotificationId = intent.getIntExtra("callNotificationId", 0)
        val autoAnswer = intent.getIntExtra("autoAnswer", 0)
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mgr.cancel(CallStateManager.activeCallNotificationId)
        if(MyCallsManager.inCallNotificationId != -1) {
            mgr.cancel(callNotificationId)
            mgr.cancel(5)
        }
        if(autoAnswer == 1) {
            MyCallsManager.answerRingingCall()
        }
    }

    override fun onStop() {
        super.onStop()
        MyCallsManager.inCallUiShown = false
        if(MyCallsManager.getCalls().isNotEmpty()) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.cancel(MyCallsManager.inCallNotificationId)
            CallNotificationHelper.showInCallNotification(this)
        }
    }

    private fun playIncomingCallDTMFTone(){
        lifecycleScope.launch {
            val dtmfGenerator = ToneGenerator(0, ToneGenerator.MAX_VOLUME)
            while(MyCallsManager.thereIsIncomingCall() && MyCallsManager.getCalls().size>1) {
                dtmfGenerator.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 2000) // all types of tones are available...
                delay(400)
                dtmfGenerator.stopTone()
                delay(500)
                dtmfGenerator.startTone(ToneGenerator.TONE_SUP_CALL_WAITING, 1000) // all types of tones are available...
                delay(400)
                dtmfGenerator.stopTone()
                delay(2000)
            }

        }
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
