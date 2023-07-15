package com.edxavier.cerberus_sms.ui.core.states

import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Contact

data class UiState(
    var isLoading:Boolean = true,
    var bannerLoadFail:Boolean = false,
    var callLog: List<CallsLog> = listOf(),
    var callLogForNumber: List<CallsLog> = listOf(),
    var contacts: List<Contact> = listOf(),
    var contactNumbers: List<Contact> = listOf(),
    var dialContacts: List<Contact> = listOf(),
    var dialCalls: List<CallsLog> = listOf(),
    var dialShown: Boolean = false,
    var dialNumber: String = ""

)