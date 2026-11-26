package com.nairobisim.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nairobisim.R
import com.nairobisim.data.converters.Converters
import java.util.*

// ==================== MOBILE MONEY (M-PESA & AIRTEL MONEY) ====================

enum class MobileMoneyProvider(val displayName: String, val prefix: String, val color: String) {
    MPESA("M-Pesa", "254", "#2E7D32"),       // Safaricom
    AIRTEL_MONEY("Airtel Money", "254", "#E91E63"),  // Airtel
    TKOPA("T-KOPA", "254", "#FF5722")      // Telkom
}

enum class MobileMoneyTransactionType(val displayName: String) {
    SEND("Send Money"),
    RECEIVE("Receive Money"),
    DEPOSIT("Deposit from Bank"),
    WITHDRAW("Withdraw to Bank"),
    PAY_BILL("Pay Bill"),
    BUY_AIRTIME("Buy Airtime"),
    BUY_GOODS("Buy Goods (Lipa Na M-Pesa)"),
    REFUND("Refund"),
    FEE("Transaction Fee")
}

enum class MobileMoneyTransactionStatus(val displayName: String, val color: String) {
    SUCCESSFUL("Successful", "#2E7D32"),
    PENDING("Pending", "#FB8C00"),
    FAILED("Failed", "#C62828"),
    CANCELLED("Cancelled", "#9E9E9E")
}

