package com.edxavier.cerberus_sms.services

import android.content.Intent
import android.os.IBinder
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import com.edxavier.cerberus_sms.CallActivity
import com.edxavier.cerberus_sms.helpers.CallNotificationHelper
import com.edxavier.cerberus_sms.helpers.CallStateManager
import com.edxavier.cerberus_sms.helpers.stateToString
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class CallService: InCallService() {


    override fun onCallAdded(call: Call) {
        call.details.handle.schemeSpecificPart?.let { Log.e("EDER", it) }
        val bundle = call.details.intentExtras
        val bundle2 = call.details.extras

        bundle?.let {
            Log.e("EDER_b1", it.isEmpty.toString())
            Log.e("EDER_b1", it.toString())
            Log.e("EDER_b1", it.getInt("simId", -1).toString())
        }

        bundle2?.let {
            Log.e("EDER_b2", it.toString())
            Log.e("EDER_b2", it.getInt("simId", -1).toString())
        }
        //this.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        CallStateManager.call = call

        CallStateManager.callService = this

        // Its a outgoing call
        if(call.state == Call.STATE_CONNECTING || call.state == Call.STATE_DIALING)
            CallActivity.start(this, call)
        //Its a incoming call
        else if(call.state == Call.STATE_RINGING)
            CallNotificationHelper.sendNotification(this )
    }

    override fun onCallRemoved(call: Call) {
        CallStateManager.call = null
    }

}