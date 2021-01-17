package com.edxavier.cerberus_sms.helpers

import android.annotation.SuppressLint
import android.telecom.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edxavier.cerberus_sms.R
import kotlinx.android.synthetic.main.call_list1.view.*
import kotlinx.android.synthetic.main.call_list1.view.callDisplayContact
import kotlinx.android.synthetic.main.call_list1.view.callStatus
import kotlinx.android.synthetic.main.call_list2.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@ExperimentalCoroutinesApi
class SessionCallsAdapter(
        private val coroutineScope: CoroutineScope
): ListAdapter<CallHandle, SessionCallsAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<CallHandle>() {
        override fun areItemsTheSame(oldItem: CallHandle, newItem: CallHandle): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: CallHandle, newItem: CallHandle): Boolean {
            return false
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        @SuppressLint("SetTextI18n")
        fun bind(callHandle: CallHandle, cScope: CoroutineScope){
            itemView.apply {
                with(this){
                    cScope.launch {
                        CallStateManager.callHandleSeconds
                                .filter { ch ->
                                    ch.call?.getPhoneNumber() == callHandle.call?.getPhoneNumber()
                                }
                                .collect {
                                    if(it.call?.state == Call.STATE_ACTIVE)
                                        callStatus.text = callHandle.seconds.timeFormat()
                                    try {
                                        if (it.call?.state == Call.STATE_HOLDING) {
                                            callHangupBtn.visibility = View.VISIBLE
                                            callStatus.text = it.call?.state?.stateToString()
                                        }else
                                            callHangupBtn.visibility = View.GONE
                                    }catch (e:Exception){}

                                }
                    }
                    cScope.launch {
                        CallStateManager.callState
                                .filter { ch ->
                                    ch.call?.getPhoneNumber() == callHandle.call?.getPhoneNumber()
                                }
                                .collect {
                                    //Log.e("EDER:OBJ_STAT", it.state.stateToString())
                                    //it.call?.let { c-> Log.e("EDER:OBJ_STAT2", c.getPhoneNumber()) }
                                    if(it.state != Call.STATE_ACTIVE)
                                        callStatus.text = it.state.stateToString()
                                }
                    }
                    callDisplayContact.text = callHandle.call?.getPhoneNumber()
                    callStatus.text = callHandle.call?.state?.stateToString()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (itemCount>1)
            ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.call_list2, parent, false))
        else
            ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.call_list1, parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), coroutineScope)
    }

}