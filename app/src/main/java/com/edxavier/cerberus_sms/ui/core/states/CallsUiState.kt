package com.edxavier.cerberus_sms.ui.core.states

import com.edxavier.cerberus_sms.ui.screens.incall.InCallDto

data class CallsUiState(
    var callsQueue:List<InCallDto> = ArrayList(),
    var ringingOrDialing: Boolean = false,
    var incomingCall: Boolean = false,

    var micOff: Boolean = false,
    var speakerOn: Boolean = false,
    var paused: Boolean = false,
    var showDialPad: Boolean = false,

)