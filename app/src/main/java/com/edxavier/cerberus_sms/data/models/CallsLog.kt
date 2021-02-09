package com.edxavier.cerberus_sms.data.models

import androidx.room.PrimaryKey
import java.util.*

data class CallsLog(
        var id: Int = -1,
        var operator: Operator? = null,
        var name: String = "",
        var number: String = "",
        var type: Int = -1,
        var duration: Int = 0,
        var callDate: Calendar = Calendar.getInstance(),
        var total:Int = 0
)