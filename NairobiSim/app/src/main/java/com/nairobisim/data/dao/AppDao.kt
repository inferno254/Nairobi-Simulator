package com.nairobisim.data.dao

import androidx.room.*
import com.nairobisim.data.converters.Converters
import com.nairobisim.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile WHERE id = :id")
    fun getProfile(id: String): Flow<PlayerProfile?>
    
    @Query("SELECT * FROM player_profile WHERE id = :id")
    suspend fun getProfileOnce(id: String): PlayerProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PlayerProfile)
    
    @Update
    suspend fun updateProfile(profile: PlayerProfile)
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM business WHERE isOperational = 1")
    fun getActiveBusinesses(): Flow<List<Business>>

    @Query("SELECT * FROM business WHERE isOperational = 1")
    suspend fun getActiveBusinessesOnce(): List<Business>
    
    @Query("SELECT * FROM business")
    fun getAllBusinesses(): Flow<List<Business>>
    
    @Query("SELECT * FROM business WHERE id = :id")
    suspend fun getBusiness(id: String): Business?
    
    @Query("SELECT * FROM business WHERE category = :category")
    fun getBusinessesByCategory(category: BusinessCategory): Flow<List<Business>>
    
    @Query("SELECT * FROM business WHERE location = :location")
    fun getBusinessesByLocation(location: NairobiArea): Flow<List<Business>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<Business>)
    
    @Update
    suspend fun updateBusiness(business: Business)
    
    @Update
    suspend fun updateBusinesses(businesses: List<Business>)
    
    @Delete
    suspend fun deleteBusiness(business: Business)
    
    @Query("DELETE FROM business WHERE id = :id")
    suspend fun deleteBusinessById(id: String)
}

@Dao
@TypeConverters(Converters::class)
interface TransactionDao {
    @Query("SELECT * FROM daily_transaction ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<DailyTransaction>>
    
    @Query("SELECT * FROM daily_transaction WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsInRange(startDate: Date, endDate: Date): Flow<List<DailyTransaction>>
    
    @Query("SELECT * FROM daily_transaction WHERE businessId = :businessId ORDER BY date DESC LIMIT :limit")
    fun getBusinessTransactions(businessId: String, limit: Int): Flow<List<DailyTransaction>>
    
    @Query("SELECT SUM(amount) FROM daily_transaction WHERE type = 'INCOME' AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalIncome(startDate: Date, endDate: Date): Long?
    
    @Query("SELECT SUM(amount) FROM daily_transaction WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalExpenses(startDate: Date, endDate: Date): Long?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: DailyTransaction)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactions(transactions: List<DailyTransaction>)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loan WHERE isActive = 1")
    fun getActiveLoans(): Flow<List<Loan>>
    
    @Query("SELECT * FROM loan")
    fun getAllLoans(): Flow<List<Loan>>
    
    @Query("SELECT * FROM loan WHERE id = :id")
    suspend fun getLoan(id: String): Loan?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan)
    
    @Update
    suspend fun updateLoan(loan: Loan)
    
    @Query("SELECT SUM(outstandingBalance) FROM loan WHERE isActive = 1")
    suspend fun getTotalDebt(): Long?
}

@Dao
interface ChamaDao {
    @Query("SELECT * FROM chama_group WHERE isActive = 1")
    fun getActiveChamas(): Flow<List<ChamaGroup>>
    
    @Query("SELECT * FROM chama_group WHERE id = :id")
    suspend fun getChama(id: String): ChamaGroup?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChama(chama: ChamaGroup)
    
    @Update
    suspend fun updateChama(chama: ChamaGroup)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM business WHERE id = :businessId")
    suspend fun getBusinessForEmployees(businessId: String): Business?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business)
    
    @Update
    suspend fun updateBusiness(business: Business)
}

@Dao
@TypeConverters(Converters::class)
interface MarketEventDao {
    @Query("SELECT * FROM market_event WHERE startDate <= :date AND endDate >= :date")
    fun getActiveEvents(date: Date): Flow<List<MarketEvent>>
    
    @Query("SELECT * FROM market_event WHERE startDate >= :date ORDER BY startDate ASC LIMIT :limit")
    fun getUpcomingEvents(date: Date, limit: Int): Flow<List<MarketEvent>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MarketEvent)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<MarketEvent>)
}

