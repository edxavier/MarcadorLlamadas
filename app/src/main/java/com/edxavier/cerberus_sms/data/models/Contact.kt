package com.edxavier.cerberus_sms.data.models

import androidx.room.PrimaryKey

class Contact(
        var id: Int = -1,
        var operator: Operator? = null,
        var name: String = "",
        var number: String = "",
        var photo: String = ""
)