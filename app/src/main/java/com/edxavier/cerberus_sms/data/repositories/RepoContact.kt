package com.edxavier.cerberus_sms.data.repositories

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import androidx.core.content.ContextCompat
import com.edxavier.cerberus_sms.data.AppDatabase
import com.edxavier.cerberus_sms.data.models.Contact
import com.edxavier.cerberus_sms.helpers.toPhoneFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RepoContact(private var context: Context) {

    private val dao = AppDatabase.getInstance(context).operatorDao()
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

    suspend fun getPhoneContact(phoneNumber:String):Contact = withContext(Dispatchers.IO){
        //buscar tal como se recibe
        Log.e("EDER", "getPhoneContact")
        var contact = getPhoneContact(phoneNumber, false)
        // si no hay resultado, son 8 digitos y no inicia con 505, buscar agregando el 505
        contact?.let {
            Log.e("EDER", "Contacto encontrado")
            Log.e("EDER", it.name)
        }
        if (contact==null && phoneNumber.length==8 && !phoneNumber.startsWith("+505")){
            contact = getPhoneContact(phoneNumber, true)
        }else if(contact==null && phoneNumber.length>8 && phoneNumber.startsWith("+505")){
            //Si no hay resultado, son mas de 8 digitosm e inicia con 505, buscar sin el 505
            contact = getPhoneContact(phoneNumber.substring(4, phoneNumber.length), false)
        }        //regresar el resultaso o el mismo numero formateado
        return@withContext contact ?: Contact(phoneNumber.toPhoneFormat())
    }

    private suspend fun getPhoneContact(phoneNumber:String, includeNicCode:Boolean = false):Contact? = withContext(Dispatchers.IO){
        val PROJECTION = arrayOf(ContactsContract.Contacts._ID,
                Phone.DISPLAY_NAME,
                Phone.HAS_PHONE_NUMBER,
                Phone.PHOTO_URI,
                Phone.NUMBER)

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
                Phone.DISPLAY_NAME + "")

        if (contactCursor!=null){
            var name = phoneNumber.toPhoneFormat()
            var photo = ""
            while (contactCursor.moveToNext()){
                name = contactCursor.getString(contactCursor.getColumnIndex(Phone.DISPLAY_NAME))
                //Log.e("EDER_name", name)
                photo = contactCursor.getString(contactCursor.getColumnIndex(Phone.PHOTO_URI)) ?:""
            }
            if (contactCursor.count>0)
                return@withContext Contact(name, photo)
            else
                return@withContext null
        }else
            return@withContext null
    }
}