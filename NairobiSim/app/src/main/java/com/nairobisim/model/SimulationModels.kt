package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nairobisim.data.converters.Converters
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: String = "player_1",
    val name: String = "Nairobi Entrepreneur",
    var kesBalance: Long = 50000, // Starting capital: 50K KES
    var mpesaBalance: Long = 10000,
    var bankBalance: Long = 0,
    var saccosBalance: Long = 0,
    var cryptoWallet: Long = 0,
    var reputation: Int = 0,
    var level: Int = 1,
    var experience: Long = 0,
    var lastLogin: Date = Date(),
    var dailyStreak: Int = 0,
    var totalBusinessesOwned: Int = 0,
    var totalEmployees: Int = 0
)

@Serializable
@Entity(tableName = "business")
data class Business(
    @PrimaryKey val id: String,
    val name: String,
    val category: BusinessCategory,
    val location: NairobiArea,
    var level: Int = 1,
    var capitalInvested: Long = 0,
    var monthlyRevenue: Long = 0,
    var monthlyExpenses: Long = 0,
    var employees: List<Employee> = emptyList(),
    var assets: List<BusinessAsset> = emptyList(),
    var isOperational: Boolean = true,
    var lastCollection: Date = Date(),
    val unlockRequirements: UnlockRequirements
) {
    val netMonthlyIncome: Long
        get() = monthlyRevenue - monthlyExpenses
    
    fun calculateRevenue(): Long {
        val baseRevenue = when (category) {
            BusinessCategory.MATATU_SACCO -> 150000L * level
            BusinessCategory.KIOSK -> 80000L * level
            BusinessCategory.BUTCHERY -> 120000L * level
            BusinessCategory.GREEN_GROCERY -> 60000L * level
            BusinessCategory.HARDWARE -> 200000L * level
            BusinessCategory.AGROVET -> 180000L * level
            BusinessCategory.CYBER_CAFE -> 90000L * level
            BusinessCategory.CAR_WASH -> 70000L * level
            BusinessCategory.SALON_BARBERSHOP -> 100000L * level
            BusinessCategory.RESTAURANT -> 250000L * level
            BusinessCategory.BODABODA_FLEET -> 120000L * level
            BusinessCategory.EVENT_TENT_HIRE -> 300000L * level
            BusinessCategory.WATER_VENDING -> 50000L * level
            BusinessCategory.CHARCOAL_DEALER -> 90000L * level
            BusinessCategory.SECOND_HAND_CLOTHES -> 150000L * level
            BusinessCategory.POSHO_MILL -> 80000L * level
            BusinessCategory.DAIRY_FARM -> 400000L * level
            BusinessCategory.POULTRY_FARM -> 300000L * level
            BusinessCategory.FISH_FARM -> 350000L * level
            BusinessCategory.HORTICULTURE -> 500000L * level
        }
        
        val locationMultiplier = location.trafficMultiplier
        val employeeBonus = employees.sumOf { it.efficiencyBonus }
        val assetBonus = assets.sumOf { it.revenueBonus }
        
        return (baseRevenue * locationMultiplier * (1 + employeeBonus + assetBonus)).toLong()
    }
}

@Serializable
enum class BusinessCategory {
    MATATU_SACCO("Matatu Sacco", "Transport", 500000),
    KIOSK("Kiosk", "Retail", 50000),
    BUTCHERY("Butchery", "Food", 100000),
    GREEN_GROCERY("Green Grocery", "Food", 80000),
    HARDWARE("Hardware Store", "Construction", 300000),
    AGROVET("Agrovet", "Agriculture", 200000),
    CYBER_CAFE("Cyber Cafe", "Services", 150000),
    CAR_WASH("Car Wash", "Services", 100000),
    SALON_BARBERSHOP("Salon/Barbershop", "Services", 120000),
    RESTAURANT("Restaurant", "Food", 400000),
    BODABODA_FLEET("Bodaboda Fleet", "Transport", 250000),
    EVENT_TENT_HIRE("Event Tent Hire", "Events", 350000),
    WATER_VENDING("Water Vending", "Essential", 60000),
    CHARCOAL_DEALER("Charcoal Dealer", "Energy", 80000),
    SECOND_HAND_CLOTHES("Mitumba", "Retail", 100000),
    POSHO_MILL("Posho Mill", "Food Processing", 150000),
    DAIRY_FARM("Dairy Farm", "Agriculture", 800000),
    POULTRY_FARM("Poultry Farm", "Agriculture", 500000),
    FISH_FARM("Fish Farm", "Agriculture", 600000),
    HORTICULTURE("Horticulture", "Agriculture", 1000000)
    
    val displayName: String
    val sector: String
    val baseCapital: Long
    
