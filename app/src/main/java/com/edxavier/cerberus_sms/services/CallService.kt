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
        // Its a outgoing call
        if(call.state == Call.STATE_CONNECTING)
            CallActivity.start(this, call)
        //Its a incoming call
        else if(call.state == Call.STATE_RINGING)
            CallNotificationHelper.sendNotification(this )
    }

    override fun onCallRemoved(call: Call) {
        CallStateManager.call = null
    }

}