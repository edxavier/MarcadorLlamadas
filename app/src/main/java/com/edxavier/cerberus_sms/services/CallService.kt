package com.edxavier.cerberus_sms.services

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.edxavier.cerberus_sms.CallActivity
import com.edxavier.cerberus_sms.helpers.CallNotificationHelper
import com.edxavier.cerberus_sms.helpers.CallState
import com.edxavier.cerberus_sms.helpers.CallStateManager
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class CallService: InCallService() {


    override fun onCallAdded(call: Call) {

        //call.details.handle.schemeSpecificPart?.let { Log.e("EDER", it) }
        //this.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
        call.registerCallback(callback)
        CallStateManager.newCall = call
        CallStateManager.callService = this

        // Its a outgoing call
        if(call.state == Call.STATE_CONNECTING || call.state == Call.STATE_DIALING)
            CallActivity.start(this, call)
        //Its a incoming call
        else if(call.state == Call.STATE_RINGING && !CallStateManager.callActivityShown)
            CallNotificationHelper.sendNotification(this )
        //else if(call.state == Call.STATE_RINGING && CallStateManager.callActivityShown)
        //    CallActivity.start(this, call)
    }

    override fun onCallRemoved(call: Call) {
        Log.e("EDER", "onCallRemoved")
        //Buscar la llamada dentro de la lista y eliminarla
        //CallStateManager.newCall = null
        //Log.e("EDER ","Calls: ${CallStateManager.callList.size}")
        val callIndex = CallStateManager.getCallIndex(call)
        if(callIndex>=0) {
            val ch = CallStateManager.callList[callIndex]
            ch.call?.unregisterCallback(callback)
            CallStateManager.callList.removeAt(callIndex)
            CallStateManager.updateUi.value = CallStateManager.callList.size
            //Log.e("EDER ","Calls: ${CallStateManager.callList.size}")
        }
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, newState: Int) {
            //Log.e("EDER", "callHandle INDX ${CallStateManager.getCallIndex(call)} ${newState.stateToString()}")
            call?.let {
                CallStateManager.pushStateChange(CallState(call, newState))
            }
        }
    }


}