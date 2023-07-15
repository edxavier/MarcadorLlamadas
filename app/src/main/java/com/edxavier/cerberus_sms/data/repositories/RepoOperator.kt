package com.edxavier.cerberus_sms.data.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import com.edxavier.cerberus_sms.R
import com.edxavier.cerberus_sms.data.AppDatabase
import com.edxavier.cerberus_sms.data.models.Operator
import com.edxavier.cerberus_sms.helpers.clearPhoneString
import com.edxavier.cerberus_sms.helpers.getOperatorString
import com.edxavier.cerberus_sms.helpers.isValidPhoneNumber
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.ArrayList

class RepoOperator(private var context: Context) {
    private val dao = AppDatabase.getInstance(context).operatorDao()

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: RepoOperator? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: RepoOperator(context).also { instance = it }
            }
    }

    suspend fun initializeOperators() = withContext(Dispatchers.IO) {
        val operatorList: MutableList<Operator> =  ArrayList()
        if(dao.countOperators()==0){
            Log.e("EDER", "initializing")
            // INICIALIZAR CODIGOS US
            var inputStream: InputStream = context.resources.openRawResource(R.raw.usa_areas_codes)
            var reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            var csvReader = CSVReader(reader)
            val usAreas = csvReader.readAll()
            //val us = app.resources.getString(R.string.usa)
            val us = "Estados Unidos"
            for (usArea in usAreas)
                operatorList.add(Operator(countryCode = "+1", areaCode = usArea[0], area = usArea[1], country = us, operator = Operator.INTERNATIONAL))

            operatorList.add(Operator(countryCode = "+7", country = "Rusia", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+51", country = "Perú", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+52", country = "Mexico", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+53", country = "Cuba", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+54", country = "Argentina", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+55", country = "Brasil", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+56", country = "Chile", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+57", country = "Colombia", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+58", country = "Venezuela", operator = Operator.INTERNATIONAL))


            operatorList.add(Operator(countryCode = "+30", country = "Grecia", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+31", country = "Holanda", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+32", country = "Belgica", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+33", country = "Francia", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+34", country = "España", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+39", country = "Italia", operator = Operator.INTERNATIONAL))

            operatorList.add(Operator(countryCode = "+40", country = "Rumania", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+41", country = "Suiza", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+43", country = "Austria", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+44", country = "Inglaterra", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+45", country = "Dinamarca", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+46", country = "Suecia", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+47", country = "Noruega", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+48", country = "Polonia", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+49", country = "Alemania", operator = Operator.INTERNATIONAL))

            operatorList.add(Operator(countryCode = "+81", country = "Japón", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+82", country = "Corea del Sur", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+86", country = "China", operator = Operator.INTERNATIONAL))

            //INICIALIZAR CODIGOS OTROS PAISES
            operatorList.add(Operator(countryCode = "+501", country = "Belice", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+502", country = "Guatemala", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+503", country = "El Salvador", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+504", country = "Honduras", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+505", country = "Nicaragua", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+506", country = "Costa Rica", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+507", country = "Panama", operator = Operator.INTERNATIONAL))
            operatorList.add(Operator(countryCode = "+509", country = "Haití", operator = Operator.INTERNATIONAL))

            operatorList.add(Operator(countryCode = "+506", areaCode = "22", area = "San Jose | Heredia", country = "Costa Rica", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+506", areaCode = "24", area = "Alajuela", country = "Costa Rica", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+506", areaCode = "25", area = "Cartago", country = "Costa Rica", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+506", areaCode = "26", area = "Guanacaste | Puntarenas Norte", country = "Costa Rica", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+506", areaCode = "27", area = "Limon | Puntarenas Sur", country = "Costa Rica", operator = Operator.CONVENTIONAL))

            inputStream = context.resources.openRawResource(R.raw.area_codes)
            reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
            csvReader = CSVReader(reader)
            val otherAreas = csvReader.readAll()
            for (area in otherAreas)
                operatorList.add(Operator(countryCode = area[2].clearPhoneString(), areaCode = area[3].clearPhoneString(), area = area[1], country = area[0], operator = Operator.INTERNATIONAL))


            for (i in 4100..4299)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 4700..4772)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5010..5019)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5110..5139)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5310..5311)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5410..5499)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5510..5517)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5530..5539)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5543..5544)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5554..5579)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5582..5599)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5610..5639)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5690..5699)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5710..5718)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5810..5818)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5820..5879)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5910..5914)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))
            for (i in 5920..5989)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.CLARO))

            for (i in 4300..4399)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 3400..3534)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 4400..4475)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5020..5029)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5070..5109)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5140..5149)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5210..5299)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5312..5313)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5390..5399)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5400..5409)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5500..5509)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5518..5519)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5540..5542)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5545..5549)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5600..5609)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5640..5689)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5790..5799)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))
            for (i in 5915..5917)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.MOVISTAR))

            operatorList.add(Operator(countryCode = "+502", areaCode = "5819", country = "Guatemala", operator = Operator.MOVISTAR))

            for (i in 3000..3359)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4000..4099)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4476..4499)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4500..4699)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4773..4799)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4800..4819)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4822..4899)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 4900..4999)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5000..5009)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5030..5069)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5150..5199)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5200..5209)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5300..5309)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5314..5389)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5520..5529)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5550..5553)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5580..5581)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5700..5709)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5719..5789)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5800..5809)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5880..5899)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5900..5909)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5918..5919)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))
            for (i in 5990..5999)
                operatorList.add(Operator(countryCode = "+502", areaCode = i.toString(), country = "Guatemala", operator = Operator.TIGO))


            // -----------------------------------------------SALVADOR---------------------------------------------------

            for (i in 7080..7099)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7600..7669)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7687..7690)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7740..7759)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+503", areaCode = "7803", country = "El Salvador", operator = Operator.CLARO))
            for (i in 7805..7809)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7840..7844)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7850..7869)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7950..7969)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7985..7989)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))
            for (i in 7000..7069)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.CLARO))

            for (i in 6200..6299)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))
            operatorList.add(Operator(countryCode = "+503", areaCode = "7072", country = "El Salvador", operator = Operator.DIGICEL))
            operatorList.add(Operator(countryCode = "+503", areaCode = "7073", country = "El Salvador", operator = Operator.DIGICEL))
            operatorList.add(Operator(countryCode = "+503", areaCode = "7801", country = "El Salvador", operator = Operator.DIGICEL))
            for (i in 7300..7449)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))
            for (i in 7460..7469)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))
            for (i in 7695..7698)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))
            for (i in 7760..7779)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))

            for (i in 7970..7979)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))


            for (i in 7100..7199)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7450..7459)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7699..7719)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7780..7784)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7790..7794)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            operatorList.add(Operator(countryCode = "+503", areaCode = "7800",country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7810..7839)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7845..7849)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for (i in 7990..7999)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for( i in 6100..6199)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))
            for( i in 7691..7694)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))

            for( i in 7200..7299)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 7470..7599)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 7670..7685)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 7720..7739)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 7785..7789)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 7795..7799)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 7870..7949)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))
            for( i in 6000..6099)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))

            for( i in 7980..7983)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.INTELFON))

            operatorList.add(Operator(countryCode = "+503", areaCode = "78025",country = "El Salvador", operator = Operator.CLARO))

            operatorList.add(Operator(countryCode = "+503", areaCode = "70700",country = "El Salvador", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70703",country = "El Salvador", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70704",country = "El Salvador", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70705",country = "El Salvador", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70709",country = "El Salvador", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70710",country = "El Salvador", operator = Operator.CLARO))

            for( i in 78020..78024)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.DIGICEL))

            for( i in 76865..76869)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.MOVISTAR))

            operatorList.add(Operator(countryCode = "+503", areaCode = "70711",country = "El Salvador", operator = Operator.MOVISTAR))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70708",country = "El Salvador", operator = Operator.MOVISTAR))
            operatorList.add(Operator(countryCode = "+503", areaCode = "70706",country = "El Salvador", operator = Operator.MOVISTAR))

            operatorList.add(Operator(countryCode = "+503", areaCode = "70701",country = "El Salvador", operator = Operator.TIGO))
            for( i in 76860..76864)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.TIGO))

            for( i in 79840..79844)
                operatorList.add(Operator(countryCode = "+503", areaCode = i.toString(),country = "El Salvador", operator = Operator.INTELFON))

            operatorList.add(Operator(countryCode = "+504", areaCode = "3", country = "Honduras", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+504", areaCode = "8", country = "Honduras", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+504", areaCode = "7", country = "Honduras", operator = Operator.HONDUTEL))
            operatorList.add(Operator(countryCode = "+504", areaCode = "9", country = "Honduras", operator = Operator.TIGO))


            // NICARAGUA

            //FIJOS
            operatorList.add(Operator(countryCode ="+505", areaCode = "222", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "223", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "224", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "225", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "226", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "227", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "228", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "229", country = "Nicaragua", area = "Managua", operator = Operator.CONVENTIONAL))

            operatorList.add(Operator(countryCode = "+505", areaCode = "231", country = "Nicaragua", area ="Leon", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "234", country = "Nicaragua", area ="Chinandega", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "235", country = "Nicaragua", area ="Chinandega", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "251", country = "Nicaragua", area ="Chontales", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "252", country = "Nicaragua", area ="Masaya", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "253", country = "Nicaragua", area ="Carazo", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "254", country = "Nicaragua", area ="Boaco", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "255", country = "Nicaragua", area ="Granada", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "256", country = "Nicaragua", area ="Rivas", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "257", country = "Nicaragua", area ="RAAS", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "258", country = "Nicaragua", area ="Rio San Juan", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "271", country = "Nicaragua", area ="Esteli", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "272", country = "Nicaragua", area ="Madriz", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "273", country = "Nicaragua", area ="Nueva Segovia", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "277", country = "Nicaragua", area ="Matagalpa", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "278", country = "Nicaragua", area ="Jinotega", operator = Operator.CONVENTIONAL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "279", country = "Nicaragua", area ="RAAN", operator = Operator.CONVENTIONAL))

            //MOVIL CLARO
            operatorList.add(Operator(countryCode = "+505", areaCode = "550", country = "Nicaragua", operator = Operator.CLARO))

            for (i in 570..589)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))

            operatorList.add(Operator(countryCode = "+505", areaCode = "820", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "821", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "822", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "823", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "830", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "831", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "833", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "835", country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "836", country = "Nicaragua", operator = Operator.CLARO))

            for (i in 840..844)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "849", country = "Nicaragua", operator = Operator.CLARO))

            for (i in 850..854)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))
            for (i in 860..866)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "869", country = "Nicaragua", operator = Operator.CLARO))

            for (i in 870..874)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))
            for (i in 882..885)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))
            for (i in 890..894)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.CLARO))

            //MOVISTAR
            for (i in 750..755)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))

            for (i in 760..789)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))

            for (i in 810..819)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))

            for (i in 824..829)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "832", country = "Nicaragua", operator = Operator.TIGO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "837", country = "Nicaragua", operator = Operator.TIGO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "838", country = "Nicaragua", operator = Operator.TIGO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "839", country = "Nicaragua", operator = Operator.TIGO))
            for (i in 845..848)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))

            for (i in 855..859)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))

            operatorList.add(Operator(countryCode = "+505", areaCode = "867", country = "Nicaragua", operator = Operator.TIGO))
            operatorList.add(Operator(countryCode = "+505", areaCode = "868", country = "Nicaragua", operator = Operator.TIGO))
            for (i in 875..881)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))
            for (i in 886..889)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))
            for (i in 895..899)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.TIGO))

            operatorList.add(Operator(countryCode = "+505", areaCode = "620", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "630", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "633", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "635", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "640", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "644", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "645", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "650", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "655", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "677", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "681", country = "Nicaragua", operator = Operator.COOTEL))
            for (i in 681..690)
                operatorList.add(Operator(countryCode = "+505", areaCode = i.toString(), country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "695", country = "Nicaragua", operator = Operator.COOTEL))
            operatorList.add(Operator(countryCode = "+505", areaCode = "699", country = "Nicaragua", operator = Operator.COOTEL))

            dao.save(operatorList)
        }

    }

    suspend fun getOperator(number:String):Operator? = withContext(Dispatchers.IO){
        var countryCode = ""
        var areaCode = ""
        var tempNumber = number.clearPhoneString()
        // Verificar si es un numero telefonico valido
        if(tempNumber.isValidPhoneNumber()){
            //si el numero inicia con 00 Reemplazar con +
            if(tempNumber.length >= 2 && tempNumber.startsWith("00"))
                tempNumber = "+" + tempNumber.substring(2)
            // si son 8 digitos y el numero no inicia con +, poner +505 por defecto
            if(tempNumber.length in 4..8 && ! tempNumber.startsWith('+'))
                tempNumber = "+505${tempNumber}"
            countryCode = getCountryCode(tempNumber)
            areaCode = getAreaCode(tempNumber, countryCode)
            var operator:Operator? = null
            if (countryCode.isNotBlank() && areaCode.isNotBlank()){
                operator =  dao.getOperator2(countryCode, areaCode)
            }
            if (countryCode.isNotBlank() && operator==null){
                operator = dao.getOperatorByCountry(countryCode)
            }

            return@withContext operator
        }else
            return@withContext null
    }

    private fun getCountryCode(tempNumber: String):String{
        var code = ""
        var res:List<Operator> = ArrayList()
        var res2:List<Operator> = ArrayList()
        var res3:List<Operator> = ArrayList()
        // Consultar para los primeros dos digitos hasta loas primeros 4 (+5, +50, +505)
        // el resultado sera tomado en orden, si los 3 dan resultados, se tomara el ultimo como el correcto
        if (tempNumber.length>=2)
            res = dao.getOperatorsCountryCodeStartsWith(tempNumber.substring(0, 2))
        if (tempNumber.length>=3)
            res2 = dao.getOperatorsCountryCodeStartsWith(tempNumber.substring(0, 3))
        if (tempNumber.length>=4)
            res3 = dao.getOperatorsCountryCodeStartsWith(tempNumber.substring(0, 4))

        if(res.isNotEmpty())
            code = res[0].countryCode
        if(res2.isNotEmpty())
            code = res2[0].countryCode
        if(res3.isNotEmpty())
            code = res3[0].countryCode
        return code
    }
    private fun getAreaCode(tempNumber: String, countryCode:String):String {
        var code = ""
        if (countryCode=="+1" && tempNumber.length>=5)
            code = tempNumber.substring(2, 5)
        if (countryCode=="+507" || countryCode=="+501" && tempNumber.length>=5)
            code = tempNumber.substring(4, 5)
        if (countryCode=="+506" && tempNumber.length>=5) {
            if (tempNumber.substring(4, 5) == "2")
                code = tempNumber.substring(4, 6)
        }
        if (countryCode=="+504" && tempNumber.length>=5) {
            if ((tempNumber.substring(4, 5) == "3" || tempNumber.substring(4, 5) == "8" || tempNumber.substring(4, 5) == "7" || tempNumber.substring(4, 5) == "9"))
                code = tempNumber.substring(4, 5)
        }
        if (countryCode=="+502" && tempNumber.length>=8)
            code = tempNumber.substring(4, 8)
        if (countryCode=="+503") {
            if (tempNumber.length>=9 && (tempNumber.substring(4,8)=="7802" || tempNumber.substring(4,8)=="7070" || tempNumber.substring(4,8)=="7071")
                    || tempNumber.substring(4,8)=="7686"  || tempNumber.substring(4,8)=="7984")
                code = tempNumber.substring(4, 9)
            else if (tempNumber.length>=8){
                code = tempNumber.substring(4, 8)
            }
        }
        if (countryCode=="+505" && tempNumber.length>8)
            code = tempNumber.substring(4, 7)
        if (countryCode=="+52"){
            if (tempNumber.length == 5)
                code = tempNumber.substring(3, 5)
            else if (tempNumber.length >= 6)
                code = if (tempNumber.substring(3, 5) == "55" || tempNumber.substring(3, 5) == "81") {
                    tempNumber.substring(3, 5)
                } else
                    tempNumber.substring(3, 6)
        }
        if(countryCode == "+53"){//Cuba
            if (tempNumber.length == 4 || tempNumber.substring(3, 4) == "7")
                code = tempNumber.substring(3, 4)
            if (tempNumber.length >= 6 && (tempNumber.substring(3, 6) == "226" || tempNumber.substring(3, 6) == "322" || tempNumber.substring(3, 6) == "432"))
                code = tempNumber.substring(3, 6)
            else if (tempNumber.length >=5 && tempNumber.substring(3, 4) != "7")
                code = tempNumber.substring(3, 5)
        }
        if(countryCode == "+57"){//Colombia
            if(tempNumber.length>=4)
                code = tempNumber.substring(3, 4)
        }
        if(countryCode == "+51"){//Peru
            if (tempNumber.length == 4 || tempNumber.substring(3, 4) == "1")
                code = tempNumber.substring(3, 4)
            else if (tempNumber.length >=5 && tempNumber.substring(3, 4) != "1")
                code = tempNumber.substring(3, 5)
        }
        if(countryCode == "+56"){//Chile
            if (tempNumber.length == 4 || tempNumber.substring(3, 4) == "2")
                code = tempNumber.substring(3, 4)
            else if (tempNumber.length >=5 && tempNumber.substring(3, 4) != "2")
                code = tempNumber.substring(3, 5)
        }
        if(countryCode == "+55"){//Brasil
            code = if(tempNumber.length>=5)
                tempNumber.substring(3, 5)
            else
                ""
        }
        if(countryCode == "+58"){//Venezuela
            if(tempNumber.length>=6)
                code = tempNumber.substring(3, 6)
        }
        return code
    }
}