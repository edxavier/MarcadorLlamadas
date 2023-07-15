package com.edxavier.cerberus_sms.helpers

import android.telecom.Call
import android.telecom.VideoProfile
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object MyCallsManager {
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    private val callsQueue:MutableList<Call> = ArrayList()
    var inCallUiShown = false
    var inCallNotificationId = -1

    var micOff: Boolean = false
    var speakerOn: Boolean = false
    var paused: Boolean = false
    var adSize: AdSize = AdSize.BANNER


    fun addCall(call: Call){
        callsQueue.add(call)
        ioScope.launch {
            FlowEventBus.publish(callsQueue)
        }

    }
    fun getCalls(): MutableList<Call> {
        return callsQueue
    }
    fun getActiveCall(): Call? {
        return callsQueue.find{ it.state == Call.STATE_ACTIVE}
    }
    fun getHoldCall(): Call? {
        return callsQueue.find{ it.state == Call.STATE_HOLDING}
    }
    fun getLatestCall(): Call{
        return callsQueue.last()
    }
    fun removeCall(call: Call){
        val disconnectedCall = callsQueue.find{ it == call}
        callsQueue.remove(disconnectedCall)
        ioScope.launch {
            FlowEventBus.publish(callsQueue)
        }
    }

    fun callStateChange(call:Call){
        ioScope.launch {
            FlowEventBus.publish(callsQueue)
            FlowEventBus.publish(call)
        }
    }
    fun answerRingingCall(){
        val ringingCall = callsQueue.find{ it.state == Call.STATE_RINGING}
        ringingCall?.let {
            it.answer(VideoProfile.STATE_AUDIO_ONLY)
            callStateChange(it)
        }
    }
    fun thereIsRingingOrDialingCall(): Boolean{
        val ringingCall = callsQueue.find{ it.state == Call.STATE_RINGING || it.state == Call.STATE_DIALING}
        return ringingCall!=null
    }
    fun thereIsIncomingCall(): Boolean{
        val ringingCall = callsQueue.find{ it.state == Call.STATE_RINGING}
        return ringingCall!=null
    }
    private fun thereIsActiveCall(): Boolean{
        val activeCall = callsQueue.find{ it.state == Call.STATE_ACTIVE}
        return activeCall!=null
    }
    fun disconnectCall(){
        if(callsQueue.size==1){
            disconnectUniqueCall()
        }else if(thereIsRingingOrDialingCall()){
            disconnectRingingCall()
        }else if(thereIsActiveCall() && callsQueue.size>1){
            disconnectActiveCall()
        }else{
            disconnectUniqueCall()
        }
    }
    private fun disconnectActiveCall(){
        val activeCall = callsQueue.find{ it.state == Call.STATE_ACTIVE}
        activeCall?.disconnect()
    }
    private fun disconnectRingingCall(){
        val ringingCall = callsQueue.find{ it.state == Call.STATE_RINGING}
        val dialingCall = callsQueue.find{ it.state == Call.STATE_DIALING}
        ringingCall?.disconnect()
        dialingCall?.disconnect()
    }
    private fun disconnectUniqueCall(){
        callsQueue.last().disconnect()
    }
}