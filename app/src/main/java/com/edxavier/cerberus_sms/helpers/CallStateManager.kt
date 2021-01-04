package com.edxavier.cerberus_sms.helpers

import android.app.NotificationManager
import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCoroutinesApi
object CallStateManager {

    private  val _callState = MutableStateFlow(0)
    val callState: StateFlow<Int> = _callState

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            _callState.value = newState
        }
    }

    var call: Call? = null
        set(value) {
            field?.unregisterCallback(callback)
            value?.let {
                it.registerCallback(callback)
                _callState.value = it.state
            }
            field = value
        }

    fun answer() {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call?.disconnect()
    }
}