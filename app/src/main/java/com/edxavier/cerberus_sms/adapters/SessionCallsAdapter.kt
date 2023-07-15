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
import com.edxavier.cerberus_sms.databinding.CallList2Binding
import com.edxavier.cerberus_sms.databinding.ContactItemBinding
import com.edxavier.cerberus_sms.helpers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    class ViewHolder(val bindig: CallList2Binding): RecyclerView.ViewHolder(bindig.root){
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
                                        bindig.callStatus.text = callHandle.seconds.timeFormat()
                                    if (it.call?.state == Call.STATE_HOLDING)
                                        bindig.callStatus.text = it.call?.state?.stateToString()
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
                                        bindig.callStatus.text = it.state.stateToString()
                                }
                    }

                    bindig.callStatus.text = callHandle.call?.state?.stateToString()
                    callHandle.call?.getPhoneNumber()?.let { callNUmber ->
                        //Log.e("EDER_NUM", "---------$callNUmber---------")
                        val repo = RepoOperator.getInstance(context)
                        val repoContact = RepoContact.getInstance(context)
                        cScope.launch {
                            val formattedPhone = callNUmber.toPhoneFormat()
                            bindig.callDisplayContact.text = formattedPhone
                            //Log.e("EDER_NUM", "---------$formattedPhone---------")
                            val op = repo.getOperator(callNUmber)

                            if(repoContact.hasReadContactsPermission()){
                                val contact = repoContact.getPhoneContact(callNUmber)
                                //val contact2 = repoContact.getPhoneContact(formattedPhone)
                                //Log.e("EDER_NUM", "---------${contact2.name}---------")

                                bindig.callDisplayContact.text = contact.name
                                if (contact.photo.isNotBlank())
                                    bindig.callAvatar.load(Uri.parse(contact.photo)){
                                        transformations(CircleCropTransformation())
                                    }
                            }
                            if(op!=null){
                                bindig.callCountry.visible()
                                if(op.operator!= Operator.INTERNATIONAL)
                                    bindig.operatorCard.visible()
                                else
                                    bindig.operatorCard.invisible()
                                bindig.callOperator.text = op.operator.getOperatorString()
                                bindig.operatorCard.setCardBackgroundColor(op.operator.getOperatorColor(context))
                                var tmp = ""
                                tmp = if(op.area.isNotBlank())
                                    "${op.area}, ${op.country}"
                                else
                                    op.country
                                bindig.callCountry.text = tmp
                            }else{
                                bindig.callCountry.text = ""
                                bindig. callCountry.invisible()
                                bindig.operatorCard.visible()
                                bindig.callOperator.text = Operator.UNKNOWN.getOperatorString()
                                bindig.operatorCard.setCardBackgroundColor(Operator.UNKNOWN.getOperatorColor(context))
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CallList2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), coroutineScope, context)
    }

}