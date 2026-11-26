package com.nairobisim.data.repository

import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.dao.*
import com.nairobisim.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import kotlin.random.Random

class BankingRepository(
    private val db: AppDatabase,
    private val mobileMoneyAccountDao: MobileMoneyAccountDao,
    private val mobileMoneyTransactionDao: MobileMoneyTransactionDao,
    private val bankAccountDao: BankAccountDao,
    private val bankTransactionDao: BankTransactionDao,
    private val centralBankDao: CentralBankDao,
    private val npcPlayerDao: NpcPlayerDao
) {

    private val random = Random.Default

    // ==================== MOBILE MONEY (M-PESA & AIRTEL) ====================

    // Register a new M-Pesa/Airtel Money account
    suspend fun registerMobileMoney(phone: String, provider: MobileMoneyProvider, name: String, pin: String): MobileMoneyAccount {
        val existing = mobileMoneyAccountDao.getAccount(phone)
        if (existing != null && existing.isRegistered) {
            throw IllegalStateException("Account already registered")
        }

        val account = MobileMoneyAccount(
            phoneNumber = phone,
            provider = provider,
            accountName = name,
            balance = 0,
            isRegistered = true,
            registrationDate = System.currentTimeMillis(),
            pin = pin
        )
        mobileMoneyAccountDao.insertAccount(account)

        // Give starting balance for new players
        val updated = account.copy(balance = 50000) // KES 50,000 starting
        mobileMoneyAccountDao.insertAccount(updated)

        return updated
    }

    // Get M-Pesa account by phone number
    suspend fun getMobileMoneyAccount(phone: String): MobileMoneyAccount? =
        mobileMoneyAccountDao.getAccount(phone)

    // Send money via M-Pesa to another phone
    suspend fun sendMoney(fromPhone: String, toPhone: String, amount: Long, pin: String): MobileMoneyTransaction {
        val fromAccount = mobileMoneyAccountDao.getAccount(fromPhone)
            ?: throw IllegalStateException("Sender account not found")
        val toAccount = mobileMoneyAccountDao.getAccount(toPhone)
            ?: throw IllegalStateException("Recipient account not found")

        if (fromAccount.pin != pin) throw IllegalStateException("Invalid PIN")
        if (fromAccount.balance < amount) throw IllegalStateException("Insufficient funds")

        // Check daily limit
        val today = resetDailyIfNeeded(fromAccount)
        if (today.dailySpent + amount > today.dailyLimit) {
            throw IllegalStateException("Daily transaction limit exceeded")
        }

        // Calculate fee (M-Pesa charges ~KES 50-100 for sends)
        val fee = calculateMpesaFee(amount)

        // Update sender
        val updatedFrom = fromAccount.copy(
            balance = fromAccount.balance - amount - fee,
            dailySpent = fromAccount.dailySpent + amount,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updatedFrom)

        // Update recipient
        val updatedTo = toAccount.copy(
            balance = toAccount.balance + amount,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updatedTo)

        // Create transaction record
        val transaction = MobileMoneyTransaction(
            id = "${fromAccount.provider.prefix}${System.currentTimeMillis()}",
            provider = fromAccount.provider,
            type = MobileMoneyTransactionType.SEND,
            fromPhone = fromPhone,
            toPhone = toPhone,
            amount = amount,
            fee = fee,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = "Send KES $amount to $toPhone",
            reference = "MPESA${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        // Update NPC wealth if recipient is NPC
        updateNpcWealthIfNeeded(toPhone, amount, 0)

        return transaction
    }

    // Receive money (simulated - money appears from nowhere, e.g. salary)
    suspend fun receiveMoney(phone: String, amount: Long, description: String): MobileMoneyTransaction {
        val account = mobileMoneyAccountDao.getAccount(phone)
            ?: throw IllegalStateException("Account not found")

        val updated = account.copy(
            balance = account.balance + amount,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updated)

        val transaction = MobileMoneyTransaction(
            id = "${account.provider.prefix}${System.currentTimeMillis()}",
            provider = account.provider,
            type = MobileMoneyTransactionType.RECEIVE,
            fromPhone = "SYSTEM",
            toPhone = phone,
            amount = amount,
            fee = 0,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = description,
            reference = "MPESA${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Deposit from bank to M-Pesa
    suspend fun depositToMobileMoney(phone: String, amount: Long): MobileMoneyTransaction {
        val account = mobileMoneyAccountDao.getAccount(phone)
            ?: throw IllegalStateException("Account not found")

        // Check if player has enough in bank
        val bankAccounts = bankAccountDao.getAccountsByName(account.accountName)
        val totalBankBalance = bankAccounts.sumOf { it.balance }
        if (totalBankBalance < amount) throw IllegalStateException("Insufficient bank balance")

        // Deduct from first available bank account
        val bankAccount = bankAccounts.first { it.balance >= amount }
            ?: throw IllegalStateException("No bank account with sufficient funds")
        val updatedBank = bankAccount.copy(balance = bankAccount.balance - amount)
        bankAccountDao.insertAccount(updatedBank)

        // Add to M-Pesa
        val updated = account.copy(
            balance = account.balance + amount,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updated)

        val transaction = MobileMoneyTransaction(
            id = "${account.provider.prefix}${System.currentTimeMillis()}",
            provider = account.provider,
            type = MobileMoneyTransactionType.DEPOSIT,
            fromPhone = bankAccount.accountNumber,
            toPhone = phone,
            amount = amount,
            fee = 0,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = "Deposit from ${bankAccount.bank.displayName}",
            reference = "BANK${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Withdraw from M-Pesa to bank
    suspend fun withdrawToBank(phone: String, amount: Long, bankAccountNumber: String): MobileMoneyTransaction {
        val account = mobileMoneyAccountDao.getAccount(phone)
            ?: throw IllegalStateException("Account not found")
        val bankAccount = bankAccountDao.getAccount(bankAccountNumber)
            ?: throw IllegalStateException("Bank account not found")

        if (account.balance < amount) throw IllegalStateException("Insufficient M-Pesa balance")

        // Withdrawal fee (KES 50-100)
        val fee = calculateWithdrawalFee(amount)

        val updatedMobile = account.copy(
            balance = account.balance - amount - fee,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updatedMobile)

        val updatedBank = bankAccount.copy(
            balance = bankAccount.balance + amount,
            updatedAt = System.currentTimeMillis()
        )
        bankAccountDao.insertAccount(updatedBank)

        val transaction = MobileMoneyTransaction(
            id = "${account.provider.prefix}${System.currentTimeMillis()}",
            provider = account.provider,
            type = MobileMoneyTransactionType.WITHDRAW,
            fromPhone = phone,
            toPhone = bankAccountNumber,
            amount = amount,
            fee = fee,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = "Withdraw to ${bankAccount.bank.displayName}",
            reference = "MPESA${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Pay bill (KPLC, water, etc.)
    suspend fun payBill(phone: String, biller: String, accountNumber: String, amount: Long, pin: String): MobileMoneyTransaction {
        val account = mobileMoneyAccountDao.getAccount(phone)
            ?: throw IllegalStateException("Account not found")

        if (account.pin != pin) throw IllegalStateException("Invalid PIN")
        if (account.balance < amount) throw IllegalStateException("Insufficient funds")

        val fee = 0L // No fee for bill payments usually

        val updated = account.copy(
            balance = account.balance - amount - fee,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updated)

        val transaction = MobileMoneyTransaction(
            id = "${account.provider.prefix}${System.currentTimeMillis()}",
            provider = account.provider,
            type = MobileMoneyTransactionType.PAY_BILL,
            fromPhone = phone,
            toPhone = null,
            amount = amount,
            fee = fee,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = "Pay bill to $biller ($accountNumber)",
            reference = "MPESA${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Buy airtime
    suspend fun buyAirtime(phone: String, amount: Long, pin: String): MobileMoneyTransaction {
        val account = mobileMoneyAccountDao.getAccount(phone)
            ?: throw IllegalStateException("Account not found")

        if (account.pin != pin) throw IllegalStateException("Invalid PIN")
        if (account.balance < amount) throw IllegalStateException("Insufficient funds")

        val updated = account.copy(
            balance = account.balance - amount,
            updatedAt = System.currentTimeMillis()
        )
        mobileMoneyAccountDao.insertAccount(updated)

        val transaction = MobileMoneyTransaction(
            id = "${account.provider.prefix}${System.currentTimeMillis()}",
            provider = account.provider,
            type = MobileMoneyTransactionType.BUY_AIRTIME,
            fromPhone = phone,
            toPhone = null,
            amount = amount,
            fee = 0,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = "Buy KES $amount airtime",
            reference = "MPESA${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Get M-Pesa transaction history
    fun getMobileMoneyTransactions(phone: String): Flow<List<MobileMoneyTransaction>> =
        mobileMoneyTransactionDao.getTransactionsForPhone(phone)

    // ==================== BANKS ====================

    // Open a bank account
    suspend fun openBankAccount(name: String, bank: KenyanBank, accountType: BankAccountType, initialDeposit: Long): BankAccount {
        val accountNumber = generateBankAccountNumber(bank)

        val account = BankAccount(
            accountNumber = accountNumber,
            bank = bank,
            accountType = accountType,
            accountName = name,
            balance = initialDeposit,
            isActive = true
        )
        bankAccountDao.insertAccount(account)

        // Record initial deposit
        val transaction = BankTransaction(
            id = "BANK${System.currentTimeMillis()}",
            bank = bank,
            accountNumber = accountNumber,
            type = BankTransactionType.DEPOSIT,
            amount = initialDeposit,
            balanceAfter = initialDeposit,
            status = BankTransactionStatus.COMPLETED,
            description = "Account opening deposit"
        )
        bankTransactionDao.insertTransaction(transaction)

        return account
    }

    // Get bank accounts by name
    suspend fun getBankAccounts(name: String): List<BankAccount> =
        bankAccountDao.getAccountsByName(name)

    // Deposit to bank
    suspend fun depositToBank(accountNumber: String, amount: Long): BankTransaction {
        val account = bankAccountDao.getAccount(accountNumber)
            ?: throw IllegalStateException("Account not found")

        val updated = account.copy(
            balance = account.balance + amount,
            updatedAt = System.currentTimeMillis()
        )
        bankAccountDao.insertAccount(updated)

        val transaction = BankTransaction(
            id = "BANK${System.currentTimeMillis()}",
            bank = account.bank,
            accountNumber = accountNumber,
            type = BankTransactionType.DEPOSIT,
            amount = amount,
            balanceAfter = updated.balance,
            status = BankTransactionStatus.COMPLETED,
            description = "Cash deposit"
        )
        bankTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Withdraw from bank
    suspend fun withdrawFromBank(accountNumber: String, amount: Long): BankTransaction {
        val account = bankAccountDao.getAccount(accountNumber)
            ?: throw IllegalStateException("Account not found")

        if (account.balance < amount) throw IllegalStateException("Insufficient funds")

        val updated = account.copy(
            balance = account.balance - amount,
            updatedAt = System.currentTimeMillis()
        )
        bankAccountDao.insertAccount(updated)

        val transaction = BankTransaction(
            id = "BANK${System.currentTimeMillis()}",
            bank = account.bank,
            accountNumber = accountNumber,
            type = BankTransactionType.WITHDRAWAL,
            amount = amount,
            balanceAfter = updated.balance,
            status = BankTransactionStatus.COMPLETED,
            description = "Cash withdrawal"
        )
        bankTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Transfer between bank accounts
    suspend fun transferBetweenBanks(fromAccount: String, toAccount: String, amount: Long): BankTransaction {
        val from = bankAccountDao.getAccount(fromAccount)
            ?: throw IllegalStateException("Source account not found")
        val to = bankAccountDao.getAccount(toAccount)
            ?: throw IllegalStateException("Destination account not found")

        if (from.balance < amount) throw IllegalStateException("Insufficient funds")

        val updatedFrom = from.copy(balance = from.balance - amount)
        bankAccountDao.insertAccount(updatedFrom)

        val updatedTo = to.copy(balance = to.balance + amount)
        bankAccountDao.insertAccount(updatedTo)

        val transaction = BankTransaction(
            id = "BANK${System.currentTimeMillis()}",
            bank = from.bank,
            accountNumber = fromAccount,
            type = BankTransactionType.TRANSFER_OUT,
            amount = amount,
            balanceAfter = updatedFrom.balance,
            status = BankTransactionStatus.COMPLETED,
            description = "Transfer to ${to.bank.displayName} (${toAccount})"
        )
        bankTransactionDao.insertTransaction(transaction)

        return transaction
    }

    // Get bank transaction history
    fun getBankTransactions(account: String): Flow<List<BankTransaction>> =
        bankTransactionDao.getTransactionsForAccount(account)

    // Apply interest to all bank accounts (called daily)
    suspend fun applyBankInterest() {
        val accounts = bankAccountDao.getAllAccounts().first()
        for (account in accounts) {
            if (account.accountType == BankAccountType.SAVINGS || account.accountType == BankAccountType.FIXED_DEPOSIT) {
                val dailyInterest = (account.balance * account.accountType.interestRate / 100 / 365).toLong()
                if (dailyInterest > 0) {
                    val updated = account.copy(
                        balance = account.balance + dailyInterest,
                        interestAccrued = account.interestAccrued + dailyInterest,
                        updatedAt = System.currentTimeMillis()
                    )
                    bankAccountDao.insertAccount(updated)
                }
            }
        }
    }

    // ==================== CENTRAL BANK (CBK) ====================

    // Get Central Bank state
    suspend fun getCentralBankState(): CentralBankState {
        val existing = centralBankDao.getState()
        return existing ?: CentralBankState(
            moneySupplyM2 = calculateTotalMoneySupply()
        )
    }

    // Update Central Bank rates (called periodically)
    suspend fun updateCentralBank() {
        val current = centralBankDao.getState() ?: CentralBankState()
        val totalM2 = calculateTotalMoneySupply()

        // CBK adjusts rates based on money supply and inflation
        val newRate = when {
            totalM2 > current.moneySupplyCap * 0.8 -> current.centralBankRate + 0.5  // Too much money, raise rates
            totalM2 < current.moneySupplyCap * 0.3 -> current.centralBankRate - 0.5  // Too little, lower rates
            else -> current.centralBankRate
        }

        centralBankDao.insertState(current.copy(
            centralBankRate = newRate,
            moneySupplyM2 = totalM2,
            lastUpdated = System.currentTimeMillis()
        ))
    }

    // Calculate total money in circulation (M2)
    suspend fun calculateTotalMoneySupply(): Long {
        val mobileAccounts = mobileMoneyAccountDao.getAllAccounts().first()
        val bankAccounts = bankAccountDao.getAllAccounts().first()
        val npcs = npcPlayerDao.getCpuNpcs().first()

        val mobileTotal = mobileAccounts.sumOf { it.balance }
        val bankTotal = bankAccounts.sumOf { it.balance }
        val npcTotal = npcs.sumOf { it.mpesaBalance + it.bankBalance }

        return mobileTotal + bankTotal + npcTotal
    }

    // ==================== NPC SYSTEM ====================

    // Get all CPU NPCs
    fun getCpuNpcs(): Flow<List<NpcPlayer>> = npcPlayerDao.getCpuNpcs()

    // Send money to an NPC
    suspend fun sendMoneyToNpc(fromPhone: String, npcId: String, amount: Long, pin: String): MobileMoneyTransaction {
        val npc = npcPlayerDao.getNpc(npcId)
            ?: throw IllegalStateException("NPC not found")

        return sendMoney(fromPhone, npc.phoneNumber!!, amount, pin)
    }

    // NPC sends money to player (simulated business transaction)
    suspend fun npcSendMoneyToPlayer(npcId: String, toPhone: String, amount: Long, reason: String): MobileMoneyTransaction {
        val npc = npcPlayerDao.getNpc(npcId)
            ?: throw IllegalStateException("NPC not found")

        if (npc.mpesaBalance < amount) {
            throw IllegalStateException("${npc.name} doesn't have enough money")
        }

        val updatedNpc = npc.copy(
            mpesaBalance = npc.mpesaBalance - amount,
            totalWealth = npc.totalWealth - amount,
            updatedAt = System.currentTimeMillis()
        )
        npcPlayerDao.insertNpc(updatedNpc)

        val transaction = MobileMoneyTransaction(
            id = "${npc.phoneNumber?.substring(0, 3) ?: "254"}${System.currentTimeMillis()}",
            provider = MobileMoneyProvider.MPESA,
            type = MobileMoneyTransactionType.RECEIVE,
            fromPhone = npc.phoneNumber!!,
            toPhone = toPhone,
            amount = amount,
            fee = 0,
            status = MobileMoneyTransactionStatus.SUCCESSFUL,
            description = "${npc.name}: $reason",
            reference = "MPESA${System.currentTimeMillis()}"
        )
        mobileMoneyTransactionDao.insertTransaction(transaction)

        // Add to player's M-Pesa
        val playerAccount = mobileMoneyAccountDao.getAccount(toPhone)
        if (playerAccount != null) {
            mobileMoneyAccountDao.insertAccount(playerAccount.copy(
                balance = playerAccount.balance + amount,
                updatedAt = System.currentTimeMillis()
            ))
        }

        return transaction
    }

    // Promote NPC to player-owned (when enough real users join)
    suspend fun promoteNpcToPlayer(npcId: String, playerId: String) {
        val npc = npcPlayerDao.getNpc(npcId)
            ?: throw IllegalStateException("NPC not found")

        npcPlayerDao.insertNpc(npc.copy(
            playerType = PlayerType.PLAYER_OWNED_NPC,
            ownerPlayerId = playerId,
            updatedAt = System.currentTimeMillis()
        ))
    }

    // ==================== HELPER METHODS ====================

    private fun calculateMpesaFee(amount: Long): Long {
        return when {
            amount <= 500 -> 0L
            amount <= 1000 -> 10L
            amount <= 2500 -> 20L
            amount <= 5000 -> 30L
            amount <= 10000 -> 50L
            amount <= 20000 -> 100L
            else -> 150L
        }
    }

    private fun calculateWithdrawalFee(amount: Long): Long {
        return when {
            amount <= 2500 -> 25L
            amount <= 5000 -> 50L
            amount <= 10000 -> 100L
            else -> 150L
        }
    }

    private fun resetDailyIfNeeded(account: MobileMoneyAccount): MobileMoneyAccount {
        val now = System.currentTimeMillis()
        val daysSinceReset = (now - account.lastResetDate) / 86400000L
        return if (daysSinceReset >= 1) {
            account.copy(dailySpent = 0, lastResetDate = now)
        } else account
    }

    private fun generateBankAccountNumber(bank: KenyanBank): String {
        return "${bank.shortName.substring(0, 2).uppercase()}${random.nextInt(100000, 999999)}"
    }

    private fun updateNpcWealthIfNeeded(phone: String, mpesaChange: Long, bankChange: Long) {
        // This would be called after transactions to update NPC wealth
        // For now, we rely on the NPC's own mpesaBalance/bankBalance being updated
        // in sendMoneyToNpc and npcSendMoneyToPlayer
    }
}