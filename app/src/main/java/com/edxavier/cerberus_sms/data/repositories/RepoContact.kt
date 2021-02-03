package com.edxavier.cerberus_sms.data.repositories

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.util.Log
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.helpers.clearPhoneString
import com.edxavier.cerberus_sms.helpers.toPhoneFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.*


class RepoContact(private var context: Context) {

    val contentResolver: ContentResolver = context.contentResolver

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: RepoContact? = null
        fun getInstance(context: Context) =
                instance ?: synchronized(this) {
                    instance ?: RepoContact(context).also { instance = it }
                }
    }

    fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getPhoneContact(phoneNumber: String):Contact = withContext(Dispatchers.IO){
        //buscar tal como se recibe
        //Log.e("EDER", "getPhoneContact")
        var contact = getPhoneContact(phoneNumber, false)
        // si no hay resultado, son 8 digitos y no inicia con 505, buscar agregando el 505
        /*contact?.let {
            Log.e("EDER", "Contacto encontrado")
            Log.e("EDER", it.name)
        }*/
        if (contact==null && phoneNumber.length==8 && !phoneNumber.startsWith("+505")){
            contact = getPhoneContact(phoneNumber, true)
        }else if(contact==null && phoneNumber.length>8 && phoneNumber.startsWith("+505")){
            //Si no hay resultado, son mas de 8 digitosm e inicia con 505, buscar sin el 505
            contact = getPhoneContact(phoneNumber.substring(4, phoneNumber.length), false)
        }        //regresar el resultaso o el mismo numero formateado
        return@withContext contact ?: Contact(name = phoneNumber.toPhoneFormat())
    }

    private suspend fun getPhoneContact(phoneNumber: String, includeNicCode: Boolean = false):Contact? = withContext(
        Dispatchers.IO
    ){
        val PROJECTION = arrayOf(
            Phone.CONTACT_ID,
            Phone.DISPLAY_NAME,
            Phone.HAS_PHONE_NUMBER,
            Phone.PHOTO_URI,
            Phone.NUMBER
        )

        //val FILTER = "${ContactsContract.CommonDataKinds.Phone.NUMBER}=? and ${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER}=?"
        var tempNumber = phoneNumber
        //Log.e("EDER1", tempNumber)
        if(includeNicCode)
            tempNumber = "+505$tempNumber"
        //Log.e("EDER2", tempNumber)

        val FILTER = "${Phone.HAS_PHONE_NUMBER}=?"
        val uri: Uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(tempNumber))

        val contactCursor = contentResolver.query(
            uri,
            PROJECTION, FILTER, arrayOf("1"),
            Phone.DISPLAY_NAME + ""
        )

        if (contactCursor!=null){
            var name = phoneNumber.toPhoneFormat()
            var photo = ""
            while (contactCursor.moveToNext()){
                name = contactCursor.getString(contactCursor.getColumnIndex(Phone.DISPLAY_NAME))
                //Log.e("EDER_name", name)
                photo = contactCursor.getString(contactCursor.getColumnIndex(Phone.PHOTO_URI)) ?:""
            }
            if (contactCursor.count>0)
                return@withContext Contact(name = name, photo = photo)
            else
                return@withContext null
        }else
            return@withContext null
    }

    suspend fun getContactNumbers(id:Int):List<Contact> = withContext(Dispatchers.IO){
        val contactList: MutableList<Contact> =  ArrayList()
        val repoOperator = RepoOperator.getInstance(context)
        val PROJECTION = arrayOf(
            Phone.CONTACT_ID,
            //Phone._ID,
            //Phone.DISPLAY_NAME,
            //Phone.HAS_PHONE_NUMBER,
            //Phone.PHOTO_URI,
            Phone.NUMBER
        )

        //val FILTER = "${ContactsContract.CommonDataKinds.Phone.NUMBER}=? and ${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER}=?"

        val FILTER = "${Phone.HAS_PHONE_NUMBER}=? AND ${Phone.CONTACT_ID}=?"

        val contactCursor = contentResolver.query(
            Phone.CONTENT_URI,
            PROJECTION, FILTER, arrayOf("1", id.toString()),
            Phone.NUMBER
        )

        if (contactCursor!=null){
            var prevNumber = ""
            while (contactCursor.moveToNext()){
                val contact = Contact()
                contact.id = contactCursor.getString(contactCursor.getColumnIndex(Phone.CONTACT_ID)).toInt()
                contact.number = contactCursor.getString(contactCursor.getColumnIndex(Phone.NUMBER))
                contact.operator = repoOperator.getOperator(contact.number)
                //Log.e("EDER", "${contact.number} ${contact.id} -> ${contactCursor.getString(contactCursor.getColumnIndex(Phone._ID))}}")
                if(prevNumber.clearPhoneString() != contact.number.clearPhoneString()) {
                    contactList.add(contact)
                    prevNumber = contact.number
                }
            }
        }
        return@withContext contactList
    }

    suspend fun getPhoneContactsList():List<Contact> = withContext(Dispatchers.IO){
        val contactList: MutableList<Contact> =  ArrayList()
        val repoOperator = RepoOperator.getInstance(context)
        val PROJECTION = arrayOf(
            Contacts._ID,
            Contacts.DISPLAY_NAME,
            Contacts.HAS_PHONE_NUMBER,
            Contacts.PHOTO_URI
        )

        //val FILTER = "${ContactsContract.CommonDataKinds.Phone.NUMBER}=? and ${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER}=?"

        val FILTER = "${Contacts.HAS_PHONE_NUMBER}=?"

        val contactCursor = contentResolver.query(
            Contacts.CONTENT_URI,
            PROJECTION, FILTER, arrayOf("1"),
            Phone.DISPLAY_NAME + ""
        )

        if (contactCursor!=null){
            while (contactCursor.moveToNext()){
                val contact = Contact()
                contact.id = contactCursor.getString(contactCursor.getColumnIndex(Contacts._ID)).toInt()
                contact.name = contactCursor.getString(contactCursor.getColumnIndex(Contacts.DISPLAY_NAME))
                contact.photo = contactCursor.getString(contactCursor.getColumnIndex(Contacts.PHOTO_URI)) ?:""
                contactList.add(contact)
            }
        }
        return@withContext contactList
    }


}