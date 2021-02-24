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
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.databinding.ContactItemBinding
import com.edxavier.cerberus_sms.helpers.*
import com.edxavier.cerberus_sms.ui.ContactDetailsActivity
import kotlinx.android.synthetic.main.call_list2.view.*


class ContactAdapter(val context: Context, val activity: Activity): ListAdapter<Contact, ContactAdapter.ViewHolder>(
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

    class ViewHolder(val bindig: ContactItemBinding): RecyclerView.ViewHolder(bindig.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = getItem(position)
        with(holder.bindig){
            contactItemName.text = contact.name
            if (contact.photo.isNotBlank()) {
                contactItemAvatar.load(Uri.parse(contact.photo)) {
                    placeholder(R.drawable.diente_leon)
                    error(R.drawable.diente_leon)
                    transformations(CircleCropTransformation())
                }
            }else {
                contactItemAvatar.load(R.drawable.diente_leon) {
                    error(R.drawable.diente_leon)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.diente_leon)
                }
            }

            contactItemContainer.setOnClickListener {
                val intent = Intent(context, ContactDetailsActivity::class.java).apply {
                    putExtra("contact_id", contact.id)
                    putExtra("contact_name", contact.name)
                    putExtra("contact_photo", contact.photo)
                }
                context.startActivity(intent)
                //val pair1:Pair<View, String> = Pair.create(contactItemAvatar as View, "profile")
                //val pair2:Pair<View, String> = Pair.create(contactItemName as View, "name")
                //val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
                //context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
                //context.startActivity(intent, options.toBundle())
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