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
                delay(1000)
                if(call==null) {
                    timerStarted = false
                    break
                }
                if (call?.state == Call.STATE_ACTIVE || call?.state == Call.STATE_HOLDING){
                    seconds += 1
                    CallStateManager.callHandleSeconds.value = CallHandle(call, seconds)
                }else{
                    timerStarted = false
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