@Entity(tableName = "mobile_money_account")
@TypeConverters(Converters::class)
data class MobileMoneyAccount(
    @PrimaryKey val phoneNumber: String,     // 2547XXXXXXXX
    val provider: MobileMoneyProvider,
    val accountName: String,                 // Player or NPC name
    val balance: Long = 0,
    val isRegistered: Boolean = false,
    val registrationDate: Long? = null,
    val pin: String = "0000",                // 4-digit PIN
    val dailyLimit: Long = 150000,           // KES daily transaction limit
    val dailySpent: Long = 0,
    val lastResetDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mobile_money_transaction")
@TypeConverters(Converters::class)
data class MobileMoneyTransaction(
    @PrimaryKey val id: String,              // e.g. "MPESA20261125123456789"
    val provider: MobileMoneyProvider,
    val type: MobileMoneyTransactionType,
    val fromPhone: String,
    val toPhone: String?,                    // null for pay bill/airtime
    val amount: Long,
    val fee: Long = 0,
    val status: MobileMoneyTransactionStatus,
    val description: String,                 // e.g. "Send to 2547XXXXXXXX"
    val reference: String? = null,           // M-Pesa confirmation code
    val timestamp: Long = System.currentTimeMillis()
)

// ==================== BANKS (REALISTIC KENYAN BANKING) ====================

enum class KenyanBank(val displayName: String, val shortName: String, val color: String) {
    KCB("KCB Bank Kenya", "KCB", "#0066B3"),
    EQUITY("Equity Bank Kenya", "Equity", "#C8102E"),
    COOP("Co-operative Bank", "Co-op", "#00529B"),
    NCBA("NCBA Bank", "NCBA", "#E4002B"),
    ABSA("Absa Bank Kenya", "Absa", "#C8102E"),
    STANBIC("Stanbic Bank", "Stanbic", "#0033A0"),
    IM_BANK("I&M Bank", "I&M", "#00529B"),
    DTB("Diamond Trust Bank", "DTB", "#00529B"),
    FAMILY_BANK("Family Bank", "Family", "#00529B"),
    CITI("Citibank Kenya", "Citi", "#003B70")
}

enum class BankAccountType(val displayName: String, val interestRate: Double, val minBalance: Long) {
    SAVINGS("Savings Account", 5.5, 0),          // 5.5% annual
    FIXED_DEPOSIT("Fixed Deposit", 10.0, 50000), // 10% annual, min 50K
    CURRENT("Current Account", 0.0, 0),
    CALL_DEPOSIT("Call Deposit", 7.0, 25000)
}

enum class BankTransactionType(val displayName: String) {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    INTEREST("Interest Earned"),
    LOAN_DISBURSEMENT("Loan Disbursement"),
    LOAN_REPAYMENT("Loan Repayment"),
    OVERDRAFT("Overdraft"),
    FEE("Bank Fee"),
    CHEQUE("Cheque"),
    STANDING_ORDER("Standing Order")
}

enum class BankTransactionStatus(val displayName: String, val color: String) {
    COMPLETED("Completed", "#2E7D32"),
    PENDING("Pending", "#FB8C00"),
    FAILED("Failed", "#C62828"),
    REVERSED("Reversed", "#9E9E9E")
}

@Entity(tableName = "bank_account")
@TypeConverters(Converters::class)
data class BankAccount(
    @PrimaryKey val accountNumber: String,    // e.g. "1234567890"
    val bank: KenyanBank,
    val accountType: BankAccountType,
    val accountName: String,                  // Player or NPC name
    val balance: Long = 0,
    val isActive: Boolean = true,
    val overdraftLimit: Long = 0,             // KES
    val overdraftUsed: Long = 0,
    val interestAccrued: Long = 0,
    val lastInterestDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_transaction")
@TypeConverters(Converters::class)
data class BankTransaction(
    @PrimaryKey val id: String,
    val bank: KenyanBank,
    val accountNumber: String,
    val type: BankTransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val status: BankTransactionStatus,
    val description: String,
    val reference: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

// ==================== CENTRAL BANK (CBK) ====================

enum class MonetaryPolicyStatus(val displayName: String) {
    EXPANSIONARY("Expansionary (Lower Rates)"),
    CONTRACTIONARY("Contractionary (Higher Rates)"),
    NEUTRAL("Neutral")
}

@Entity(tableName = "central_bank_state")
@TypeConverters(Converters::class)
data class CentralBankState(
    @PrimaryKey val id: String = "cbk_main",
    val centralBankRate: Double = 12.5,        // CBR - currently 12.5% in Kenya
    val inflationRate: Double = 6.3,            // Current inflation
    val moneySupplyM2: Long = 0,                // Total M2 money supply
    val moneySupplyCap: Long = 1000000000000,   // KES 1 trillion cap
    val monetaryPolicy: MonetaryPolicyStatus = MonetaryPolicyStatus.NEUTRAL,
    val reserveRequirement: Double = 4.5,       // Cash Reserve Ratio
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val moneySupplyPercent: Double
        get() = moneySupplyM2.toDouble() / moneySupplyCap * 100
}

// ==================== SACCO & MICROFINANCE ====================

enum class SACCOType(val displayName: String, val interestRate: Double, val description: String) {
    STIMA("Stima SACCO", 12.0, "Kenya Power employees SACCO"),
    MWALIMU("Mwalimu SACCO", 11.5, "Teachers SACCO"),
    POLICE("Police SACCO", 11.0, "Police officers SACCO"),
    HINDY("HINDY SACCO", 13.0, "General SACCO"),
    IMMACULATE("Immaculate SACCO", 14.0, "Mama Ngina Street SACCO")
}

enum class MicrofinanceType(val displayName: String, val interestRate: Double, val maxLoan: Long) {
    FAULU("Faulu Microfinance", 24.0, 100000),
    KREP("KREP Bank", 26.0, 150000),
    KWFT("KWFT (Deki)", 22.0, 80000),
    RAFIKI("Rafiki Microfinance", 28.0, 200000),
    SILVERSTAR("Silverstar Microfinance", 30.0, 50000)
}

// ==================== NPC / PLAYER SYSTEM ====================

enum class PlayerType(val displayName: String) {
    REAL_PLAYER("Real Player"),
    CPU_NPC("CPU Player (NPC)"),
    PLAYER_OWNED_NPC("Player-Owned NPC")      // NPC now controlled by a real user
}

@Entity(tableName = "npc_player")
@TypeConverters(Converters::class)
data class NpcPlayer(
    @PrimaryKey val id: String,
    val name: String,
    val playerType: PlayerType,
    val phoneNumber: String?,                 // M-Pesa number if has account
    val bankAccountNumber: String?,           // Bank account if has one
    val mpesaBalance: Long = 0,
    val bankBalance: Long = 0,
    val totalWealth: Long = 0,
    val location: String = "Nairobi",
    val reputation: Int = 0,
    val isMj3engo: Boolean = false,          // Famous NPC flag
    val ownerPlayerId: String? = null,        // If player-owned, who owns it
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==================== DEFAULT DATA ====================

// Famous Nairobi NPCs (CPU until enough users join)
val DEFAULT_NPC_PLAYERS: List<NpcPlayer> = listOf(
    NpcPlayer(
        id = "npc_001",
        name = "Mzee Jengo (mj3engo)",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254711000001",
        bankAccountNumber = "0001000001",
        mpesaBalance = 2500000,
        bankBalance = 5000000,
        totalWealth = 7500000,
        location = "City Centre",
        reputation = 85,
        isMj3engo = true
    ),
    NpcPlayer(
        id = "npc_002",
        name = "Mama Njeri",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254722000002",
        bankAccountNumber = "0002000002",
        mpesaBalance = 50000,
        bankBalance = 120000,
        totalWealth = 170000,
        location = "Kawangware",
        reputation = 60
    ),
    NpcPlayer(
        id = "npc_003",
        name = "Kamau wa Githurai",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254733000003",
        bankAccountNumber = "0003000003",
        mpesaBalance = 800000,
        bankBalance = 2500000,
        totalWealth = 3300000,
        location = "Githurai",
        reputation = 70
    ),
    NpcPlayer(
        id = "npc_004",
        name = "Aisha Hassan",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254744000004",
        bankAccountNumber = "0004000004",
        mpesaBalance = 300000,
        bankBalance = 1800000,
        totalWealth = 2100000,
        location = "Eastleigh",
        reputation = 75
    ),
    NpcPlayer(
        id = "npc_005",
        name = "Otieno Omondi",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254755000005",
        bankAccountNumber = "0005000005",
        mpesaBalance = 150000,
        bankBalance = 500000,
        totalWealth = 650000,
        location = "Kariobangi",
        reputation = 50
    ),
    NpcPlayer(
        id = "npc_006",
        name = "Wanjiku Mutua",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254766000006",
        bankAccountNumber = "0006000006",
        mpesaBalance = 200000,
        bankBalance = 900000,
        totalWealth = 1100000,
        location = "Upper Hill",
        reputation = 65
    ),
    NpcPlayer(
        id = "npc_007",
        name = "James Mwangi",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254777000007",
        bankAccountNumber = "0007000007",
        mpesaBalance = 500000,
        bankBalance = 3000000,
        totalWealth = 3500000,
        location = "Kilimani",
        reputation = 80
    ),
    NpcPlayer(
        id = "npc_008",
        name = "Sarah Wanjiku",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254788000008",
        bankAccountNumber = "0008000008",
        mpesaBalance = 100000,
        bankBalance = 400000,
        totalWealth = 500000,
        location = "Westlands",
        reputation = 55
    ),
    NpcPlayer(
        id = "npc_009",
        name = "Brian Otieno",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254799000009",
        bankAccountNumber = "0009000009",
        mpesaBalance = 50000,
        bankBalance = 200000,
        totalWealth = 250000,
        location = "Dandora",
        reputation = 40
    ),
    NpcPlayer(
        id = "npc_010",
        name = "Grace Mutua",
        playerType = PlayerType.CPU_NPC,
        phoneNumber = "254700000010",
        bankAccountNumber = "0010000010",
        mpesaBalance = 80000,
        bankBalance = 350000,
        totalWealth = 430000,
        location = "Kilimani",
        reputation = 60
    )
)