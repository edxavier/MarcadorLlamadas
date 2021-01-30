package com.edxavier.cerberus_sms.helpers

import android.content.Context
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CallHandle(
        var call: Call? = null,
        var seconds:Int = 0,
        var timerStarted:Boolean = false
){
    private val context: CoroutineContext = Job() + Dispatchers.Default


    fun startTimer(){
        timerStarted = true
        //Log.e("EDER", getNumber())
        val scope = CoroutineScope(context)
        scope.launch {
            while (true){
                if(call==null) {
                    timerStarted = false
                    break
                }
                if (call?.state == Call.STATE_ACTIVE || call?.state == Call.STATE_HOLDING){
                    //seconds += 1
                    seconds = ((Date().time - call!!.details.connectTimeMillis)/1000).toInt()
                    //Log.e("EDER", "$seconds")
                    CallStateManager.callHandleSeconds.value = CallHandle(call, seconds)
                }else{
                    timerStarted = false
                    break
                }
                delay(1000)
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