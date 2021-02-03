package com.edxavier.cerberus_sms.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.databinding.ContactItemBinding
import com.edxavier.cerberus_sms.databinding.ContactPhoneNumbersItemBinding
import com.edxavier.cerberus_sms.helpers.*
import com.edxavier.cerberus_sms.ui.ContactDetailsActivity
import kotlinx.android.synthetic.main.call_list2.view.*


class PhoneNumbersAdapter(val context: Context, val activity: Activity): ListAdapter<Contact, PhoneNumbersAdapter.ViewHolder>(
    DiffCallback()
){

    class DiffCallback: DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.equals(newItem)
        }
    }

    class ViewHolder(val bindig: ContactPhoneNumbersItemBinding): RecyclerView.ViewHolder(bindig.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContactPhoneNumbersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        with(holder.bindig){
            itemNumber.text = contact.number.toPhoneFormat()
            contact.operator?.let { op ->
                operatorCard.visible()
                itemOperator.text = op.operator.getOperatorString()
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
                itemCountry.text = tmp
            }

            
            /*contactItemNumber.text = contact.number.toPhoneFormat()
            contactItemCountry.text = ""
            operatorCard.invisible()
            contact.operator?.let { op ->
                operatorCard.visible()
                contactItemOperator.text = op.operator.getOperatorString()
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
                contactItemCountry.text = tmp
            }
            */
        }
    }

}