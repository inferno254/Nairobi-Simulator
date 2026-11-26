package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nairobisim.data.converters.Converters
import kotlinx.serialization.Serializable
import java.util.*

@Entity(tableName = "player_profile")
@TypeConverters(Converters::class)
data class PlayerProfile(
    @PrimaryKey val id: String = "player_1",
    val name: String = "Nairobi Entrepreneur",
    var playerName: String = "",                          // onboarding name (shows in leaderboard)
    var kesBalance: Long? = 50000,
    var mpesaBalance: Long? = 10000,
    var bankBalance: Long? = 0,
    var saccosBalance: Long? = 0,
    var cryptoWallet: Long? = 0,
    var forexBalance: Double? = 0.0,                      // forex trading balance
    var reputation: Int = 0,
    var level: Int = 1,
    var experience: Long = 0,
    var lastLogin: Date = Date(),
    var dailyStreak: Int = 0,
    var totalBusinessesOwned: Int = 0,
    var totalEmployees: Int = 0,
    var chamas: List<Chama> = emptyList(),
    var shareHoldings: List<ShareHolding> = emptyList(),  // JSON stored
    var ownedCars: List<OwnedCar> = emptyList(),          // JSON stored
    var bribeHistory: List<BribeEncounter> = emptyList(), // JSON stored
    var totalNetWorth: Long? = 50000,                     // calculated net worth
    var isProfileComplete: Boolean = false,               // onboarding flag
    val unlockRequirements: UnlockRequirements
) {
    val displayName: String
        get() = if (playerName.isNotBlank()) playerName else name
    
    fun calculateNetWorth(): Long {
        val businessValue = 0L
        val carValue = ownedCars.sumOf { it.currentValue }
        val shareValue = shareHoldings.sumOf { it.currentValue.toLong() }
        val forexValue = (forexBalance ?: 0.0).toLong()
        return (kesBalance ?: 0) + (bankBalance ?: 0) + (mpesaBalance ?: 0) + (saccosBalance ?: 0) + businessValue + carValue + shareValue + forexValue
    }
}

@Entity(tableName = "business")
@TypeConverters(Converters::class)
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

    // Continuous per-hour profit (30-day month = 720 hours), like Business Empire
    val hourlyProfit: Double
        get() = netMonthlyIncome / 720.0

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
    HORTICULTURE("Horticulture", "Agriculture", 1000000);

    val displayName: String
    val sector: String
    val baseCapital: Long
    
    constructor(displayName: String, sector: String, baseCapital: Long) {
        this.displayName = displayName
        this.sector = sector
        this.baseCapital = baseCapital
    }
}

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
    EASTLEIGH("Eastleigh", 1.5, 1.0, 8, "Somali business hub, textiles"),
    KAYOLE("Kayole", 1.3, 0.7, 6, "Eastlands residential"),
    KILIMANI("Kilimani", 1.4, 2.0, 7, "Young professionals, apartments"),
    KIAMBU("Kiambu", 1.1, 1.0, 4, "Peri-urban, dairy farming"),
    ONLINE("Online/Digital", 1.0, 0.2, 3, "No physical location needed")
}

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

data class BusinessAsset(
    val id: String,
    val name: String,
    val category: AssetCategory,
    val cost: Long,
    val revenueBonus: Double,
    val maintenanceCost: Long,
    val durability: Int = 100
)

enum class AssetCategory {
    VEHICLE, EQUIPMENT, TECHNOLOGY, FURNITURE, LIVESTOCK, INFRASTRUCTURE;
    
    val displayName: String
        get() = when (this) {
            VEHICLE -> "Vehicle"
            EQUIPMENT -> "Equipment"
            TECHNOLOGY -> "Technology"
            FURNITURE -> "Furniture"
            LIVESTOCK -> "Livestock"
            INFRASTRUCTURE -> "Infrastructure"
        }
    
    val description: String
        get() = when (this) {
            VEHICLE -> "Transport assets"
            EQUIPMENT -> "Operational equipment"
            TECHNOLOGY -> "Digital tools"
            FURNITURE -> "Premises improvement"
            LIVESTOCK -> "Farming assets"
            INFRASTRUCTURE -> "Fixed improvements"
        }
}

