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
                                    if(it.call?.state == Call.STATE_ACTIVE) {
                                        callStatus.text = callHandle.seconds.timeFormat()
                                    }else {
                                        it.call?.let { c ->
                                            callStatus.text = c.state.stateToString()
                                        }
                                    }

                                }
                    }
                    callDisplayContact.text = callHandle.call?.getPhoneNumber()
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