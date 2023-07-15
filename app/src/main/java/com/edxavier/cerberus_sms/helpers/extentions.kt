package com.edxavier.cerberus_sms.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.telecom.Call
import android.telecom.PhoneAccount
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.BuildConfig
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.models.SimInfo
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
    if (this<0)
        return "00:00"
    val secondsLeft: Int = this % 3600 % 60
    val minutes = floor(this.toDouble() % 3600 / 60).toInt()
    val hours = floor(this.toDouble() / 3600).toInt()

    val hh = (if (hours < 10) "0" else "") + hours
    val mm = (if (minutes < 10) "0" else "") + minutes
    val ss = (if (secondsLeft < 10) "0" else "") + secondsLeft
    var result = ""
    if (hh != "00") result = "$hh:"
    result = "$result$mm:"
    result = "$result$ss"
    return result
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
    var temp:String = this
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
        this == Operator.TIGO -> op = Color(0xFF3F51B5).toArgb()
        this == Operator.MOVISTAR -> op = ContextCompat.getColor(context, R.color.md_green_700)
        this == Operator.COOTEL -> op = ContextCompat.getColor(context, R.color.md_deep_orange_500)
        this == Operator.CONVENTIONAL -> op = Color(0xFF19A7CE).toArgb()
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
        this == CallLog.Calls.REJECTED_TYPE -> op = R.drawable.reject_icon
        this == CallLog.Calls.BLOCKED_TYPE -> op = R.drawable.baseline_block_24

    }
    return op
}


fun Context.makeCall(number:String){
    val uri = "tel:" + number.replace("\\s+".toRegex(), "").replace("#", "%23")
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse(uri)
    startActivity(intent)
}

fun Context.sendSms(number:String){
    val intent2 = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
    intent2.putExtra("sms_body", "")
    startActivity(intent2)
}

fun Context.isDefaultDialerApp(): Boolean{
    val telManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    return telManager.defaultDialerPackage == BuildConfig.APPLICATION_ID
}

fun Context.hasReadCallLogPermissions(): Boolean{
    return checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasRequiredPermissions(): Boolean{
    // val readCallLog = checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    val readPhoneState = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val readContacts = checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    val callPhone = checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    // val writeContacts = checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    var perms = readContacts && readPhoneState && callPhone
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val readPhoneNumbers = checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
        perms = perms && readPhoneNumbers
    }
    return perms
}

fun Context.getCallSim(accountId: String): SimInfo {
    val hasReadPhoneStatePermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val hasReadPhoneNumbersPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
         checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
    }else
        true
    var simInfo = SimInfo()
    if(hasReadPhoneNumbersPermission && hasReadPhoneStatePermission) {
        simInfo = getSimInfoBySubsManager(
            accountId
        )
        if (simInfo.slot == -1) {
            simInfo = getSimInfoByAccManager(
                accountId
            )
        }
    }
    return simInfo
}

@SuppressLint("MissingPermission")
fun Context.getSimInfoBySubsManager(
    accountId: String
): SimInfo{
    val simInfo = SimInfo()
    val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    try {
        for (subscriptionInfo in subscriptionManager.activeSubscriptionInfoList) {
            if (accountId == subscriptionInfo.iccId ||
                accountId == subscriptionInfo.subscriptionId.toString()
            ){
                simInfo.slot = subscriptionInfo.simSlotIndex + 1
                simInfo.carrier = subscriptionInfo.carrierName.toString()
                break
            }
        }
    }catch (e: Throwable){e.printStackTrace()}
    return simInfo
}

fun Context.getSubscriptions(): List<String>{
    val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val subs = arrayListOf<String>()
    val hasReadPhoneStatePermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val hasReadPhoneNumbersPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
    }else
        true
    if(hasReadPhoneNumbersPermission && hasReadPhoneStatePermission) {
        try {
            for (subscriptionInfo in subscriptionManager.activeSubscriptionInfoList) {
                subs.add(subscriptionInfo.iccId)
            }
        } catch (e: Throwable) {
            subs.add("ERROR GETTING SUBS")
        }
    }
    return subs
}



fun Context.getAccounts(): List<String>{

    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val callCapablePhoneAccounts = telecomManager.callCapablePhoneAccounts
    val subs = arrayListOf<String>()
    val hasReadPhoneStatePermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val hasReadPhoneNumbersPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
    }else
        true
    if(hasReadPhoneNumbersPermission && hasReadPhoneStatePermission) {
        try {
            callCapablePhoneAccounts?.forEachIndexed { index, phoneAccountHandle ->
               subs.add(phoneAccountHandle.id)
            }
        } catch (e: Throwable) {
            subs.add("ERROR GETING ACC")
        }
    }
    return subs
}
@SuppressLint("MissingPermission")
fun Context.getSimInfoByAccManager(
accountId: String
): SimInfo{
    val simInfo = SimInfo()
    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val callCapablePhoneAccounts = telecomManager.callCapablePhoneAccounts

    run {
        callCapablePhoneAccounts?.forEachIndexed { index, phoneAccountHandle ->
            val phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle)
            if (phoneAccountHandle.id != accountId)
                return@forEachIndexed
            if (!phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION))
                return@run
            //found the sim card index
            simInfo.carrier = phoneAccount.shortDescription.toString()
            simInfo.slot = index + 1
            return@run
        }
    }
    return simInfo
}