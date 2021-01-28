package com.edxavier.cerberus_sms.data.daos

import androidx.room.*
import com.edxavier.cerberus_sms.data.models.Operator

@Dao
interface OperatorDao {

    @Insert
    fun save(operatorList: List<Operator>)

    @Query("DELETE FROM operator")
    fun delete()

    @Query("SELECT * FROM operator WHERE areaCode=:area AND countryCode=:country order by id asc")
    fun getOperator(country:String, area:String): Operator?

    @Query("SELECT * FROM operator WHERE countryCode=:country order by id asc limit 1")
    fun getOperatorByCountry(country:String): Operator?

    @Query("SELECT * FROM operator WHERE  countryCode=:country and areaCode like :area || '%' order by id asc ")
    fun getOperator2(country:String, area:String): Operator?

    @Query("SELECT * FROM operator WHERE countryCode like :code || '%' order by id asc limit 1")
    fun getOperatorsCountryCodeStartsWith(code:String): List<Operator>

    @Query("SELECT count(id) FROM operator")
    fun countOperators(): Int
}