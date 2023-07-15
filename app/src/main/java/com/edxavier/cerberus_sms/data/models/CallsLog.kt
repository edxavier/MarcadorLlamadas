package com.edxavier.cerberus_sms.data.models

import androidx.room.PrimaryKey
import java.util.*

data class CallsLog(
        var id: Int = -1,
        var operator: Operator? = null,
        var sim: Int = -1,
        var carrier: String = "",
        var account_id: String = "",
        var name: String = "",
        var number: String = "",
        var formatted_number: String = "",
        var photoUri: String = "",
        var type: Int = -1,
        var duration: Int = 0,
        var callDate: Calendar = Calendar.getInstance(),
        var total:Int = 0,
        var isBlocked:Boolean = false,
        var subs: List<String> = listOf(),
        var accs: List<String> = listOf()
)