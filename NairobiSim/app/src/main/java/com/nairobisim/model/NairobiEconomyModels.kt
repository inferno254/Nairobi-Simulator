package com.nairobisim.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Embedded
import androidx.room.PrimaryKey
import com.nairobisim.data.converters.Converters
import androidx.room.TypeConverters
import java.util.*

// ==================== NAIROBI INCOME CLASS SYSTEM (KNBS-based) ====================

enum class NairobiIncomeClass(
    val displayName: String,
    val nickname: String,
    val rangeDescription: String,
    val minMonthly: Long,
    val maxMonthly: Long,
    val color: String
) {
    LOW("Low Income", "Struggling", "Below KES 46,355/month", 0L, 46355L, "#C62828"),
    MIDDLE("Middle Income", "Getting By", "KES 46,356 - 184,394/month", 46356L, 184394L, "#1565C0"),
    UPPER("Upper Income", "Wealthy", "Above KES 184,395/month", 184395L, Long.MAX_VALUE, "#1A8A3A");

    companion object {
        fun fromMonthlyIncome(income: Long): NairobiIncomeClass {
            return values().firstOrNull { income >= it.minMonthly && income <= it.maxMonthly }
                ?: UPPER
        }
    }
}

// ==================== RANK CLIMBING SYSTEM ====================

enum class NairobiRank(
    val displayName: String,
    val nickname: String,
    val minNetWorth: Long,
    val description: String,
    val color: String,
    val icon: String
) {
    STREET_HUSTLER("Street Hustler", "Mjinga wa Mtaa", 0L, "Starting from zero. Every shilling counts.", "#757575", "🏃"),
    MARKET_HUSTLER("Market Hustler", "Mjinga wa Marikiti", 50000L, "Selling by the roadside. Learning the grind.", "#4CAF50", "🛒"),
    KIOSK_OWNER("Kiosk Owner", "Mwenye Kiosk", 200000L, "Own a small shop. Regular customers.", "#2E7D32", "🏪"),
    MATATU_CONDUCTOR("Matatu Conductor", "Makanga", 500000L, "Working the routes. Know every stage.", "#FB8C00", "🚌"),
    BODA_BOSS("Boda Boss", "Boda King", 1000000L, "Fleet of motorbikes. Territory control.", "#EF6C00", "🛵"),
    ESTATE_LANDLORD("Estate Landlord", "Mwenye Nyumba", 2500000L, "Collecting rent from tenants.", "#6A1B9A", "🏢"),
    BUSINESS_MOGUL("Business Mogul", "Bigman", 10000000L, "Multiple businesses. Employees working for you.", "#1565C0", "💼"),
    TYCOON("Tycoon", "Mfalme wa Bisnes", 50000000L, "Empire builder. The city bends to you.", "#1A8A3A", "👑"),
    NAIROBI_GODFATHER("Nairobi Godfather", "Mfalme Mkuu", 100000000L, "Owns half the city. Sharp boys work for you.", "#4A148C", "👑");

    companion object {
        fun fromNetWorth(netWorth: Long): NairobiRank {
            return values().reversed().firstOrNull { netWorth >= it.minNetWorth }
                ?: STREET_HUSTLER
        }
    }
}

// ==================== COST OF LIVING (2024 Nairobi data) ====================

data class CostOfLivingProfile(
    val id: String = "cost_of_living",
    val housingCost: Long = 0,
    val foodCost: Long = 0,
    val transportCost: Long = 0,
    val utilitiesCost: Long = 0,
    val healthcareCost: Long = 0,
    val miscellaneousCost: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalMonthly: Long
        get() = housingCost + foodCost + transportCost + utilitiesCost + healthcareCost + miscellaneousCost

    fun isAffordable(monthlyIncome: Long): Boolean = monthlyIncome >= totalMonthly

    val deficit: Long
        get() = (totalMonthly - monthlyIncome).coerceAtLeast(0)

    val affordabilityRatio: Double
        get() = if (totalMonthly > 0) monthlyIncome.toDouble() / totalMonthly else 0.0

    private var monthlyIncome: Long = 0
    fun setIncome(income: Long) { monthlyIncome = income }
}

// Default Nairobi cost profiles by area and income class
val NAIROBI_COST_PROFILES = mapOf(
    "low_kibera" to CostOfLivingProfile(
        housingCost = 8000,
        foodCost = 9000,
        transportCost = 3000,
        utilitiesCost = 1500,
        healthcareCost = 1000,
        miscellaneousCost = 2000
    ),
    "middle_kilimani" to CostOfLivingProfile(
        housingCost = 35000,
        foodCost = 22000,
        transportCost = 8000,
        utilitiesCost = 8500,
        healthcareCost = 3000,
        miscellaneousCost = 8000
    ),
    "upper_karen" to CostOfLivingProfile(
        housingCost = 80000,
        foodCost = 55000,
        transportCost = 25000,
        utilitiesCost = 20000,
        healthcareCost = 10000,
        miscellaneousCost = 30000
    )
)

