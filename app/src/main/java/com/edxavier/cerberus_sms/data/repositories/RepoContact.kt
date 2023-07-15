package com.edxavier.cerberus_sms.data.repositories

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.BlockedNumberContract.BlockedNumbers
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Contacts.*
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.data.models.CallsLog
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.helpers.clearPhoneString
import com.edxavier.cerberus_sms.helpers.getCallSim
import com.edxavier.cerberus_sms.helpers.toPhoneFormat
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class RepoContact(var mContext: Context) {
    private val selectFields = arrayOf(
        CallLog.Calls._ID,
        CallLog.Calls.NUMBER,
        CallLog.Calls.CACHED_FORMATTED_NUMBER,
        CallLog.Calls.CACHED_NAME,
        CallLog.Calls.CACHED_PHOTO_URI,
        CallLog.Calls.TYPE,
        CallLog.Calls.DURATION,
        CallLog.Calls.DATE,
        CallLog.Calls.PHONE_ACCOUNT_ID,
    )
    private val contentResolver: ContentResolver = mContext.contentResolver

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: RepoContact? = null
        fun getInstance(ctxt: Context) =
                instance ?: synchronized(this) {
                    instance ?: RepoContact(ctxt).also { instance = it }
                }
    }

    fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun getPhoneContact(phoneNumber: String):Contact{
        //buscar tal como se recibe
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
        return contact ?: Contact(name = phoneNumber.toPhoneFormat(), number = phoneNumber.toPhoneFormat())
    }

    private fun getPhoneContact(phoneNumber: String, includeNicCode: Boolean = false):Contact? {
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
            return if (contactCursor.count>0)
                Contact(name = name, photo = photo)
            else
                null
        }else
            return null
    }

    private fun isWhatsappNumber(raw_contact_id:String): Boolean {
        val c = contentResolver.query(
            RawContacts.CONTENT_URI,
            arrayOf(RawContacts.CONTACT_ID, RawContacts.ACCOUNT_TYPE, RawContacts._ID),
            "${RawContacts._ID}=?",
            arrayOf(raw_contact_id),
            null
        )
        var isWhatsappNumber = false
        c?.let {
            while (c.moveToNext()) {
                if(c.getString(1)=="com.whatsapp") {
                    isWhatsappNumber = true
                    break
                }
            }
        }
        c?.close()
        return isWhatsappNumber
    }
    suspend fun getContactNumbers(id:Int = -1, searchText: String = ""):List<Contact> = withContext(Dispatchers.IO){

        val contactList: MutableList<Contact> =  ArrayList()
        val repoOperator = RepoOperator.getInstance(mContext)
        val projection = arrayOf(
            Data.CONTACT_ID,
            Phone.NUMBER,
            Data.MIMETYPE,
            Data.RAW_CONTACT_ID,
            Data._ID,
            Data.DISPLAY_NAME,
            Data.PHOTO_URI
        )
        val filter = if(id>-1) {
          "$HAS_PHONE_NUMBER=? AND " +
                    "${Data.CONTACT_ID}=? AND ${Data.MIMETYPE}=?"
        }else{
            "$HAS_PHONE_NUMBER=? AND " +
                    "${Phone.NUMBER} LIKE ? AND ${Data.MIMETYPE}=?"
        }
        val args = if(id>-1){
            arrayOf("1", id.toString(), Phone.CONTENT_ITEM_TYPE)
        }else{
            arrayOf("1", "$searchText%", Phone.CONTENT_ITEM_TYPE)
        }
        val contactCursor = contentResolver.query(
            Data.CONTENT_URI,
            projection, filter, args,
            "${Phone.NUMBER} LIMIT 20"
        )
        val contacts: MutableList<Contact> = ArrayList()
        if (contactCursor!=null){
            while (contactCursor.moveToNext()){
                val contact = Contact()
                contact.id = contactCursor.getString(0).toInt()
                contact.number = contactCursor.getString(1)
                contact.whatsapp = isWhatsappNumber(contactCursor.getString(3))
                contact.name = contactCursor.getString(5)?:""
                contact.photo = contactCursor.getString(6)?:""
                contact.info = "${contact.whatsapp}"
                // Log.e("EDER", contact.info)
                contact.operator = repoOperator.getOperator(contact.number)
                // contact.operator = repoOperator.getOperator(contact.number)
                contactList.add(contact)
            }
            contactCursor.close()
            val group = contactList.groupBy{ it.number.clearPhoneString().toPhoneFormat() }
            group.forEach {
                var hasWhatsapp = false
                it.value.forEach { c->
                    if(c.whatsapp){
                        hasWhatsapp = true
                    }
                }
                val c = it.value[0]
                c.whatsapp = hasWhatsapp
                contacts.add(c)
            }
        }
        return@withContext contacts
    }

    suspend fun getContacts(
        searchText: String = "",
        justFavorites: Boolean = false
    ):List<Contact> = withContext(Dispatchers.IO){
        val contactList: MutableList<Contact> =  ArrayList()
        val projection = arrayOf(
            _ID,
            DISPLAY_NAME,
            HAS_PHONE_NUMBER,
            PHOTO_URI,
            STARRED,
            LOOKUP_KEY
        )
        val filterSelection = if(justFavorites)
            "$HAS_PHONE_NUMBER=? AND $STARRED=?"
        else
            "$HAS_PHONE_NUMBER=?"
        val args = mutableListOf("1")
        if(justFavorites){ args.add("1")}

        val dataUri = if(searchText.isNotEmpty())
            Uri.withAppendedPath(CONTENT_FILTER_URI, Uri.encode(searchText))
        else
            CONTENT_URI

        val cursor = contentResolver.query(
            dataUri,
            projection,
            filterSelection,
            args.toTypedArray(),
            Phone.DISPLAY_NAME
        )

        if (cursor!=null){
            while (cursor.moveToNext()){
                val contact = Contact()
                contact.id = cursor.getString(0).toInt()
                contact.name = cursor.getString(1)
                contact.photo = cursor.getString(3) ?:""
                contact.favorite = cursor.getInt(4) == 1
                contact.lookupKey = cursor.getString(5)?:""
//                Log.e("EDER", "${cursor.getString(4) ?:""} / ${cursor.getString(5) ?:""} /" +
//                        "${cursor.getLong(6)} / ${cursor.getLong(7)} / ${cursor.getLong(8)}")
                contactList.add(contact)
            }
            cursor.close()
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


    suspend fun getCallLog(searchText:String=""):List<CallsLog> = withContext(Dispatchers.IO){
        val calls: MutableList<CallsLog> =  ArrayList()
        val repoOperator = RepoOperator.getInstance(mContext)

        val limit = if(searchText.isEmpty())
            Prefs.getInt("call_log_limit", 300)
        else
            Prefs.getInt("call_log_limit", 10)
        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"
        val callUri = CallLog.Calls.CONTENT_URI
        val selectionFilter = if (searchText.isEmpty())
            "${CallLog.Calls._ID} != ?"
        else
            "${CallLog.Calls.NUMBER} LIKE ?"
        val selectionArgs = if(searchText.isEmpty()) arrayOf("0") else arrayOf("$searchText%")

        val cursor = contentResolver.query(
            callUri,
            selectFields,
            selectionFilter,
            selectionArgs,
            sortOrder
        )
        // loop through cursor
        while (cursor != null && cursor.moveToNext()) {
            val callLog = CallsLog()
            callLog.id = Integer.valueOf(cursor.getString(0))
            callLog.number = cursor.getString(1)?:""
            callLog.formatted_number = cursor.getString(2)?:""
            callLog.name = cursor.getString(3)?:""
            if(callLog.name.isEmpty() && callLog.formatted_number.isNotEmpty()){
                callLog.name = callLog.formatted_number
            }else if(callLog.name.isEmpty() && callLog.number.isNotEmpty()){
                callLog.name = callLog.number.toPhoneFormat()
            }

            callLog.photoUri = cursor.getString(4)?:""
            callLog.type = cursor.getInt(5)
            callLog.duration = cursor.getInt(6)
            val callDate = cursor.getLong(7)
            callLog.callDate = Calendar.getInstance().apply { timeInMillis = callDate }
            callLog.account_id = cursor.getString(8)?:""
            calls.add(callLog)
        }//Fin de While
        cursor?.close()
        val callsGrouped: MutableList<CallsLog> =  ArrayList()
        val gby = calls.groupBy { it.number }
        gby.forEach {
            val call = it.value[0]
            call.operator = repoOperator.getOperator(call.number.clearPhoneString())
            call.total = it.value.size
            call.isBlocked = BlockedNumberContract.isBlocked(mContext, call.number)
            val sim = mContext.getCallSim(call.account_id)
            call.sim = sim.slot
            if(sim.slot == -1 && call.account_id == "0")
                call.sim = 1
            if(sim.slot == -1 && call.account_id == "1")
                call.sim = 2
            call.carrier = sim.carrier
            callsGrouped.add(call)
        }
        return@withContext callsGrouped
    }
    suspend fun getCallLogFor(number: String):List<CallsLog> = withContext(Dispatchers.IO){
        val calls: MutableList<CallsLog> =  ArrayList()
        val limit = Prefs.getInt("call_log_limit", 300)

        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"
        val callUri = CallLog.Calls.CONTENT_URI
        val selectionFilter = "${CallLog.Calls.NUMBER} == ?"
        val selectionArgs = arrayOf(number)

        val cursor = contentResolver.query(
            callUri,
            selectFields,
            selectionFilter,
            selectionArgs,
            sortOrder
        )
        // loop through cursor
        while (cursor != null && cursor.moveToNext()) {
            val callLog = CallsLog()
            callLog.id = Integer.valueOf(cursor.getString(0))
            callLog.number = cursor.getString(1)?:""
            callLog.formatted_number = cursor.getString(2)?:""
            callLog.name = cursor.getString(3)?:""
            if(callLog.name.isEmpty() && callLog.formatted_number.isNotEmpty()){
                callLog.name = callLog.formatted_number
            }else if(callLog.name.isEmpty() && callLog.number.isNotEmpty()){
                callLog.name = callLog.number.toPhoneFormat()
            }

            callLog.photoUri = cursor.getString(4)?:""
            callLog.type = cursor.getInt(5)
            callLog.duration = cursor.getInt(6)
            val callDate = cursor.getLong(7)
            callLog.callDate = Calendar.getInstance().apply { timeInMillis = callDate }
            callLog.account_id = cursor.getString(8)?:""
            val sim = mContext.getCallSim(callLog.account_id)
            callLog.sim = sim.slot
            if(sim.slot == -1 && callLog.account_id == "0")
                callLog.sim = 1
            if(sim.slot == -1 && callLog.account_id == "1")
                callLog.sim = 2
            calls.add(callLog)
        }//Fin de While
        cursor?.close()
        return@withContext calls
    }

    fun deleteNumberCallLog(number: String? = null){
        val callUri = CallLog.Calls.CONTENT_URI
        val selectionFilter = "${CallLog.Calls.NUMBER} == ?"
        if (number.isNullOrBlank()){
            contentResolver.delete(
                callUri, null, null
            )
        }else{
            val selectionArgs = arrayOf(number)
            contentResolver.delete(
                callUri, selectionFilter, selectionArgs
            )
        }

    }
    fun blockNumber(number: String){
        val values = ContentValues()
        values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        contentResolver.insert(BlockedNumbers.CONTENT_URI, values)
    }

    fun setContactAsFav(fav: Boolean, contactId:Int){
        val v = ContentValues()
        val starred = if(fav) 1 else 0
        v.put(STARRED,starred)
        contentResolver.update(
            CONTENT_URI,
            v, "$_ID=?",
            arrayOf(contactId.toString())
        )
    }
}