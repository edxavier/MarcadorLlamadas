package com.edxavier.cerberus_sms.helpers

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsLogger {
    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    private fun log(name: String, params: Bundle? = null) {
        analytics?.logEvent(name, params)
    }

    fun screenView(screenName: String) {
        log(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName) }
        )
    }

    fun callOutgoing() {
        log("call_outgoing", Bundle().apply {
            putString("type", "outgoing")
        })
    }

    fun callAnswered() {
        log("call_answered", Bundle().apply {
            putString("type", "incoming")
        })
    }

    fun callEnded(durationSeconds: Int) {
        log("call_ended", Bundle().apply {
            putString("duration", durationSeconds.toString())
            putLong("duration_seconds", durationSeconds.toLong())
        })
    }

    fun callMissed() {
        log("call_missed", Bundle().apply {
            putString("type", "missed")
        })
    }

    fun dialerOpened() {
        log("dialer_opened")
    }

    fun blockNumber() {
        log("block_number")
    }

    fun contactDetailViewed() {
        log("contact_detail_viewed")
    }

    fun permissionDenied(permission: String) {
        log("permission_denied", Bundle().apply {
            putString("permission_name", permission)
        })
    }
}