data class UnlockRequirements(
    val minLevel: Int = 1,
    val minCapital: Long = 0,
    val requiredBusinesses: List<String> = emptyList(),
    val requiredReputation: Int = 0,
    val requiredArea: NairobiArea? = null
)

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

enum class TransactionType {
    INCOME("Income", "+"),
    EXPENSE("Expense", "-"),
    INVESTMENT("Investment", "→"),
    LOAN("Loan", "🏦"),
    MPESA("M-Pesa", "📱"),
    BANK("Bank", "🏛️"),
    SACCO("SACCO", "🤝"),
    CRYPTO("Crypto", "₿"),
    CHAMA("Chama", "👥");

    val displayName: String
    val icon: String
    
    constructor(displayName: String, icon: String) {
        this.displayName = displayName
        this.icon = icon
    }
}

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
        get() = if (termMonths > 0) (principal * (1 + interestRate / 100) / termMonths).toLong() else 0L

    val lenderName: String get() = source.displayName
    val originalAmount: Long get() = principal
    val remainingBalance: Long get() = outstandingBalance
    val monthsRemaining: Int get() = termMonths
}

enum class LoanSource(
    val title: String,
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
    ASSET_FINANCING("Asset Financing", 2.0, 4.0, 3000000, "Asset as collateral, for equipment/vehicles");

    val displayName: String get() = title
}

enum class LoanType(
    val title: String,
    val description: String,
    val minAmount: Long,
    val maxAmount: Long,
    val interestRate: Double,
    val minTerm: Int,
    val maxTerm: Int
) {
    BUSINESS("Business Loan", "Standard business financing for expansion", 100000, 5000000, 0.14, 12, 60),
    EMERGENCY("Emergency Loan", "Quick cash flow assistance", 10000, 100000, 0.20, 1, 6),
    ASSET("Asset Financing", "Funding for vehicles and machinery", 200000, 3000000, 0.12, 12, 48),
    MICRO("Micro Loan", "Small business working capital", 5000, 50000, 0.18, 1, 12);

    val displayName: String get() = title
}

data class Chama(
    val id: String,
    val name: String,
    val description: String = "",
    val members: List<String> = emptyList(),
    val monthlyContribution: Long = 0,
    val cycleDuration: Int = 12,
    val currentCycle: Int = 1,
    val totalPool: Long = 0,
    val memberShares: Map<String, Long> = emptyMap(),
    val maxMembers: Int = 20,
    val isActive: Boolean = true,
    val nextPayoutDate: String = ""
)

@Entity(tableName = "chama_group")
@TypeConverters(Converters::class)
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

data class ChamaMember(
    val userId: String,
    val name: String,
    val phoneNumber: String,
    var hasPaid: Boolean = false,
    var hasReceived: Boolean = false,
    var trustScore: Int = 100
)

enum class ChamaFrequency {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    BIWEEKLY("Bi-weekly", 14),
    MONTHLY("Monthly", 30);

    val displayName: String
    val days: Int
    
    constructor(displayName: String, days: Int) {
        this.displayName = displayName
        this.days = days
    }
}

@Entity(tableName = "market_event")
@TypeConverters(Converters::class)
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

enum class EventType {
    HOLIDAY, FESTIVAL, WEATHER, ECONOMIC, INFRASTRUCTURE, HEALTH, POLITICAL, SEASONAL, SPORTS, TECH;
    
    val displayName: String
        get() = when (this) {
            HOLIDAY -> "Public Holiday"
            FESTIVAL -> "Festival/Event"
            WEATHER -> "Weather Event"
            ECONOMIC -> "Economic Policy"
            INFRASTRUCTURE -> "Infrastructure"
            HEALTH -> "Health Alert"
            POLITICAL -> "Political Event"
            SEASONAL -> "Seasonal"
            SPORTS -> "Sports Event"
            TECH -> "Tech Launch"
        }
    
    val description: String
        get() = when (this) {
            HOLIDAY -> "Increased spending"
            FESTIVAL -> "Tourism boost"
            WEATHER -> "Rain/drought impact"
            ECONOMIC -> "Tax/regulation changes"
            INFRASTRUCTURE -> "Road/power/water issues"
            HEALTH -> "Disease outbreak"
            POLITICAL -> "Elections/protests"
            SEASONAL -> "Planting/harvest/back-to-school"
            SPORTS -> "Match viewing business"
            TECH -> "Digital opportunity"
        }
}

