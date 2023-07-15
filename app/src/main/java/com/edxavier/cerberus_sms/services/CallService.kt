package com.edxavier.cerberus_sms.services

import android.app.NotificationManager
import android.content.Context
import android.telecom.*
import com.edxavier.cerberus_sms.InCallActivity
import com.edxavier.cerberus_sms.helpers.*
import kotlinx.coroutines.*


@ExperimentalCoroutinesApi
class CallService: InCallService() {

    companion object {
        @Volatile
        private var instance: CallService? = null
        fun getInstance():CallService?{
            return instance
        }
        fun setInstance(service: CallService){
                instance=service
        }
    }
    override fun onCallAdded(call: Call) {

        // var accHandle = call.details.accountHandle
        // getCallSim(accHandle.id)
        setInstance(this)
        call.registerCallback(callback)
        MyCallsManager.addCall(call)

        CallStateManager.newCall = call
        CallStateManager.callService = this

        // Its a outgoing call
        if(call.state == Call.STATE_CONNECTING || call.state == Call.STATE_DIALING)
            InCallActivity.start(this, call)
        //Its a incoming call
        else if(call.state == Call.STATE_RINGING && !MyCallsManager.inCallUiShown)
            CallNotificationHelper.sendIncomeCallNotification(this )

    }

    override fun onCallRemoved(call: Call) {
        // Buscar la llamada dentro de la lista y eliminarla
        call.unregisterCallback(callback)
        MyCallsManager.removeCall(call)

        val callIndex = CallStateManager.getCallIndex(call)
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(callIndex>=0) {
            val ch = CallStateManager.callList[callIndex]
            ch.call?.unregisterCallback(callback)
            if(CallStateManager.callList.size>1) {
                CallStateManager.callList.removeAt(callIndex)
                CallStateManager.updateUi.value = CallStateManager.callList.size
            }else if(!CallStateManager.callActivityShown){
                CallStateManager.callList.removeAt(callIndex)
                mgr.cancel(CallStateManager.activeCallNotificationId)
            }
            //Log.e("EDER ","Calls: ${CallStateManager.callList.size}")
        }
    }


    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call?, newState: Int) {
            call?.let {
                CallStateManager.pushStateChange(CallState(call, newState))
                MyCallsManager.callStateChange(it)
            }
        }

    }


}