@Dao
interface SimulationStateDao {
    @Query("SELECT * FROM simulation_state WHERE id = 'global'")
    fun getState(): Flow<SimulationState?>
    
    @Query("SELECT * FROM simulation_state WHERE id = 'global'")
    suspend fun getStateOnce(): SimulationState?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(state: SimulationState)
    
    @Update
    suspend fun updateState(state: SimulationState)
}

@Dao
@TypeConverters(Converters::class)
interface ForexTradeDao {
    @Query("SELECT * FROM forex_trade WHERE symbol = :symbol ORDER BY openTime DESC")
    fun getTradesBySymbol(symbol: String): Flow<List<ForexTrade>>
    
    @Query("SELECT * FROM forex_trade WHERE isClosed = 0 ORDER BY openTime DESC")
    fun getOpenTrades(): Flow<List<ForexTrade>>
    
    @Query("SELECT * FROM forex_trade ORDER BY openTime DESC LIMIT :limit")
    fun getRecentTrades(limit: Int): Flow<List<ForexTrade>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: ForexTrade)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<ForexTrade>)
    
    @Update
    suspend fun updateTrade(trade: ForexTrade)
}

@Dao
@TypeConverters(Converters::class)
interface ShareTradeDao {
    @Query("SELECT * FROM share_trade WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getTradesBySymbol(symbol: String): Flow<List<ShareTrade>>
    
    @Query("SELECT * FROM share_trade ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTrades(limit: Int): Flow<List<ShareTrade>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: ShareTrade)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<ShareTrade>)
}

@Dao
@TypeConverters(Converters::class)
interface SharpBoyEncounterDao {
    @Query("SELECT * FROM sharp_boy_encounter ORDER BY timestamp DESC")
    fun getAllEncounters(): Flow<List<SharpBoyEncounter>>
    
    @Query("SELECT * FROM sharp_boy_encounter WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getUnresolvedEncounters(): Flow<List<SharpBoyEncounter>>
    
    @Query("SELECT * FROM sharp_boy_encounter WHERE id = :id")
    suspend fun getEncounter(id: String): SharpBoyEncounter?
    
    @Query("SELECT COUNT(*) FROM sharp_boy_encounter")
    suspend fun getCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncounter(encounter: SharpBoyEncounter)
    
    @Update
    suspend fun updateEncounter(encounter: SharpBoyEncounter)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE playerId = :playerId")
    suspend fun getUserProfile(playerId: String): UserProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
    
    @Update
    suspend fun updateUserProfile(profile: UserProfile)
}

@Dao
@TypeConverters(Converters::class)
interface FreelanceContractDao {
    @Query("SELECT * FROM freelance_contract ORDER BY startedAt DESC")
    fun getAllContracts(): Flow<List<FreelanceContract>>
    
    @Query("SELECT * FROM freelance_contract WHERE status != 'PAID' AND status != 'FAILED' ORDER BY startedAt DESC")
    fun getActiveContracts(): Flow<List<FreelanceContract>>
    
    @Query("SELECT * FROM freelance_contract WHERE status = 'DELAYED' ORDER BY startedAt DESC")
    fun getDelayedContracts(): Flow<List<FreelanceContract>>
    
    @Query("SELECT * FROM freelance_contract WHERE id = :id")
    suspend fun getContractOnce(id: String): FreelanceContract?
    
    @Query("SELECT * FROM freelance_contract")
    suspend fun getAllContractsOnce(): List<FreelanceContract>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: FreelanceContract)
    
    @Update
    suspend fun updateContract(contract: FreelanceContract)
}

@Dao
@TypeConverters(Converters::class)
interface SmartPersonDao {
    @Query("SELECT * FROM smart_person")
    fun getAllPeople(): Flow<List<SmartPerson>>
    
    @Query("SELECT * FROM smart_person WHERE id = :id")
    suspend fun getPerson(id: String): SmartPerson?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: SmartPerson)
    
    @Update
    suspend fun updatePerson(person: SmartPerson)
}

@Dao
@TypeConverters(Converters::class)
interface CounselingSessionDao {
    @Query("SELECT * FROM counseling_session WHERE personId = :personId ORDER BY createdAt DESC")
    fun getSessionsForPerson(personId: String): Flow<List<CounselingSession>>
    
