package com.nairobisim.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nairobisim.model.*
import java.lang.reflect.Type
import java.util.*

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time
    
    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }
    
    @TypeConverter
    fun fromBusinessCategory(value: BusinessCategory?): String? = value?.name
    
    @TypeConverter
    fun toBusinessCategory(value: String?): BusinessCategory? = value?.let { BusinessCategory.valueOf(it) }
    
    @TypeConverter
    fun fromNairobiArea(value: NairobiArea?): String? = value?.name
    
    @TypeConverter
    fun toNairobiArea(value: String?): NairobiArea? = value?.let { NairobiArea.valueOf(it) }
    
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name
    
    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }
    
    @TypeConverter
    fun fromLoanSource(value: LoanSource?): String? = value?.name
    
    @TypeConverter
    fun toLoanSource(value: String?): LoanSource? = value?.let { LoanSource.valueOf(it) }
    
    @TypeConverter
    fun fromEmployeeRole(value: EmployeeRole?): String? = value?.name
    
    @TypeConverter
    fun toEmployeeRole(value: String?): EmployeeRole? = value?.let { EmployeeRole.valueOf(it) }
    
    @TypeConverter
    fun fromAssetCategory(value: AssetCategory?): String? = value?.name
    
    @TypeConverter
    fun toAssetCategory(value: String?): AssetCategory? = value?.let { AssetCategory.valueOf(it) }
    
    @TypeConverter
    fun fromEventType(value: EventType?): String? = value?.name
    
    @TypeConverter
    fun toEventType(value: String?): EventType? = value?.let { EventType.valueOf(it) }
    
    @TypeConverter
    fun fromChamaFrequency(value: ChamaFrequency?): String? = value?.name
    
    @TypeConverter
    fun toChamaFrequency(value: String?): ChamaFrequency? = value?.let { ChamaFrequency.valueOf(it) }
    
    @TypeConverter
    fun fromGameSpeed(value: GameSpeed?): String? = value?.name
    
    @TypeConverter
    fun toGameSpeed(value: String?): GameSpeed? = value?.let { GameSpeed.valueOf(it) }
    
    @TypeConverter
    fun fromEmployeeList(value: List<Employee>?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toEmployeeList(value: String?): List<Employee>? = value?.let {
        val type = object : TypeToken<List<Employee>>() {}.type
        gson.fromJson(it, type)
    }
    
    @TypeConverter
    fun fromBusinessAssetList(value: List<BusinessAsset>?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toBusinessAssetList(value: String?): List<BusinessAsset>? = value?.let {
        val type = object : TypeToken<List<BusinessAsset>>() {}.type
        gson.fromJson(it, type)
    }
    
    @TypeConverter
    fun fromChamaMemberList(value: List<ChamaMember>?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toChamaMemberList(value: String?): List<ChamaMember>? = value?.let {
        val type = object : TypeToken<List<ChamaMember>>() {}.type
        gson.fromJson(it, type)
    }
    
    @TypeConverter
    fun fromBusinessCategoryList(value: List<BusinessCategory>?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toBusinessCategoryList(value: String?): List<BusinessCategory>? = value?.let {
        val type = object : TypeToken<List<BusinessCategory>>() {}.type
        gson.fromJson(it, type)
    }
    
    @TypeConverter
    fun fromNairobiAreaList(value: List<NairobiArea>?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toNairobiAreaList(value: String?): List<NairobiArea>? = value?.let {
        val type = object : TypeToken<List<NairobiArea>>() {}.type
        gson.fromJson(it, type)
    }
    
    @TypeConverter
    fun fromUnlockRequirements(value: UnlockRequirements?): String? = value?.let { gson.toJson(it) }
    
    @TypeConverter
    fun toUnlockRequirements(value: String?): UnlockRequirements? = value?.let {
        gson.fromJson(it, UnlockRequirements::class.java)
    }
}