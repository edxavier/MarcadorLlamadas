package com.edxavier.cerberus_sms.data.models

import androidx.room.PrimaryKey

data class Contact(
        var id: Int = -1,
        var operator: Operator? = null,
        var favorite: Boolean = false,
        var name: String = "",
        var number: String = "",
        var whatsapp: Boolean = false,
        var photo: String = "",
        var info: String = "",
        var lookupKey: String = ""
)