package com.edxavier.cerberus_sms.helpers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.telecom.Call
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.edxavier.cerberus_sms.InCallActivity
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.services.CancelCallReceiver
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
object CallNotificationHelper{
    const val CHANNEL_ID = "com.edxavier.cerberus_sms.incoming.call"
    const val CHANNEL_NAME = "Llamadas entrantes"

    const val CHANNEL_ID_ACTIVE_CALL = "com.edxavier.cerberus_sms.active.call"
    const val CHANNEL_ACTIVE_CALL = "Llamadas en curso"
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    @SuppressLint("WrongConstant")
    fun sendIncomeCallNotification(ctx: Context){
        val notificationId = Calendar.getInstance().timeInMillis.toInt()
        MyCallsManager.inCallNotificationId = notificationId
        val incCallNotifMgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)
            if(MyCallsManager.getCalls().size>1){
                ioScope.launch {
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
            }else {
                channel.setSound(
                    ringtoneUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            incCallNotifMgr.createNotificationChannel(channel)
        }
        // Create an intent which triggers your fullscreen incoming call user interface.
        val intent = Intent(Intent.ACTION_MAIN, null)
        val fullIntent = Intent(Intent.ACTION_MAIN, null)
        val flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.flags = flags
        fullIntent.flags = flags

        intent.setClass(ctx, InCallActivity::class.java)
        fullIntent.setClass(ctx, InCallActivity::class.java)

        intent.putExtra("callNotificationId", notificationId)
        intent.putExtra("autoAnswer", 1)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(ctx, 1, intent, pendingIntentFlags)
        val pendingFullScreenIntent = PendingIntent.getActivity(ctx, 2, fullIntent, pendingIntentFlags)

        //Create an Intent for the BroadcastReceiver
        val cancelIntent = Intent(ctx, CancelCallReceiver::class.java)
        cancelIntent.putExtra("callNotificationId", notificationId)
        //Create the PendingIntent
        val cancelPendingIntent = PendingIntent.getBroadcast(ctx, 0, cancelIntent, pendingIntentFlags)


        // Build the notification as an ongoing high priority item; this ensures it will show as
        // a heads up notification which slides down over top of the current content.
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setAutoCancel(true)
        builder.setColorized(true)
        builder.color = ctx.getColor(R.color.md_green_500)
        builder.setSound(ringtoneUri)

        builder.setChannelId(CHANNEL_ID)
        builder.setCategory(NotificationCompat.CATEGORY_CALL)
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.addAction(
                NotificationCompat.Action.Builder(
                        R.drawable.ic_call_end_24,
                        HtmlCompat.fromHtml(
                            "<font color='${ContextCompat.getColor(ctx, R.color.md_red_700)}'>Rechazar</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                        ),
                        cancelPendingIntent).build()
        )
        builder.addAction(
                NotificationCompat.Action.Builder(
                        R.drawable.ic_call_24,
                        HtmlCompat.fromHtml(
                            "<font color='${ContextCompat.getColor(ctx, R.color.md_green_700)}'>Responder</font>",
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                        ),
                        pendingIntent).build()
        )

        // Set notification content intent to take user to the fullscreen UI if user taps on the
        // notification body.
        builder.setContentIntent(pendingIntent)
        // Set full screen intent to trigger display of the fullscreen UI when the notification
        // manager deems it appropriate.
        builder.setFullScreenIntent(pendingFullScreenIntent, true)

        // Setup notification content.
        ioScope.launch {
            val number = CallStateManager.newCall?.getPhoneNumber()
            val repo = RepoContact.getInstance(ctx)
            val contact = repo.getPhoneContact(number!!)

            // Setup notification content.
            builder.setSmallIcon(R.drawable.incoming_call)
            builder.setContentTitle(contact.name)
            builder.setContentText("Llamada entrante")
            // Use builder.addAction(..) to add buttons to answer or reject the call.
            incCallNotifMgr.notify(notificationId, builder.build())
            // Listen is call stop ringing
            FlowEventBus.subscribe<Call> {call ->
                if(call.state == Call.STATE_DISCONNECTED) {
                    incCallNotifMgr.cancel(notificationId)
                }else if(call.state == Call.STATE_ACTIVE) {
                    incCallNotifMgr.cancel(notificationId)
                }
            }
        }
    }

    fun showInCallNotification(ctx: Context){
        //val notificationId = Calendar.getInstance().timeInMillis.toInt()
        val notificationId = 5
        CallStateManager.activeCallNotificationId = notificationId
        MyCallsManager.inCallNotificationId = notificationId
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID_ACTIVE_CALL, CHANNEL_ACTIVE_CALL,
                    NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            channel.enableVibration(false)
            mgr.createNotificationChannel(channel)
        }
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setClass(ctx, InCallActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("callNotificationId", notificationId)
        val pendingIntent = PendingIntent.getActivity(ctx, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_ACTIVE_CALL)
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        builder.setAutoCancel(true)

        builder.setSound(null)
        builder.setVibrate(null)
        builder.setColorized(true)
        builder.color = ctx.getColor(R.color.md_green_500)
        builder.setChannelId(CHANNEL_ID_ACTIVE_CALL)
        builder.setContentIntent(pendingIntent)
        builder.setCategory(NotificationCompat.CATEGORY_CALL)
        builder.setOngoing(true)

        builder.setUsesChronometer(true)
        builder.setWhen(MyCallsManager.getLatestCall().details.connectTimeMillis)

        ioScope.launch {
            val number = CallStateManager.newCall?.getPhoneNumber()?.toPhoneFormat()
            val repo = RepoContact.getInstance(ctx)
            val contact = repo.getPhoneContact(number!!)
            // Setup notification content.
            builder.setSmallIcon(R.drawable.ic_call_24)
            builder.setContentTitle("Llamada en curso...")
            builder.setContentText(contact.name)
            // Use builder.addAction(..) to add buttons to answer or reject the call.
            mgr.notify(notificationId, builder.build())
        }
    }

}