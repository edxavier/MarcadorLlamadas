package com.edxavier.cerberus_sms.helpers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.edxavier.cerberus_sms.R
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SessionCallsAdapter(): ListAdapter<CallHandle, SessionCallsAdapter.ViewHolder>(DiffCallback()) {

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
        fun bind(callHandle: CallHandle){
            itemView.apply {
                with(this){
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
        holder.bind(getItem(position))
    }

}