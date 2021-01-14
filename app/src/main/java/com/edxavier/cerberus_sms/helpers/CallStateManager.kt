package com.edxavier.cerberus_sms.helpers

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

@ExperimentalCoroutinesApi
object CallStateManager {

    private  val _callState = MutableStateFlow(CallState())
    val updateUi = MutableStateFlow(0)
    val callHandleSeconds = MutableStateFlow(CallHandle())

    val callState: StateFlow<CallState> = _callState

    val callList:MutableList<CallHandle> = ArrayList()

    fun pushStateChange(callState:CallState){
        _callState.value = callState
    }

    var callService:InCallService? = null

    var newCall: Call? = null
        set(value) {
            value?.let {

                val handle = CallHandle()
                //it.registerCallback(handle.callback)
                handle.call = it
                //handle.seconds = Random(0).nextInt()
                _callState.value = CallState(it, it.state)
                callList.add(handle)
                updateUi.value = callList.size
                //adapter.submitList(callList)
            }
            field = value
        }


    fun getCallIndex(call:Call):Int{
        var callIndex = -1
        callList.forEachIndexed { index, handle ->
            handle.call?.let {
                if (it == call)
                    callIndex = index
            }
        }
        return callIndex
    }

    fun getActiveCallIndex():Int{
        var callIndex = -1
        callList.forEachIndexed { index, handle ->
            handle.call?.let {
                if (it.state == Call.STATE_ACTIVE)
                    callIndex = index
            }
        }
        return callIndex
    }

    fun muteMicrophone(mute:Boolean) {
        callService?.setMuted(mute)
    }

    fun setSpeakerOn() {
        callService?.setAudioRoute(CallAudioState.ROUTE_SPEAKER)
    }
    fun setSpeakerOff() {
        callService?.setAudioRoute(CallAudioState.ROUTE_WIRED_OR_EARPIECE)
    }

    fun hold() {
        newCall?.hold()
    }
    fun unHold() {
        newCall?.unhold()
    }
}