    constructor(displayName: String, sector: String, baseCapital: Long) {
        this.displayName = displayName
        this.sector = sector
        this.baseCapital = baseCapital
    }
}

@Serializable
enum class NairobiArea(
    val displayName: String,
    val trafficMultiplier: Double,
    val rentMultiplier: Double,
    val competitionLevel: Int,
    val description: String
) {
    CBD("CBD", 1.5, 3.0, 9, "High traffic, expensive rent, fierce competition"),
    WESTLANDS("Westlands", 1.4, 2.5, 8, "Expat area, good spending power"),
    KILELESHWA("Kileleshwa", 1.3, 2.2, 7, "Middle class residential"),
    KAREN("Karen", 1.2, 3.5, 5, "Wealthy suburbs, high-end clients"),
    RUNDA("Runda", 1.1, 3.0, 4, "Ultra-wealthy, niche markets"),
    KASARANI("Kasarani", 1.3, 1.2, 6, "Growing area, student population"),
    EMBAKASI("Embakasi", 1.4, 1.0, 7, "High density, near airport"),
    KIBERA("Kibera", 1.6, 0.5, 8, "Massive population, informal economy"),
    MATHARE("Mathare", 1.5, 0.4, 7, "High density, essential services"),
    DANDORA("Dandora", 1.3, 0.8, 6, "Industrial/residential mix"),
    KAWANGWARE("Kawangware", 1.4, 0.9, 7, "Transit hub, busy"),
    GITHURAI("Githurai", 1.3, 0.9, 6, "Commuter town"),
    THIKA_ROAD("Thika Road", 1.2, 1.5, 6, "Corridor development"),
    NGONG_ROAD("Ngong Road", 1.2, 1.4, 6, "Mixed residential/commercial"),
    MOMBASA_ROAD("Mombasa Road", 1.1, 1.8, 5, "Industrial/logistics"),
    LANGATA("Langata", 1.1, 1.6, 5, "Near attractions, mixed"),
    RONGAI("Rongai", 1.2, 1.0, 5, "Satellite town"),
    KIKUYU("Kikuyu", 1.1, 0.9, 4, "Peri-urban"),
    LIMURU("Limuru", 1.0, 0.8, 3, "Agricultural area"),
    ONLINE("Online/Digital", 1.0, 0.2, 3, "No physical location needed")
}

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val role: EmployeeRole,
    val salary: Long,
    val skillLevel: Int = 1,
    val efficiencyBonus: Double = 0.0,
    val isActive: Boolean = true,
    val hiredDate: Date = Date()
)

@Serializable
enum class EmployeeRole(
    val baseSalary: Long,
    val maxEfficiencyBonus: Double
) {
    MANAGER(50000, 0.3),
    CASHIER(25000, 0.15),
    SALES_PERSON(20000, 0.2),
    DRIVER(30000, 0.1),
    MECHANIC(35000, 0.2),
    CHEF(40000, 0.25),
    WAITER(18000, 0.1),
    CLEANER(15000, 0.05),
    SECURITY(22000, 0.05),
    ACCOUNTANT(45000, 0.2),
    MARKETER(30000, 0.3),
    FARM_HAND(18000, 0.1),
    VET_TECHNICIAN(35000, 0.2)
}

@Serializable
data class BusinessAsset(
    val id: String,
    val name: String,
    val category: AssetCategory,
    val cost: Long,
    val revenueBonus: Double,
    val maintenanceCost: Long,
    val durability: Int = 100
)

@Serializable
enum class AssetCategory {
    VEHICLE("Vehicle", "Transport assets"),
    EQUIPMENT("Equipment", "Operational equipment"),
    TECHNOLOGY("Technology", "Digital tools"),
    FURNITURE("Furniture", "Premises improvement"),
    LIVESTOCK("Livestock", "Farming assets"),
    INFRASTRUCTURE("Infrastructure", "Fixed improvements")
}

@Serializable
data class UnlockRequirements(
    val minLevel: Int = 1,
    val minCapital: Long = 0,
    val requiredBusinesses: List<String> = emptyList(),
    val requiredReputation: Int = 0,
    val requiredArea: NairobiArea? = null
)

@Serializable
@Entity(tableName = "daily_transaction")
data class DailyTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Date = Date(),
    val type: TransactionType,
    val category: String,
    val amount: Long,
    val description: String,
    val balanceAfter: Long,
    val businessId: String? = null
)

@Serializable
enum class TransactionType {
    INCOME("Income", "+"),
    EXPENSE("Expense", "-"),
    INVESTMENT("Investment", "→"),
    LOAN("Loan", "🏦"),
    MPESA("M-Pesa", "📱"),
    BANK("Bank", "🏛️"),
    SACCO("SACCO", "🤝"),
    CRYPTO("Crypto", "₿"),
    CHAMA("Chama", "👥")
    
