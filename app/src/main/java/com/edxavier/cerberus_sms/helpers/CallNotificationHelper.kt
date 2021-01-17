package com.edxavier.cerberus_sms.helpers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.edxavier.cerberus_sms.CallActivity
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.services.CancelCallReceiver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
object CallNotificationHelper: CoroutineScope {
    const val CHANNEL_ID = "com.edxavier.cerberus_sms"
    const val CHANNEL_NAME = "com.edxavier.cerberus_sms"

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    @SuppressLint("WrongConstant")
    fun sendNotification(ctx: Context){
        job = Job()
        val notificationId = Calendar.getInstance().timeInMillis.toInt()

        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)
            mgr.createNotificationChannel(channel)
        }

        // Create an intent which triggers your fullscreen incoming call user interface.
        val intent = Intent(Intent.ACTION_MAIN, null)
        val fullIntent = Intent(Intent.ACTION_MAIN, null)

        intent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        fullIntent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP

        intent.setClass(ctx, CallActivity::class.java)
        fullIntent.setClass(ctx, CallActivity::class.java)

        intent.putExtra("notificationId", notificationId)
        intent.putExtra("autoAnswer", 1)
        val pendingIntent = PendingIntent.getActivity(ctx, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val pendingFullScreenIntent = PendingIntent.getActivity(ctx, 2, fullIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //Create an Intent for the BroadcastReceiver
        val cancelIntent = Intent(ctx, CancelCallReceiver::class.java)
        cancelIntent.putExtra("notificationId", notificationId)
        //Create the PendingIntent
        val cancelPendingIntent = PendingIntent.getBroadcast(ctx, 0, cancelIntent,  PendingIntent.FLAG_UPDATE_CURRENT)


        // Build the notification as an ongoing high priority item; this ensures it will show as
        // a heads up notification which slides down over top of the current content.
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setAutoCancel(true)
        builder.setChannelId(CHANNEL_ID)
        builder.setCategory(NotificationCompat.CATEGORY_CALL)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.addAction(
                NotificationCompat.Action.Builder(
                        R.drawable.ic_call_end_24,
                        HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(ctx, R.color.md_red_700) + "\">Rechazar</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY),
                        cancelPendingIntent).build()
        )
        builder.addAction(
                NotificationCompat.Action.Builder(
                        R.drawable.ic_call_24,
                        HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(ctx, R.color.md_green_700) + "\">Responder</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY),
                        pendingIntent).build()
        )

        // Set notification content intent to take user to the fullscreen UI if user taps on the
        // notification body.
        builder.setContentIntent(pendingIntent)
        // Set full screen intent to trigger display of the fullscreen UI when the notification
        // manager deems it appropriate.
        builder.setFullScreenIntent(pendingFullScreenIntent, true)

        // Setup notification content.
        val number = CallStateManager.newCall?.getPhoneNumber()
        // Setup notification content.
        builder.setSmallIcon(R.drawable.ic_ring_volume_24)
        builder.setContentTitle(
                HtmlCompat.fromHtml("<font color=\"" + ContextCompat.getColor(ctx, R.color.md_indigo_700) + "\">$number</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY))
        builder.setSubText("")
        builder.setContentText("LLamada entrante")


        // Use builder.addAction(..) to add buttons to answer or reject the call.
        mgr.notify(notificationId, builder.build())

        launch {
            CallStateManager.callState.collect { callState ->
                //Log.e("EDER_NOTF", "${CallStateManager.callList[index].call!!.state}")
                if(callState.state == Call.STATE_DISCONNECTED) {
                    //Log.e("EDER", "LLAMADA PERDIDA")
                    mgr.cancel(notificationId)
                    job.cancel()
                }
            }
        }
    }
}