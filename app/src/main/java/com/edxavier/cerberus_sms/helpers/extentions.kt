package com.edxavier.cerberus_sms.helpers

import android.content.Context
import android.telecom.Call
import android.view.View
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
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

fun Call.getPhoneNumber():String {
    return if (this.details.handle!=null)
        this.details.handle.schemeSpecificPart.toString()
    else
        "Desconocido??"
}

fun String.clearPhoneString(): String {
    return this.replace("%20","").replace("[^+*A-Za-z0-9]".toRegex(),"")
}
fun String.toPhoneFormat(): String {
    var temp:String = this.clearPhoneString()
    if(this.length >= 2 && this.startsWith("00"))
        temp = "+" + this.substring(2, this.length)
    if (temp.length == 12)
        temp = String.format("%s %s %s", temp.substring(0, 4), temp.substring(4, 8),
                temp.substring(8, 12))
    else if (temp.length == 8)
        temp = String.format("%s %s", temp.substring(0, 4), temp.substring(4, 8))
    return  temp
}

fun String.isValidPhoneNumber(): Boolean {
    return android.util.Patterns.PHONE.matcher(this.clearPhoneString()).matches()
}

fun View.visible(){ this.visibility = View.VISIBLE }
fun View.invisible(){ this.visibility = View.GONE }


fun Int.getOperatorString(): String {
    var op = ""
    when {
        this == Operator.UNKNOWN -> op = "Indefinido"
        this == Operator.CLARO -> op ="Claro"
        this == Operator.MOVISTAR -> op ="Movistar"
        this == Operator.COOTEL -> op ="Cootel"
        this == Operator.CONVENTIONAL -> op ="Línea fija"
        this == Operator.INTERNATIONAL -> op ="Internacional"
        this == Operator.KOLBI -> op ="Kolbi"
        this == Operator.TIGO -> op ="Tigo"
        this == Operator.HONDUTEL -> op ="Hondutel"
        this == Operator.DIGICEL -> op ="Digicel"
        this == Operator.INTELFON -> op ="Intelfon"
    }
    return op
}


fun Int.getOperatorColor(context: Context): Int {
    var op: Int = ContextCompat.getColor(context, R.color.md_teal_500)
    when {
        this == Operator.CLARO -> op = ContextCompat.getColor(context, R.color.md_red_700)
        this == Operator.TIGO -> op = ContextCompat.getColor(context, R.color.md_indigo_700)
        this == Operator.MOVISTAR -> op = ContextCompat.getColor(context, R.color.md_green_700)
        this == Operator.COOTEL -> op = ContextCompat.getColor(context, R.color.md_deep_orange_500)
        this == Operator.CONVENTIONAL -> op = ContextCompat.getColor(context, R.color.md_blue_700)
        this == Operator.INTERNATIONAL -> op = ContextCompat.getColor(context, R.color.md_purple_700)
        this == Operator.UNKNOWN -> op = ContextCompat.getColor(context, R.color.md_black_1000)
    }
    return op
}
