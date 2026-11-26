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

    @TypeConverter
    fun fromChamaList(value: List<Chama>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toChamaList(value: String?): List<Chama>? = value?.let {
        val type = object : TypeToken<List<Chama>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let {
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromTradeType(value: TradeType?): String? = value?.name

    @TypeConverter
    fun toTradeType(value: String?): TradeType? = value?.let { TradeType.valueOf(it) }

    @TypeConverter
    fun fromShareHoldingList(value: List<ShareHolding>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toShareHoldingList(value: String?): List<ShareHolding>? = value?.let {
        val type = object : TypeToken<List<ShareHolding>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromOwnedCarList(value: List<OwnedCar>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toOwnedCarList(value: String?): List<OwnedCar>? = value?.let {
        val type = object : TypeToken<List<OwnedCar>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromBribeEncounterList(value: List<BribeEncounter>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toBribeEncounterList(value: String?): List<BribeEncounter>? = value?.let {
        val type = object : TypeToken<List<BribeEncounter>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromCurrencyPairList(value: List<CurrencyPair>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toCurrencyPairList(value: String?): List<CurrencyPair>? = value?.let {
        val type = object : TypeToken<List<CurrencyPair>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromMarketRate(value: MarketRate?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toMarketRate(value: String?): MarketRate? = value?.let {
        gson.fromJson(it, MarketRate::class.java)
    }

    @TypeConverter
    fun fromFreelanceSkill(value: FreelanceSkill?): String? = value?.name

    @TypeConverter
    fun toFreelanceSkill(value: String?): FreelanceSkill? = value?.let { FreelanceSkill.valueOf(it) }

    @TypeConverter
    fun fromFreelanceLevel(value: FreelanceLevel?): String? = value?.name

    @TypeConverter
    fun toFreelanceLevel(value: String?): FreelanceLevel? = value?.let { FreelanceLevel.valueOf(it) }

    @TypeConverter
    fun fromFreelanceJobStatus(value: FreelanceJobStatus?): String? = value?.name

    @TypeConverter
    fun toFreelanceJobStatus(value: String?): FreelanceJobStatus? = value?.let { FreelanceJobStatus.valueOf(it) }

    @TypeConverter
    fun fromLifePath(value: LifePath?): String? = value?.name

    @TypeConverter
    fun toLifePath(value: String?): LifePath? = value?.let { LifePath.valueOf(it) }

    @TypeConverter
    fun fromCounselingOutcome(value: CounselingOutcome?): String? = value?.name

    @TypeConverter
    fun toCounselingOutcome(value: String?): CounselingOutcome? = value?.let { CounselingOutcome.valueOf(it) }

    @TypeConverter
    fun fromLifePathList(value: List<LifePath>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toLifePathList(value: String?): List<LifePath>? = value?.let {
        val type = object : TypeToken<List<LifePath>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromPersonPsychology(value: PersonPsychology?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toPersonPsychology(value: String?): PersonPsychology? = value?.let {
        val type = object : TypeToken<PersonPsychology>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromLifeBarrierList(value: List<LifeBarrier>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toLifeBarrierList(value: String?): List<LifeBarrier>? = value?.let {
        val type = object : TypeToken<List<LifeBarrier>>() {}.type
        gson.fromJson(it, type)
    }

    @TypeConverter
    fun fromBarrierType(value: BarrierType?): String? = value?.name

    @TypeConverter
    fun toBarrierType(value: String?): BarrierType? = value?.let { BarrierType.valueOf(it) }

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus?): String? = value?.name

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus? = value?.let { SessionStatus.valueOf(it) }

    @TypeConverter
    fun fromPersuasionApproach(value: PersuasionApproach?): String? = value?.name

    @TypeConverter
    fun toPersuasionApproach(value: String?): PersuasionApproach? = value?.let { PersuasionApproach.valueOf(it) }

    @TypeConverter
    fun fromPersuasionApproachList(value: List<PersuasionApproach>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toPersuasionApproachList(value: String?): List<PersuasionApproach>? = value?.let {
        val type = object : TypeToken<List<PersuasionApproach>>() {}.type
        gson.fromJson(it, type)
    }

    // Betting converters
    @TypeConverter
    fun fromSportType(value: SportType?): String? = value?.name

    @TypeConverter
    fun toSportType(value: String?): SportType? = value?.let { SportType.valueOf(it) }

    @TypeConverter
    fun fromBetType(value: BetType?): String? = value?.name

    @TypeConverter
    fun toBetType(value: String?): BetType? = value?.let { BetType.valueOf(it) }

    @TypeConverter
    fun fromBetStatus(value: BetStatus?): String? = value?.name

    @TypeConverter
    fun toBetStatus(value: String?): BetStatus? = value?.let { BetStatus.valueOf(it) }

    // Ponzi converters
    @TypeConverter
    fun fromPonziSchemeStatus(value: PonziSchemeStatus?): String? = value?.name

    @TypeConverter
    fun toPonziSchemeStatus(value: String?): PonziSchemeStatus? = value?.let { PonziSchemeStatus.valueOf(it) }

    @TypeConverter
    fun fromPonziSchemeType(value: PonziSchemeType?): String? = value?.name

    @TypeConverter
    fun toPonziSchemeType(value: String?): PonziSchemeType? = value?.let { PonziSchemeType.valueOf(it) }

    // Mobile Money converters
    @TypeConverter
    fun fromMobileMoneyProvider(value: MobileMoneyProvider?): String? = value?.name

    @TypeConverter
    fun toMobileMoneyProvider(value: String?): MobileMoneyProvider? = value?.let { MobileMoneyProvider.valueOf(it) }

    @TypeConverter
    fun fromMobileMoneyTransactionType(value: MobileMoneyTransactionType?): String? = value?.name

    @TypeConverter
    fun toMobileMoneyTransactionType(value: String?): MobileMoneyTransactionType? = value?.let { MobileMoneyTransactionType.valueOf(it) }

    @TypeConverter
    fun fromMobileMoneyTransactionStatus(value: MobileMoneyTransactionStatus?): String? = value?.name

    @TypeConverter
    fun toMobileMoneyTransactionStatus(value: String?): MobileMoneyTransactionStatus? = value?.let { MobileMoneyTransactionStatus.valueOf(it) }

    // Bank converters
    @TypeConverter
    fun fromKenyanBank(value: KenyanBank?): String? = value?.name

    @TypeConverter
    fun toKenyanBank(value: String?): KenyanBank? = value?.let { KenyanBank.valueOf(it) }

    @TypeConverter
    fun fromBankAccountType(value: BankAccountType?): String? = value?.name

    @TypeConverter
    fun toBankAccountType(value: String?): BankAccountType? = value?.let { BankAccountType.valueOf(it) }

    @TypeConverter
    fun fromBankTransactionType(value: BankTransactionType?): String? = value?.name

    @TypeConverter
    fun toBankTransactionType(value: String?): BankTransactionType? = value?.let { BankTransactionType.valueOf(it) }

    @TypeConverter
    fun fromBankTransactionStatus(value: BankTransactionStatus?): String? = value?.name

    @TypeConverter
    fun toBankTransactionStatus(value: String?): BankTransactionStatus? = value?.let { BankTransactionStatus.valueOf(it) }

    @TypeConverter
    fun fromMonetaryPolicyStatus(value: MonetaryPolicyStatus?): String? = value?.name

    @TypeConverter
    fun toMonetaryPolicyStatus(value: String?): MonetaryPolicyStatus? = value?.let { MonetaryPolicyStatus.valueOf(it) }

    @TypeConverter
    fun fromSACCOType(value: SACCOType?): String? = value?.name

    @TypeConverter
    fun toSACCOType(value: String?): SACCOType? = value?.let { SACCOType.valueOf(it) }

    @TypeConverter
    fun fromMicrofinanceType(value: MicrofinanceType?): String? = value?.name

    @TypeConverter
    fun toMicrofinanceType(value: String?): MicrofinanceType? = value?.let { MicrofinanceType.valueOf(it) }

    @TypeConverter
    fun fromPlayerType(value: PlayerType?): String? = value?.name

    @TypeConverter
    fun toPlayerType(value: String?): PlayerType? = value?.let { PlayerType.valueOf(it) }
}