@TypeConverters(Converters::class)
@Entity(tableName = "simulation_state")
data class SimulationState(
    @PrimaryKey val id: String = "global",
    var currentDate: Date = Date(),
    var gameSpeed: GameSpeed = GameSpeed.NORMAL,
    var isRunning: Boolean = false,
    var totalDaysSimulated: Int = 0,
    var randomSeed: Long = System.currentTimeMillis(),
    var lastTickRealTime: Long = System.currentTimeMillis()
)

enum class GameSpeed {
    PAUSED(0, "Paused"),
    SLOW(1, "1 day = 1 min"),
    NORMAL(2, "1 day = 10 sec"),
    FAST(3, "1 day = 1 sec"),
    VERY_FAST(4, "1 day = 0.1 sec");

    val multiplier: Int
    val displayName: String
    
    constructor(multiplier: Int, displayName: String) {
        this.multiplier = multiplier
        this.displayName = displayName
    }
}

// ==================== FOREX TRADING MODELS ====================

enum class TradeType {
    BUY, SELL
}

data class CurrencyPair(
    val symbol: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val pipSize: Double,
    val typicalSpread: Double,
    val minLotSize: Double = 0.01,
    val maxLotSize: Double = 100.0,
    val marginRate: Double = 0.01,
    val isActive: Boolean = true,
    val icon: String = ""
) {
    val displayName: String
        get() = "$baseCurrency / $quoteCurrency"
}

data class MarketRate(
    val symbol: String,
    val bid: Double,      // price to sell
    val ask: Double,      // price to buy
    val spread: Double    // ask - bid
) {
    val midPrice: Double get() = (bid + ask) / 2
    val timestamp: Long = System.currentTimeMillis()
}

@Entity(tableName = "forex_trade")
data class ForexTrade(
    @PrimaryKey val id: String,
    val symbol: String,
    val type: TradeType,
    val lotSize: Double,
    val openPrice: Double,
    val openTime: Long = System.currentTimeMillis(),
    var closePrice: Double = 0.0,
    var closeTime: Long = 0,
    var isClosed: Boolean = false,
    var stopLoss: Double = 0.0,
    var takeProfit: Double = 0.0,
    var pnl: Double = 0.0,
    val commission: Double = 0.0
) {
    val isBuy: Boolean get() = type == TradeType.BUY
    
    fun calculateUnrealizedPnL(currentBid: Double, currentAsk: Double): Double {
        val exitPrice = if (type == TradeType.BUY) currentBid else currentAsk
        val priceDiff = exitPrice - openPrice
        val pnl = priceDiff * lotSize * 100000 // standard lot = 100,000 units
        return pnl - commission
    }
    
    fun isStopLossHit(currentBid: Double, currentAsk: Double): Boolean {
        if (stopLoss <= 0.0) return false
        return when (type) {
            TradeType.BUY -> currentBid <= stopLoss
            TradeType.SELL -> currentAsk >= stopLoss
        }
    }
    
    fun isTakeProfitHit(currentBid: Double, currentAsk: Double): Boolean {
        if (takeProfit <= 0.0) return false
        return when (type) {
            TradeType.BUY -> currentAsk >= takeProfit
            TradeType.SELL -> currentBid <= takeProfit
        }
    }
    
    val marginRequired: Double
        get() = (openPrice * lotSize * 100000) * marginRate(symbol)
    
    private fun marginRate(symbol: String): Double = 0.01 // 1:100 leverage default
}

data class PlayerForexAccount(
    val playerId: String,
    var balance: Double = 10000.0,      // starting virtual capital in KES
    var equity: Double = 10000.0,       // balance + unrealized P&L
    var usedMargin: Double = 0.0,
    var freeMargin: Double = 10000.0,
    var marginLevel: Double = 0.0
) {
    fun updateEquity(unrealizedPnL: Double) {
        equity = balance + unrealizedPnL
        freeMargin = equity - usedMargin
        marginLevel = if (usedMargin > 0) (equity / usedMargin) * 100 else 0.0
    }
    
    val canOpenPosition: Boolean
        get() = freeMargin > 0 && marginLevel > 100 // margin call at 100%
}

