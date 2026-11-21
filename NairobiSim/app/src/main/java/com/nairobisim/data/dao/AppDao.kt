package com.nairobisim.data.dao

import androidx.room.*
import com.nairobisim.model.*
import kotlinx.coroutines.flow.Flow

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