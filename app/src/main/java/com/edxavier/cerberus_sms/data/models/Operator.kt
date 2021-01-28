package com.edxavier.cerberus_sms.data.models

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.AppDatabase
import com.edxavier.cerberus_sms.data.repositories.RepoOperator
import com.edxavier.cerberus_sms.helpers.clearPhoneString
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.ArrayList

@Entity(tableName = "operator")
data class Operator(
    @PrimaryKey(autoGenerate = true)
    var id:Long? = null,
    var countryCode: String = "",
    var areaCode: String = "",
    var area: String = "",
    var country: String = "",
    var operator: Int = UNKNOWN
){
    companion object{
        val UNKNOWN = 0
        val CLARO: Int = 1
        val MOVISTAR: Int = 2
        val COOTEL: Int = 3
        val CONVENTIONAL: Int = 4
        val INTERNATIONAL: Int = 5
        val KOLBI: Int = 6
        val HONDUTEL: Int = 7
        val TIGO: Int = 8
        val DIGICEL: Int = 9
        val INTELFON: Int = 10

        suspend fun initializeOperators(context: Context) {
            val repo = RepoOperator.getInstance(context)
            repo.initializeOperators()
        }
    }
}
