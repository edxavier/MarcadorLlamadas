package com.edxavier.cerberus_sms.data.repositories

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.util.Log
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.helpers.clearPhoneString
import com.edxavier.cerberus_sms.helpers.getOperatorString
import com.edxavier.cerberus_sms.helpers.toPhoneFormat
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class RepoContact(private var context: Context) {

    val contentResolver: ContentResolver = context.contentResolver

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: RepoContact? = null
        fun getInstance(ctxt: Context) =
                instance ?: synchronized(this) {
                    instance ?: RepoContact(ctxt).also { instance = it }
                }
    }

    fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getPhoneContact(phoneNumber: String):Contact = withContext(Dispatchers.IO){
        //buscar tal como se recibe
        //Log.e("EDER", "getPhoneContact")
        var contact:Contact? = null
        if(phoneNumber.length>4) {
            contact = getPhoneContact(phoneNumber, false)
            // si no hay resultado, son 8 digitos y no inicia con 505, buscar agregando el 505
            /*contact?.let {
            Log.e("EDER", "Contacto encontrado")
            Log.e("EDER", it.name)
        }*/
            if (contact == null && phoneNumber.length == 8 && !phoneNumber.startsWith("+505")) {
                contact = getPhoneContact(phoneNumber, true)
            } else if (contact == null && phoneNumber.length > 8 && phoneNumber.startsWith("+505")) {
                //Si no hay resultado, son mas de 8 digitosm e inicia con 505, buscar sin el 505
                contact = getPhoneContact(phoneNumber.substring(4, phoneNumber.length), false)
            }        //regresar el resultaso o el mismo numero formateado
        }
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
                    prevNumber = contact.number
                    if(!numberExists(contactList, contact.number))
                        contactList.add(contact)
                }
            }
            contactCursor.close()
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
            contactCursor.close()
        }
        return@withContext contactList
    }

    //Verificar que el numero exista en la lista
    private fun numberExists(numbers:List<Contact>, number:String):Boolean{
        var itExists = false
        numbers.forEach {
            if (it.number.clearPhoneString() == number.clearPhoneString()) {
                itExists = true
            }
        }
        if (!itExists && number.length==8 && !number.startsWith("+505")){
            numbers.forEach {
                if (it.number.clearPhoneString() == "+505${number.clearPhoneString()}") {
                    itExists = true
                }
            }
        }else if(!itExists && number.length>8 && number.startsWith("+505")){
            //Si no hay resultado, son mas de 8 digitosm e inicia con 505, buscar sin el 505
            val tmpNumber = number.clearPhoneString()
            numbers.forEach {
                if (it.number.clearPhoneString() == tmpNumber.substring(4, tmpNumber.length)) {
                    itExists = true
                }
            }
        }
        return itExists
    }


    suspend fun getCallLog():List<CallsLog> = withContext(Dispatchers.IO){
        Log.e("EDER", "getCallLog")
        val calls: MutableList<CallsLog> =  ArrayList()
        val repoOperator = RepoOperator.getInstance(context)
        val PROJECTION = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
        )
        val limit = Prefs.getInt("call_log_limit", 100)

        val cr = context.contentResolver
        val strOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"
        val callUri = Uri.parse("content://call_log/calls")
        val cur = cr.query(callUri, PROJECTION, null, null, strOrder)
        // loop through cursor
        //var temp_id = -1
        while (cur != null && cur.moveToNext()) {
            val callsLog = CallsLog()
            callsLog.id = Integer.valueOf(cur.getString(cur.getColumnIndex(CallLog.Calls._ID)))
            callsLog.number = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)).clearPhoneString()
            val callDate = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE))
            callsLog.callDate = Calendar.getInstance().apply { timeInMillis = callDate }
            callsLog.type = cur.getInt(cur.getColumnIndex(CallLog.Calls.TYPE))
            callsLog.duration = cur.getInt(cur.getColumnIndex(CallLog.Calls.DURATION))
            callsLog.name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME))?: callsLog.number.toPhoneFormat()
            callsLog.operator = repoOperator.getOperator(callsLog.number)
            //Log.e("EDER", "${callsLog.number} ${callsLog.type} ${callsLog.operator?.operator?.getOperatorString()}")
            calls.add(callsLog)
        }//Fin de While
        cur?.close()
        //Log.e("EDER", "Fin Soncro LLAMADAS")
        return@withContext calls
    }


}