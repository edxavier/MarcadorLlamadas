package com.edxavier.cerberus_sms.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.edxavier.cerberus_sms.helpers.CallStateManager
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class CancelCallReceiver: BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        // Listen for cancel call notification to disconnect call
        val notificationId = intent.getIntExtra("callNotificationId", 0)
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(notificationId)
        MyCallsManager.getLatestCall().disconnect()
    }
}