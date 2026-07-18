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
    private val lock = Any()
    private val callsQueue: MutableList<Call> = ArrayList()
    var inCallUiShown = false
    var inCallNotificationId = -1

    var micOff: Boolean = false
    var speakerOn: Boolean = false
    var paused: Boolean = false
    var adSize: AdSize = AdSize.BANNER

    fun addCall(call: Call) {
        synchronized(lock) { callsQueue.add(call) }
        ioScope.launch { FlowEventBus.publish(ArrayList(callsQueue)) }
    }

    fun getCalls(): MutableList<Call> {
        synchronized(lock) { return ArrayList(callsQueue) }
    }

    fun getActiveCall(): Call? {
        synchronized(lock) { return callsQueue.find { it.state == Call.STATE_ACTIVE } }
    }

    fun getHoldCall(): Call? {
        synchronized(lock) { return callsQueue.find { it.state == Call.STATE_HOLDING } }
    }

    fun getLatestCall(): Call {
        synchronized(lock) { return callsQueue.last() }
    }

    fun removeCall(call: Call) {
        synchronized(lock) {
            val disconnectedCall = callsQueue.find { it == call }
            callsQueue.remove(disconnectedCall)
        }
        ioScope.launch { FlowEventBus.publish(ArrayList(callsQueue)) }
    }

    fun callStateChange(call: Call) {
        ioScope.launch {
            FlowEventBus.publish(ArrayList(callsQueue))
            FlowEventBus.publish(call)
        }
    }

    fun answerRingingCall() {
        synchronized(lock) {
            callsQueue.find { it.state == Call.STATE_RINGING }?.let {
                it.answer(VideoProfile.STATE_AUDIO_ONLY)
                ioScope.launch {
                    FlowEventBus.publish(ArrayList(callsQueue))
                    FlowEventBus.publish(it)
                }
            }
        }
    }

    fun thereIsRingingOrDialingCall(): Boolean {
        synchronized(lock) {
            return callsQueue.any { it.state == Call.STATE_RINGING || it.state == Call.STATE_DIALING }
        }
    }

    fun thereIsIncomingCall(): Boolean {
        synchronized(lock) {
            return callsQueue.any { it.state == Call.STATE_RINGING }
        }
    }

    private fun thereIsActiveCall(): Boolean {
        synchronized(lock) {
            return callsQueue.any { it.state == Call.STATE_ACTIVE }
        }
    }

    fun disconnectCall() {
        synchronized(lock) {
            when {
                callsQueue.size == 1 -> disconnectUniqueCall()
                thereIsRingingOrDialingCall() -> disconnectRingingCall()
                thereIsActiveCall() && callsQueue.size > 1 -> disconnectActiveCall()
                else -> disconnectUniqueCall()
            }
        }
    }

    private fun disconnectActiveCall() {
        callsQueue.find { it.state == Call.STATE_ACTIVE }?.disconnect()
    }

    private fun disconnectRingingCall() {
        callsQueue.find { it.state == Call.STATE_RINGING }?.disconnect()
        callsQueue.find { it.state == Call.STATE_DIALING }?.disconnect()
    }

    private fun disconnectUniqueCall() {
        callsQueue.last().disconnect()
    }
}
