package com.edxavier.cerberus_sms.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.databinding.CallLogItemBinding
import com.edxavier.cerberus_sms.helpers.*
import java.text.SimpleDateFormat
import java.util.*


class CallLogAdapter(val context: Context, val activity: Activity): ListAdapter<CallsLog, CallLogAdapter.ViewHolder>(DiffCallback()){

    class DiffCallback: DiffUtil.ItemCallback<CallsLog>() {
        override fun areItemsTheSame(oldItem: CallsLog, newItem: CallsLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CallsLog, newItem: CallsLog): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(val bindig: CallLogItemBinding): RecyclerView.ViewHolder(bindig.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CallLogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val call = getItem(position)
        with(holder.bindig){
            callContactName.text = "${call.name} (${call.total})"
            callNumber.text = call.number.toPhoneFormat()
            callDuration.text = call.duration.timeFormat()
            callDirection.setImageResource(call.type.getCallDirectionIcon())
            val dateFormat = SimpleDateFormat("dd.MMMyy hh:mm a", Locale.getDefault())
            callDate.text = dateFormat.format(call.callDate.timeInMillis)
            call.operator?.let { op ->
                operatorCard.visible()
                callOperator.text = op.operator.getOperatorString()
                operatorCard.setCardBackgroundColor(op.operator.getOperatorColor(context))
                if(op.operator!= Operator.INTERNATIONAL)
                    operatorCard.visible()
                else
                    operatorCard.invisible()
                var tmp = ""
                tmp = if(op.area.isNotBlank())
                    "${op.area}, ${op.country}"
                else
                    op.country
                callCountry.text = tmp
            }
            if (call.operator==null)
                operatorCard.invisible()
            callLogContainer.setOnClickListener {
                val options = arrayOf("Llamar", "Escribir").toList()
                MaterialDialog(context).show {
                    title(text = call.name)
                    listItems(items = options) { _, index, _ ->
                        when(index){
                            0 -> {
                                context.makeCall(call.number)
                            }
                            1 -> {
                                context.sendSms(call.number)
                            }
                        }
                    }
                }
            }
        }
    }

}