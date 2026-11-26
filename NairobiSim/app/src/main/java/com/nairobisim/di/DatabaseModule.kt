package com.nairobisim.di

import android.content.Context
import androidx.room.Room
import com.nairobisim.data.database.AppDatabase
import com.nairobisim.data.repository.SimulationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nairobi_sim_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSimulationRepository(database: AppDatabase): SimulationRepository {
        return SimulationRepository(database)
    }
}