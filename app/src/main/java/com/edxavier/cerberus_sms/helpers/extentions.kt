package com.edxavier.cerberus_sms.helpers

import android.telecom.Call
import kotlin.math.floor


fun Int.stateToString (): String{
    return when(this){
        Call.STATE_RINGING -> "LLamada entrante"
        Call.STATE_CONNECTING -> "Conectando..."
        Call.STATE_ACTIVE -> "Activa"
        Call.STATE_HOLDING -> "En espera"
        Call.STATE_DIALING -> "LLamando..."
        Call.STATE_DISCONNECTED -> "Desconectado"
        Call.STATE_DISCONNECTING -> "Desconectando..."
        else -> "Estado desconocido"
    }
}


fun Int.timeFormat (): String{
    val secondsLeft: Int = this % 3600 % 60
    val minutes = floor(this.toDouble() % 3600 / 60).toInt()
    val hours = floor(this.toDouble() / 3600).toInt()

    val hh = (if (hours < 10) "0" else "") + hours
    val mm = (if (minutes < 10) "0" else "") + minutes
    val ss = (if (secondsLeft < 10) "0" else "") + secondsLeft

    return "$hh:$mm:$ss"
}