    @Query("SELECT * FROM counseling_session WHERE id = :id")
    suspend fun getSession(id: String): CounselingSession?
    
    @Query("SELECT * FROM counseling_session")
    suspend fun getAllSessionsOnce(): List<CounselingSession>

    @Query("SELECT * FROM counseling_session ORDER BY createdAt DESC")
    fun getAllSessionsFlow(): Flow<List<CounselingSession>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CounselingSession)
    
    @Update
    suspend fun updateSession(session: CounselingSession)
}

@Dao
@TypeConverters(Converters::class)
interface BettingMatchDao {
    @Query("SELECT * FROM betting_match WHERE isFinished = 0 ORDER BY matchTime ASC")
    fun getUpcomingMatches(): Flow<List<BettingMatch>>

    @Query("SELECT * FROM betting_match WHERE isFinished = 0 ORDER BY matchTime ASC")
    suspend fun getUpcomingMatchesOnce(): List<BettingMatch>
    
    @Query("SELECT * FROM betting_match WHERE isFinished = 1 ORDER BY matchTime DESC")
    fun getFinishedMatches(): Flow<List<BettingMatch>>
    
    @Query("SELECT * FROM betting_match WHERE id = :id")
    suspend fun getMatch(id: String): BettingMatch?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: BettingMatch)
    
    @Update
    suspend fun updateMatch(match: BettingMatch)
}

@Dao
@TypeConverters(Converters::class)
interface BetSlipDao {
    @Query("SELECT * FROM bet_slip ORDER BY placedAt DESC")
    fun getAllBets(): Flow<List<BetSlip>>

    @Query("SELECT * FROM bet_slip ORDER BY placedAt DESC")
    suspend fun getAllBetsOnce(): List<BetSlip>
    
    @Query("SELECT * FROM bet_slip WHERE status = 'PENDING' ORDER BY placedAt DESC")
    fun getPendingBets(): Flow<List<BetSlip>>
    
    @Query("SELECT * FROM bet_slip WHERE id = :id")
    suspend fun getBet(id: String): BetSlip?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(slip: BetSlip)
    
    @Update
    suspend fun updateBet(slip: BetSlip)
}

@Dao
@TypeConverters(Converters::class)
interface BettingBalanceDao {
    @Query("SELECT * FROM betting_balance WHERE id = 'betting_wallet'")
    suspend fun getBalance(): BettingBalance?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: BettingBalance)
    
    @Update
    suspend fun updateBalance(balance: BettingBalance)
}

@Dao
@TypeConverters(Converters::class)
interface PonziSchemeDao {
    @Query("SELECT * FROM ponzi_scheme ORDER BY createdAt DESC")
    fun getAllSchemes(): Flow<List<PonziScheme>>

    @Query("SELECT * FROM ponzi_scheme")
    suspend fun getAllSchemesOnce(): List<PonziScheme>
    
    @Query("SELECT * FROM ponzi_scheme WHERE id = :id")
    suspend fun getScheme(id: String): PonziScheme?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheme(scheme: PonziScheme)
    
    @Update
    suspend fun updateScheme(scheme: PonziScheme)
}

@Dao
@TypeConverters(Converters::class)
interface PonziInvestmentDao {
    @Query("SELECT * FROM ponzi_investment ORDER BY investedAt DESC")
    fun getAllInvestments(): Flow<List<PonziInvestment>>
    
    @Query("SELECT * FROM ponzi_investment WHERE schemeId = :schemeId ORDER BY investedAt DESC")
    fun getInvestmentsByScheme(schemeId: String): Flow<List<PonziInvestment>>

    @Query("SELECT * FROM ponzi_investment WHERE schemeId = :schemeId")
    suspend fun getInvestmentsBySchemeOnce(schemeId: String): List<PonziInvestment>
    
    @Query("SELECT * FROM ponzi_investment WHERE id = :id")
    suspend fun getInvestment(id: String): PonziInvestment?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: PonziInvestment)
    
    @Update
    suspend fun updateInvestment(investment: PonziInvestment)
}

// ==================== BANKING DAOs ====================

@Dao
@TypeConverters(Converters::class)
interface MobileMoneyAccountDao {
    @Query("SELECT * FROM mobile_money_account WHERE phoneNumber = :phone")
    suspend fun getAccount(phone: String): MobileMoneyAccount?
    
