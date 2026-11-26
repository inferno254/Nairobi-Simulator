package com.nairobisim.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nairobisim.data.converters.Converters
import com.nairobisim.data.dao.*
import com.nairobisim.model.*

@Database(
    entities = [
        PlayerProfile::class,
        Business::class,
        DailyTransaction::class,
        Loan::class,
        ChamaGroup::class,
        MarketEvent::class,
        SimulationState::class,
        ForexTrade::class,
        ShareTrade::class,
        UserProfile::class,
        SharpBoyEncounter::class,
        PlayerEconomicState::class,
        FreelanceContract::class,
        SmartPerson::class,
        CounselingSession::class,
        BettingMatch::class,
        BetSlip::class,
        BettingBalance::class,
        PonziScheme::class,
        PonziInvestment::class,
        MobileMoneyAccount::class,
        MobileMoneyTransaction::class,
        BankAccount::class,
        BankTransaction::class,
        CentralBankState::class,
        NpcPlayer::class,
        TaxRecord::class,
        Election::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun businessDao(): BusinessDao
    abstract fun transactionDao(): TransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun chamaDao(): ChamaDao
    abstract fun marketEventDao(): MarketEventDao
    abstract fun simulationStateDao(): SimulationStateDao
    abstract fun forexTradeDao(): ForexTradeDao
    abstract fun shareTradeDao(): ShareTradeDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun sharpBoyEncounterDao(): SharpBoyEncounterDao
    
    abstract fun freelanceContractDao(): FreelanceContractDao
    
    abstract fun smartPersonDao(): SmartPersonDao
    
    abstract fun counselingSessionDao(): CounselingSessionDao
    
    abstract fun bettingMatchDao(): BettingMatchDao
    
    abstract fun betSlipDao(): BetSlipDao
    
    abstract fun bettingBalanceDao(): BettingBalanceDao
    
    abstract fun ponziSchemeDao(): PonziSchemeDao
    
    abstract fun ponziInvestmentDao(): PonziInvestmentDao
    
    abstract fun mobileMoneyAccountDao(): MobileMoneyAccountDao
    
    abstract fun mobileMoneyTransactionDao(): MobileMoneyTransactionDao
    
    abstract fun bankAccountDao(): BankAccountDao
    
    abstract fun bankTransactionDao(): BankTransactionDao
    
    abstract fun centralBankDao(): CentralBankDao
    
    abstract fun npcPlayerDao(): NpcPlayerDao

    abstract fun taxRecordDao(): TaxRecordDao

    abstract fun electionDao(): ElectionDao
    
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nairobi_sim_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}