// ==================== INFLATION ENGINE ====================

data class InflationSnapshot(
    val date: Date = Date(),
    val overallInflation: Double = 4.31,
    val foodInflation: Double = 7.87,
    val fuelInflation: Double = 14.32,
    val housingInflation: Double = 6.9,
    val transportInflation: Double = 12.2,
    val description: String = ""
) {
    val poorHitFactor: Double
        get() = foodInflation / overallInflation

    val richHitFactor: Double
        get() = housingInflation / overallInflation
}

// ==================== MONEY CIRCULATION ====================

data class MoneyCirculationProfile(
    val kesInMpesa: Long = 0,
    val kesInBank: Long = 0,
    val kesInSacco: Long = 0,
    val kesInChama: Long = 0,
    val kesInCash: Long = 0,
    val lastTransferDate: Date = Date()
) {
    val totalCirculating: Long
        get() = kesInMpesa + kesInBank + kesInSacco + kesInChama + kesInCash

    fun liquidCashAvailable(): Long = kesInMpesa + kesInCash

    fun needsCashFor(amount: Long): Boolean = liquidCashAvailable() < amount
}

// ==================== DAILY SURVIVAL ====================

data class DailySurvivalResult(
    val day: Int,
    val income: Long,
    val expenses: Long,
    val netSurvival: Long,
    val canAffordFood: Boolean,
    val canAffordTransport: Boolean,
    val canAffordRent: Boolean,
    val hungerLevel: Int,
    val stressLevel: Int,
    val events: List<String> = emptyList()
)

// ==================== PLAYER ECONOMIC STATE ====================

@TypeConverters(Converters::class)
@Entity(tableName = "player_economic_state")
data class PlayerEconomicState(
    @PrimaryKey val playerId: String = "player_1",
    var incomeClass: NairobiIncomeClass = NairobiIncomeClass.LOW,
    var currentRank: NairobiRank = NairobiRank.STREET_HUSTLER,
    var monthlyIncome: Long = 0,
    var dailyIncome: Long = 0,
    // Cost of living fields
    var housingCost: Long = 8000,
    var foodCost: Long = 9000,
    var transportCost: Long = 3000,
    var utilitiesCost: Long = 1500,
    var healthcareCost: Long = 1000,
    var miscellaneousCost: Long = 2000,
    // Money circulation fields
    var kesInMpesa: Long = 0,
    var kesInBank: Long = 0,
    var kesInSacco: Long = 0,
    var kesInChama: Long = 0,
    var kesInCash: Long = 0,
    // Inflation fields
    var overallInflation: Double = 4.31,
    var foodInflation: Double = 7.87,
    var fuelInflation: Double = 14.32,
    var housingInflation: Double = 6.9,
    var transportInflation: Double = 12.2,
    // Survival stats
    var hungerLevel: Int = 0,
    var stressLevel: Int = 0,
    var reputation: Int = 0,
    var streetCred: Int = 0,
    var policeRecord: Int = 0,
    var sharpBoysLosses: Long = 0,
    var bribesPaid: Long = 0,
    var loansShylock: Long = 0,
    var lastSurvivalCheck: Date = Date(),
    var totalDaysSurvived: Int = 0,
    var isBankrupt: Boolean = false
) {
    val totalMonthlyCost: Long
        get() = housingCost + foodCost + transportCost + utilitiesCost + healthcareCost + miscellaneousCost

    val totalMoneyCirculating: Long
        get() = kesInMpesa + kesInBank + kesInSacco + kesInChama + kesInCash

    fun liquidCashAvailable(): Long = kesInMpesa + kesInCash

    fun updateRank(netWorth: Long) {
        currentRank = NairobiRank.fromNetWorth(netWorth)
    }

    fun updateIncomeClass(monthlyIncome: Long) {
        this.monthlyIncome = monthlyIncome
        incomeClass = NairobiIncomeClass.fromMonthlyIncome(monthlyIncome)
    }
}

// ==================== EXPENSE MODELS ====================

data class LivingExpenseItem(
    val id: String,
    val name: String,
    val category: ExpenseCategory,
    val amount: Long,
    val frequency: ExpenseFrequency,
    val isEssential: Boolean = true,
    val nairobiContext: String = ""
)

enum class ExpenseCategory(
    val displayName: String,
    val description: String
) {
    HOUSING("Housing", "Rent, house help"),
    FOOD("Food", "Unga, sukuma wiki, meat, cooking oil"),
    TRANSPORT("Transport", "Matatu, boda boda, fuel"),
    UTILITIES("Utilities", "Electricity, water, airtime, internet"),
    HEALTHCARE("Healthcare", "Clinic, pharmacy, NHIF/SHIF"),
    EDUCATION("Education", "School fees, books"),
    SOCIAL("Social", "Church, chama, nyama choma"),
    EMERGENCY("Emergency", "Medical, bail, sharp boys loss"),
    BUSINESS("Business", "Permits, licenses, bribes");
}

enum class ExpenseFrequency(
    val displayName: String,
    val days: Int
) {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30);
}