// Default currency pairs (Kenya-focused + majors)
val DEFAULT_FOREX_PAIRS = listOf(
    CurrencyPair("KES/USD", "KES", "USD", 0.0001, 0.0002),
    CurrencyPair("KES/GBP", "KES", "GBP", 0.0001, 0.0003),
    CurrencyPair("KES/EUR", "KES", "EUR", 0.0001, 0.0002),
    CurrencyPair("USD/EUR", "USD", "EUR", 0.0001, 0.00015),
    CurrencyPair("USD/GBP", "USD", "GBP", 0.0001, 0.0002),
    CurrencyPair("EUR/GBP", "EUR", "GBP", 0.0001, 0.0002),
    CurrencyPair("USD/KES", "USD", "KES", 0.01, 0.02),
    CurrencyPair("GBP/KES", "GBP", "KES", 0.01, 0.03)
)

// ==================== LEADERBOARD MODELS ====================

data class PlayerProfileLeaderboard(
    val playerId: String,
    val playerName: String,
    val totalNetWorth: Long,
    val kesBalance: Long,
    val forexBalance: Double,
    val businessCount: Int,
    val level: Int,
    val rank: Int = 0,
    val avatarUrl: String = "",
    val country: String = "Kenya",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class LeaderboardEntry(
    val playerId: String,
    val playerName: String,
    val totalNetWorth: Long,
    val kesBalance: Long = 0L,
    val forexBalance: Double = 0.0,
    val businessCount: Int = 0,
    val level: Int = 1,
    val avatarUrl: String = "",
    val country: String = "Kenya",
    val badge: String = "🌱 Hustler",
    val rank: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

// ==================== AUTH & ONBOARDING ====================

enum class AuthProvider {
    ANONYMOUS, GOOGLE, FACEBOOK
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val playerId: String,
    var playerName: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var avatarUrl: String = "",
    var bio: String = "",
    var city: String = "Nairobi",
    var country: String = "Kenya",
    var authProvider: AuthProvider = AuthProvider.ANONYMOUS,
    var authProviderId: String = "",
    var isProfileComplete: Boolean = false,
    var createdAt: Long = System.currentTimeMillis(),
    var lastLoginAt: Long = System.currentTimeMillis()
)

// ==================== SHARE MARKET MODELS (Nairobi-focused, copyright-safe) ====================

enum class ShareSector {
    TELECOM, ENERGY, BANKING, BEVERAGE, REAL_ESTATE, MEDIA, TRANSPORT, AGRICULTURE
}

data class ShareCompany(
    val symbol: String,
    val name: String,
    val sector: ShareSector,
    var currentPrice: Double,
    var previousClose: Double,
    var dayHigh: Double,
    var dayLow: Double,
    var marketCap: Long,
    var peRatio: Double,
    var dividendYield: Double,
    var changePercent: Double = 0.0,
    val description: String = "",
    val logoUrl: String = ""
) {
    val change: Double get() = currentPrice - previousClose
    
    fun updatePrice() {
        val volatility = when (sector) {
            ShareSector.TELECOM -> 0.02
            ShareSector.ENERGY -> 0.03
            ShareSector.BANKING -> 0.025
            ShareSector.BEVERAGE -> 0.015
            ShareSector.REAL_ESTATE -> 0.02
            ShareSector.MEDIA -> 0.035
            ShareSector.TRANSPORT -> 0.03
            ShareSector.AGRICULTURE -> 0.04
        }
        val change = (Math.random() - 0.5) * 2 * volatility * currentPrice
        previousClose = currentPrice
        currentPrice = (currentPrice + change).coerceAtLeast(0.01)
        dayHigh = maxOf(dayHigh, currentPrice)
        dayLow = minOf(dayLow, currentPrice)
        changePercent = ((currentPrice - previousClose) / previousClose) * 100
    }
}

// Nairobi share market (copyright-safe names - modified real companies)
val NAIROBI_SHARE_MARKET = listOf(
    ShareCompany("SWC", "SawaCom", ShareSector.TELECOM, 18.50, 18.20, 18.90, 18.10, 450_000_000_000L, 12.5, 3.2),
    ShareCompany("KPW", "KenyaPower", ShareSector.ENERGY, 1.85, 1.82, 1.92, 1.78, 85_000_000_000L, 8.2, 5.1),
    ShareCompany("EQB", "EquityBank", ShareSector.BANKING, 42.00, 41.50, 43.50, 41.00, 220_000_000_000L, 9.8, 4.5),
    ShareCompany("KBL", "KenyaBrew", ShareSector.BEVERAGE, 310.00, 305.00, 315.00, 302.00, 180_000_000_000L, 15.2, 2.8),
    ShareCompany("NSE", "NairobiSecurities", ShareSector.REAL_ESTATE, 8.50, 8.30, 8.90, 8.10, 95_000_000_000L, 11.0, 3.5),
    ShareCompany("STD", "StandardMedia", ShareSector.MEDIA, 12.30, 12.10, 13.00, 11.80, 45_000_000_000L, 14.5, 1.9),
    ShareCompany("KQW", "KenyaWings", ShareSector.TRANSPORT, 4.20, 4.10, 4.50, 3.90, 35_000_000_000L, 6.8, 0.5),
    ShareCompany("EABL", "EastAgri", ShareSector.AGRICULTURE, 155.00, 152.00, 158.00, 150.00, 120_000_000_000L, 13.2, 3.8)
)

data class ShareHolding(
    val symbol: String,
    val shares: Int,
    val averageBuyPrice: Double,
    val totalInvested: Double
) {
    val currentValue: Double
        get() = shares * currentPrice(symbol)
    
    val unrealizedPnL: Double
        get() = (currentPrice(symbol) - averageBuyPrice) * shares
    
    private fun currentPrice(symbol: String): Double =
        NAIROBI_SHARE_MARKET.find { it.symbol == symbol }?.currentPrice ?: 0.0
}

@Entity(tableName = "share_trade")
data class ShareTrade(
    @PrimaryKey val id: String,
    val symbol: String,
    val type: TradeType,
    val shares: Int,
    val price: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalValue: Double
        get() = shares * price
}

// ==================== CAR MODELS (copyright-safe) ====================

data class CarBrand(
    val id: String,
    val name: String,          // e.g. "Toyo" instead of Toyota
    val origin: String,        // e.g. "Japan"
    val prestige: Int,         // 1-10
    val reliability: Int,      // 1-10
    val logoUrl: String = ""
)

data class CarModel(
    val id: String,
    val brandId: String,
    val name: String,          // e.g. "Corolla X" instead of "Corolla"
    val year: Int,
    val priceKES: Long,
    val fuelConsumption: Double, // km per liter
    val maintenanceCost: Long,   // monthly
    val prestige: Int,         // 1-10
    val comfort: Int,          // 1-10
    val reliability: Int,      // 1-10
    val imageUrl: String = ""
)

data class OwnedCar(
    val id: String,
    val carModelId: String,
    val purchasePrice: Long,
    val purchaseDate: Long,
    var currentValue: Long,
    var condition: Int = 100,   // 0-100
    var mileageKm: Int = 0,
    val color: String = "Black",
    val plateNumber: String = ""
)

val CAR_BRANDS = listOf(
    CarBrand("toyo", "Toyo", "Japan", 9, 9),
    CarBrand("niss", "Niss", "Japan", 8, 8),
    CarBrand("hond", "Hond", "Japan", 8, 8),
    CarBrand("mazd", "Mazd", "Japan", 7, 8),
    CarBrand("merz", "Merz", "Germany", 10, 6),
    CarBrand("bmw", "BMV", "Germany", 10, 5),
    CarBrand("aud", "Aud", "Germany", 9, 6),
    CarBrand("ford", "Ford", "USA", 7, 7),
    CarBrand("chev", "Chev", "USA", 6, 7),
    CarBrand("tata", "Tata", "India", 5, 6)
)

val CAR_MODELS = listOf(
    CarModel("c1", "toyo", "Corolla X", 2024, 2_200_000, 15.0, 3_000, 8, 7, 9),
    CarModel("c2", "toyo", "Prado TX", 2024, 8_500_000, 8.0, 8_000, 9, 9, 8),
    CarModel("c3", "niss", "X-Trail Z", 2024, 3_500_000, 12.0, 4_000, 7, 8, 7),
    CarModel("c4", "niss", "Patrol S", 2024, 9_000_000, 7.0, 9_000, 8, 9, 7),
    CarModel("c5", "hond", "Civic R", 2024, 2_800_000, 14.0, 3_500, 7, 7, 8),
    CarModel("c6", "merz", "C-Class S", 2024, 7_200_000, 10.0, 7_000, 10, 9, 7),
    CarModel("c7", "bmw", "X5 M", 2024, 12_000_000, 9.0, 10_000, 10, 9, 6),
    CarModel("c8", "ford", "Ranger X", 2024, 3_800_000, 11.0, 4_500, 6, 7, 8),
    CarModel("c9", "tata", "Nano Plus", 2024, 900_000, 18.0, 2_000, 3, 4, 6),
    CarModel("c10", "mazd", "CX-5 G", 2024, 4_200_000, 13.0, 4_200, 7, 8, 7)
)

// ==================== BRIBERY / CORRUPTION MODELS (Kenya-specific) ====================

enum class BribeType {
    TRAFFIC_STOP,     // Police roadblock
    PERMIT_APPROVAL,  // County government permit
    LICENSE_RENEWAL,  // Business license
    TAX_AUDIT,        // KRA tax audit
    CUSTOMS_CLEARANCE, // Port clearance
    HEALTH_INSPECTION, // Public health
    BUILDING_APPROVAL  // Construction permit
}

data class BribeEncounter(
    val id: String,
    val type: BribeType,
    val authority: String,      // e.g. "Nairobi County Askari", "KRA Officer"
    val location: String,       // e.g. "Westlands Roadblock", "City Hall"
    val demandedAmount: Long,   // KES
    val minimumAcceptable: Long,
    val successChance: Double,  // 0.0-1.0
    val reputationRisk: Double, // chance of getting caught (0.0-1.0)
    val consequenceDescription: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isResolved: Boolean = false,
    var wasPaid: Boolean = false,
    var amountPaid: Long = 0,
    var wasCaught: Boolean = false
)

data class BribeOutcome(
    val success: Boolean,
    val amountPaid: Long,
    val wasCaught: Boolean,
    val fineAmount: Long = 0,
    val reputationImpact: Int = 0,  // negative = bad
    val message: String
)

// Default bribe scenarios
val DEFAULT_BRIBE_SCENARIOS = listOf(
    BribeEncounter("b1", BribeType.TRAFFIC_STOP, "Traffic Police", "Thika Road Roadblock", 5_000, 2_000, 0.85, 0.15, "License confiscated, court appearance"),
    BribeEncounter("b2", BribeType.PERMIT_APPROVAL, "Nairobi County Officer", "City Hall Annex", 50_000, 20_000, 0.70, 0.20, "Permit denied, business closure notice"),
    BribeEncounter("b3", BribeType.LICENSE_RENEWAL, "Single Business Permit Office", "Harambee House", 30_000, 15_000, 0.75, 0.10, "License revoked, 30-day shutdown"),
    BribeEncounter("b4", BribeType.TAX_AUDIT, "KRA Tax Officer", "Times Tower", 200_000, 100_000, 0.60, 0.30, "Tax penalty 200%, audit escalation"),
    BribeEncounter("b5", BribeType.CUSTOMS_CLEARANCE, "KRA Customs Officer", "Mombasa Port", 100_000, 50_000, 0.65, 0.25, "Goods seized, storage fees"),
    BribeEncounter("b6", BribeType.HEALTH_INSPECTION, "Public Health Officer", "Sub-County Hospital", 25_000, 10_000, 0.80, 0.15, "Business closure, health notice"),
    BribeEncounter("b7", BribeType.BUILDING_APPROVAL, "Nairobi County Planning", "City Hall Planning", 150_000, 80_000, 0.55, 0.35, "Stop-work order, demolition notice")
)

// ==================== UPDATED PLAYER PROFILE WITH FOREX & SHARES ====================

// Extended PlayerProfile fields (added to existing data class below)
