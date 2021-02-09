package com.edxavier.cerberus_sms.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.telecom.Call
import android.view.View
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.google.android.material.snackbar.Snackbar
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
        this == Operator.UNKNOWN -> op = "DESCONOCIDO"
        this == Operator.CLARO -> op ="CLARO"
        this == Operator.MOVISTAR -> op ="MOVISTAR"
        this == Operator.COOTEL -> op ="COOTEL"
        this == Operator.CONVENTIONAL -> op ="LINEA FIJA"
        this == Operator.INTERNATIONAL -> op ="INTERNACIONAL"
        this == Operator.KOLBI -> op ="KOLBI"
        this == Operator.TIGO -> op ="TIGO"
        this == Operator.HONDUTEL -> op ="HONDUTEL"
        this == Operator.DIGICEL -> op ="DIGICEL"
        this == Operator.INTELFON -> op ="INTELFON"
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
        this == Operator.UNKNOWN -> op = ContextCompat.getColor(context, R.color.md_grey_700)
    }
    return op
}

fun Int.getCallDirectionIcon(): Int {
    var op: Int = R.drawable.ic_baseline_call_received_24
    when {
        this == CallLog.Calls.OUTGOING_TYPE -> op = R.drawable.ic_baseline_call_made_24
        this == CallLog.Calls.INCOMING_TYPE -> op = R.drawable.ic_baseline_call_received_24
        this == CallLog.Calls.MISSED_TYPE -> op = R.drawable.ic_baseline_call_missed_24
    }
    return op
}

fun Context.makeCall(number:String){
    val uri = "tel:" + number.replace("\\s+".toRegex(), "").replace("#", "%23")
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse(uri)
    startActivity(intent)
}