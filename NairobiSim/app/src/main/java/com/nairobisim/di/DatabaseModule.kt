package com.nairobisim.di

import android.content.Context
import androidx.room.Room
import com.nairobisim.data.dao.BusinessDao
import com.nairobisim.data.dao.ChamaDao
import com.nairobisim.data.dao.LoanDao
import com.nairobisim.data.dao.MarketEventDao
import com.nairobisim.data.dao.PlayerProfileDao
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.SimulationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nairobi_sim_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePlayerProfileDao(database: AppDatabase): PlayerProfileDao = database.playerProfileDao()

    @Provides
    @Singleton
    fun provideBusinessDao(database: AppDatabase): BusinessDao = database.businessDao()

    @Provides
    @Singleton
    fun provideLoanDao(database: AppDatabase): LoanDao = database.loanDao()

    @Provides
    @Singleton
    fun provideChamaDao(database: AppDatabase): ChamaDao = database.chamaDao()

    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): MarketEventDao = database.marketEventDao()

    @Provides
    @Singleton
    fun provideSimulationRepository(
        playerProfileDao: PlayerProfileDao,
        businessDao: BusinessDao,
        loanDao: LoanDao,
        chamaDao: ChamaDao,
        eventDao: MarketEventDao
    ): SimulationRepository {
        return SimulationRepository(
            playerProfileDao,
            businessDao,
            loanDao,
            chamaDao,
            eventDao
        )
    }
}