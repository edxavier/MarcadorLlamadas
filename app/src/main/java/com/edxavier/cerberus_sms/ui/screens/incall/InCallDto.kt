package com.edxavier.cerberus_sms.ui.screens.incall

import android.telecom.Call
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.data.models.Operator

class InCallDto(
    var call:Call,
    var contact: Contact,
    var elapsedSeconds: Int = 0
)