package com.edxavier.cerberus_sms.helpers

import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
class CallHandle(
        var call: Call? = null,
        var seconds:Int = 0
){
    val context: CoroutineContext = Job() + Dispatchers.Default

    fun getNumber():String{
        var number = ""
        call?.let{
            number = it.details.handle.schemeSpecificPart.toString()
        }
        return number
    }

    fun startTimer(){
        //Log.e("EDER", getNumber())
        //Log.e("EDER", "OBJ startTimer")
        val scope = CoroutineScope(context)
        scope.launch {
            while (true){
                delay(1000)
                if(call==null)
                    break
                if (call!!.state == Call.STATE_ACTIVE || call!!.state == Call.STATE_HOLDING){
                    seconds += 1
                    //Log.e("EDER", "${getNumber()}: $seconds")
                }else{
                    break
                }
            }
        }
    }

    fun answer() {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        call?.disconnect()
    }
    fun hold() {
        call?.hold()
    }
    fun unHold() {
        call?.unhold()
    }

}