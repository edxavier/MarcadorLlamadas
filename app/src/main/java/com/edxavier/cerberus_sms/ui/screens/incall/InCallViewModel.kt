package com.edxavier.cerberus_sms.ui.screens.incall

import android.content.Context
import android.media.ToneGenerator
import android.telecom.Call
import android.telecom.CallAudioState
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edxavier.cerberus_sms.CallActivity
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.helpers.CallStateManager
import com.edxavier.cerberus_sms.helpers.FlowEventBus
import com.edxavier.cerberus_sms.helpers.MyCallsManager
import com.edxavier.cerberus_sms.helpers.getPhoneNumber
import com.edxavier.cerberus_sms.services.CallService
import com.edxavier.cerberus_sms.ui.core.states.CallsUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*


@OptIn(ExperimentalCoroutinesApi::class)
class InCallViewModel(private val repo: RepoContact): ViewModel() {

    val repoOperator = RepoOperator.getInstance(repo.mContext)

    private val _uiState = MutableStateFlow(CallsUiState())
    val uiState: StateFlow<CallsUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            FlowEventBus.subscribe<MutableList<Call>> { inCalls->
                val calls: MutableList<InCallDto> =  ArrayList()
                inCalls.forEach {
                    val contacts = repo.getPhoneContact(it.getPhoneNumber())
                    var connectTime = it.details.connectTimeMillis
                    connectTime = if (connectTime==0L) Date().time else connectTime
                    val secs = if(connectTime<=0L){
                        -1
                    }else{
                        ((Date().time - it.details.connectTimeMillis)/1000).toInt()
                    }
                    calls.add(InCallDto(call = it, contact = contacts, elapsedSeconds = secs))
                }
                if(inCalls.isNotEmpty() && MyCallsManager.speakerOn){
                    CallService.getInstance()?.apply {
                        setAudioRoute(CallAudioState.ROUTE_SPEAKER)
                    }
                }
                MyCallsManager.paused = (inCalls.size==1 && MyCallsManager.getHoldCall()!=null)
                var showDial = uiState.value.showDialPad
                if(showDial && MyCallsManager.thereIsIncomingCall())
                    showDial = false
                _uiState.update { state ->
                    state.copy(
                        callsQueue = calls,
                        ringingOrDialing = MyCallsManager.thereIsRingingOrDialingCall(),
                        incomingCall = MyCallsManager.thereIsIncomingCall(),
                        speakerOn = MyCallsManager.speakerOn,
                        micOff = MyCallsManager.micOff,
                        paused = MyCallsManager.paused,
                        showDialPad = showDial
                    )
                }
            }
        }
    }
    fun setSpeakerOn() {
        _uiState.update { state ->
            CallService.getInstance()?.apply {
                setAudioRoute(CallAudioState.ROUTE_SPEAKER)
            }
            MyCallsManager.speakerOn = true
            state.copy(
                speakerOn = MyCallsManager.speakerOn
            )
        }
    }
    fun setSpeakerOff() {
        _uiState.update { state ->
            MyCallsManager.speakerOn = false
            CallService.getInstance()?.setAudioRoute(CallAudioState.ROUTE_WIRED_OR_EARPIECE)
            state.copy(
                speakerOn = MyCallsManager.speakerOn
            )
        }
    }

    fun setMicOn() {
        _uiState.update { state ->
            MyCallsManager.micOff = false
            CallService.getInstance()?.setMuted(false)
            state.copy(
                micOff = MyCallsManager.micOff
            )
        }
    }

    fun setMicOff() {
        _uiState.update { state ->
            MyCallsManager.micOff = true
            CallService.getInstance()?.setMuted(true)
            state.copy(
                micOff = MyCallsManager.micOff
            )
        }
    }
    fun setOnHold() {
        MyCallsManager.getActiveCall()?.hold()
        if(MyCallsManager.getCalls().size==1) {
            MyCallsManager.paused = true
            _uiState.update { state ->
                state.copy(
                    paused = true
                )
            }
        }
    }
    fun setUnHold() {
        val activeCall = MyCallsManager.getActiveCall()
        val holdCall = MyCallsManager.getHoldCall()
        if(MyCallsManager.getCalls().size>1 && activeCall!=null){
            activeCall.hold()
        }else{
            holdCall?.unhold()
        }
        if(MyCallsManager.getCalls().size==1) {
            MyCallsManager.paused = false
            _uiState.update { state ->
                state.copy(
                    paused = false
                )
            }
        }
    }

    fun showDialPad() {
        _uiState.update { state ->
            state.copy(
                showDialPad = !state.showDialPad
            )
        }
    }
    fun playDTMFTone(digit: Char){
        MyCallsManager.getActiveCall()?.let {
            viewModelScope.launch {
                it.playDtmfTone(digit)
                delay(300)
                it.stopDtmfTone()
            }
        }
    }
}