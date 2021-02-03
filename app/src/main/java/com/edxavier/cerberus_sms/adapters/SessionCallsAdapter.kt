package com.edxavier.cerberus_sms.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.telecom.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.data.repositories.RepoContact
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.helpers.*
import kotlinx.android.synthetic.main.call_list1.view.callDisplayContact
import kotlinx.android.synthetic.main.call_list1.view.callStatus
import kotlinx.android.synthetic.main.call_list2.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class SessionCallsAdapter(
        private val coroutineScope: CoroutineScope,
        private val context:Context
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
        fun bind(callHandle: CallHandle, cScope: CoroutineScope, context: Context){
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
                                    if (it.call?.state == Call.STATE_HOLDING)
                                        callStatus.text = it.call?.state?.stateToString()
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

                    callStatus.text = callHandle.call?.state?.stateToString()
                    callHandle.call?.getPhoneNumber()?.let {
                        val repo = RepoOperator.getInstance(context)
                        val repoContact = RepoContact.getInstance(context)
                        cScope.launch {
                            callDisplayContact.text = callHandle.call?.getPhoneNumber()?.toPhoneFormat()
                            val op = repo.getOperator(it)
                            if(op!=null){
                                callCountry.visible()
                                if(op.operator!= Operator.INTERNATIONAL)
                                    operatorCard.visible()
                                else
                                    operatorCard.invisible()
                                callOperator.text = op.operator.getOperatorString()
                                operatorCard.setCardBackgroundColor(op.operator.getOperatorColor(context))
                                var tmp = ""
                                tmp = if(op.area.isNotBlank())
                                    "${op.area}, ${op.country}"
                                else
                                    op.country
                                callCountry.text = tmp
                            }else{
                                callCountry.text = ""
                                callCountry.invisible()
                                operatorCard.visible()
                                callOperator.text = Operator.UNKNOWN.getOperatorString()
                                operatorCard.setCardBackgroundColor(Operator.UNKNOWN.getOperatorColor(context))
                            }

                            if(repoContact.hasReadContactsPermission()){
                                val contact = repoContact.getPhoneContact(callHandle.call?.getPhoneNumber()!!)
                                callDisplayContact.text = contact.name
                                if (contact.photo.isNotBlank())
                                    callAvatar.load(Uri.parse(contact.photo)){
                                        transformations(CircleCropTransformation())
                                    }
                            }
                        }
                    }
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
        holder.bind(getItem(position), coroutineScope, context)
    }

}