    @Query("SELECT * FROM mobile_money_account WHERE isRegistered = 1")
    fun getAllAccounts(): Flow<List<MobileMoneyAccount>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: MobileMoneyAccount)
    
    @Update
    suspend fun updateAccount(account: MobileMoneyAccount)
}

@Dao
@TypeConverters(Converters::class)
interface MobileMoneyTransactionDao {
    @Query("SELECT * FROM mobile_money_transaction WHERE fromPhone = :phone ORDER BY timestamp DESC")
    fun getTransactionsForPhone(phone: String): Flow<List<MobileMoneyTransaction>>
    
    @Query("SELECT * FROM mobile_money_transaction ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<MobileMoneyTransaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MobileMoneyTransaction)
    
    @Update
    suspend fun updateTransaction(transaction: MobileMoneyTransaction)
}

@Dao
@TypeConverters(Converters::class)
interface BankAccountDao {
    @Query("SELECT * FROM bank_account WHERE accountNumber = :account")
    suspend fun getAccount(account: String): BankAccount?
    
    @Query("SELECT * FROM bank_account WHERE accountName = :name")
    suspend fun getAccountsByName(name: String): List<BankAccount>
    
    @Query("SELECT * FROM bank_account WHERE isActive = 1")
    fun getAllAccounts(): Flow<List<BankAccount>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccount)
    
    @Update
    suspend fun updateAccount(account: BankAccount)
}

@Dao
@TypeConverters(Converters::class)
interface BankTransactionDao {
    @Query("SELECT * FROM bank_transaction WHERE accountNumber = :account ORDER BY timestamp DESC")
    fun getTransactionsForAccount(account: String): Flow<List<BankTransaction>>
    
    @Query("SELECT * FROM bank_transaction ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<BankTransaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BankTransaction)
    
    @Update
    suspend fun updateTransaction(transaction: BankTransaction)
}

@Dao
@TypeConverters(Converters::class)
interface CentralBankDao {
    @Query("SELECT * FROM central_bank_state WHERE id = 'cbk_main'")
    suspend fun getState(): CentralBankState?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertState(state: CentralBankState)
    
    @Update
    suspend fun updateState(state: CentralBankState)
}

@Dao
@TypeConverters(Converters::class)
interface NpcPlayerDao {
    @Query("SELECT * FROM npc_player WHERE playerType = 'CPU_NPC'")
    fun getCpuNpcs(): Flow<List<NpcPlayer>>
    
    @Query("SELECT * FROM npc_player WHERE id = :id")
    suspend fun getNpc(id: String): NpcPlayer?
    
    @Query("SELECT * FROM npc_player WHERE phoneNumber = :phone")
    suspend fun getNpcByPhone(phone: String): NpcPlayer?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpc(npc: NpcPlayer)

    @Update
    suspend fun updateNpc(npc: NpcPlayer)
}

@Dao
@TypeConverters(Converters::class)
interface TaxRecordDao {
    @Query("SELECT * FROM tax_record ORDER BY createdAt DESC")
    fun getAllRecords(): Flow<List<TaxRecord>>

    @Query("SELECT * FROM tax_record ORDER BY createdAt DESC")
    suspend fun getAllRecordsOnce(): List<TaxRecord>

    @Query("SELECT * FROM tax_record WHERE period = :period")
    suspend fun getRecordsForPeriod(period: String): List<TaxRecord>

    @Query("SELECT * FROM tax_record WHERE id = :id")
    suspend fun getRecord(id: String): TaxRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TaxRecord)

    @Update
    suspend fun updateRecord(record: TaxRecord)
}

@Dao
@TypeConverters(Converters::class)
interface ElectionDao {
    @Query("SELECT * FROM election ORDER BY createdAt DESC")
    fun getAllElections(): Flow<List<Election>>

    @Query("SELECT * FROM election WHERE id = :id")
    suspend fun getElection(id: String): Election?

    @Query("SELECT * FROM election WHERE stage != 'CONCLUDED' ORDER BY createdAt DESC")
    fun getActiveElections(): Flow<List<Election>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: Election)

    @Update
    suspend fun updateElection(election: Election)
}