    val displayName: String
    val icon: String
    
    constructor(displayName: String, icon: String) {
        this.displayName = displayName
        this.icon = icon
    }
}

@Serializable
@Entity(tableName = "loan")
data class Loan(
    @PrimaryKey val id: String,
    val source: LoanSource,
    val principal: Long,
    var outstandingBalance: Long,
    val interestRate: Double, // Monthly %
    val termMonths: Int,
    val startDate: Date = Date(),
    var nextPaymentDate: Date = Date(),
    var isActive: Boolean = true,
    var missedPayments: Int = 0
) {
    val monthlyPayment: Long
        get() = (principal * (1 + interestRate / 100) / termMonths).toLong()
}

@Serializable
enum class LoanSource(
    val name: String,
    val minRate: Double,
    val maxRate: Double,
    val maxAmount: Long,
    val description: String
) {
    BANK("Bank Loan", 1.5, 3.0, 5000000, "Low rate, strict requirements, collateral needed"),
    SACCO("SACCO Loan", 1.0, 2.0, 2000000, "Member-based, flexible terms, guarantors needed"),
    MICROFINANCE("Microfinance", 3.0, 6.0, 500000, "Fast approval, high rate, group lending"),
    DIGITAL_LENDER("Digital Lender (M-Shwari, Fuliza)", 5.0, 10.0, 100000, "Instant, very high rate, short term"),
    CHAMA("Chama/ROSCA", 0.5, 1.5, 500000, "Community-based, trust-based, social pressure"),
    FAMILY_FRIENDS("Family/Friends", 0.0, 1.0, 200000, "Flexible, relationship risk"),
    ASSET_FINANCING("Asset Financing", 2.0, 4.0, 3000000, "Asset as collateral, for equipment/vehicles")
}

@Serializable
data class ChamaGroup(
    @PrimaryKey val id: String,
    val name: String,
    val members: List<ChamaMember>,
    val contributionAmount: Long,
    val frequency: ChamaFrequency,
    val currentPot: Long = 0,
    val currentRound: Int = 0,
    val totalRounds: Int,
    val payoutOrder: List<String>,
    val isActive: Boolean = true
)

@Serializable
data class ChamaMember(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    var hasPaid: Boolean = false,
    var hasReceived: Boolean = false,
    var trustScore: Int = 100
)

@Serializable
enum class ChamaFrequency {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    BIWEEKLY("Bi-weekly", 14),
    MONTHLY("Monthly", 30)
    
    val displayName: String
    val days: Int
    
    constructor(displayName: String, days: Int) {
        this.displayName = displayName
        this.days = days
    }
}

@Serializable
@Entity(tableName = "market_event")
data class MarketEvent(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: EventType,
    val startDate: Date,
    val endDate: Date,
    val affectedCategories: List<BusinessCategory>,
    val affectedAreas: List<NairobiArea>,
    val revenueMultiplier: Double = 1.0,
    val expenseMultiplier: Double = 1.0,
    val specialConditions: String = ""
)

@Serializable
enum class EventType {
    HOLIDAY("Public Holiday", "Increased spending"),
    FESTIVAL("Festival/Event", "Tourism boost"),
    WEATHER("Weather Event", "Rain/drought impact"),
    ECONOMIC("Economic Policy", "Tax/regulation changes"),
    INFRASTRUCTURE("Infrastructure", "Road/power/water issues"),
    HEALTH("Health Alert", "Disease outbreak"),
    POLITICAL("Political Event", "Elections/protests"),
    SEASONAL("Seasonal", "Planting/harvest/back-to-school"),
    SPORTS("Sports Event", "Match viewing business"),
    TECH("Tech Launch", "Digital opportunity")
}

@TypeConverters(Converters::class)
@Entity(tableName = "simulation_state")
data class SimulationState(
    @PrimaryKey val id: String = "global",
    var currentDate: Date = Date(),
    var gameSpeed: GameSpeed = GameSpeed.NORMAL,
    var isRunning: Boolean = false,
    var totalDaysSimulated: Int = 0,
    var randomSeed: Long = System.currentTimeMillis()
)

enum class GameSpeed {
    PAUSED(0, "Paused"),
    SLOW(1, "1 day = 1 min"),
    NORMAL(2, "1 day = 10 sec"),
    FAST(3, "1 day = 1 sec"),
    VERY_FAST(4, "1 day = 0.1 sec")
    
    val multiplier: Int
    val displayName: String
    
    constructor(multiplier: Int, displayName: String) {
        this.multiplier = multiplier
        this.displayName = displayName
    }
}