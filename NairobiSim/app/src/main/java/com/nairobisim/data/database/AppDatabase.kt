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
        SimulationState::class
    ],
    version = 1,
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