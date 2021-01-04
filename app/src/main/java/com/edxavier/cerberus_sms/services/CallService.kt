package com.edxavier.cerberus_sms.services

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.edxavier.cerberus_sms.CallActivity
import com.edxavier.cerberus_sms.helpers.CallNotificationHelper
import com.edxavier.cerberus_sms.helpers.CallStateManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class CallService: InCallService() {

    override fun onCallAdded(call: Call) {
        CallStateManager.call = call
        CallNotificationHelper.sendNotification(this )
        //CallActivity.start(this, call)
    }

    override fun onCallRemoved(call: Call) {
        CallStateManager.call = null
    }

}