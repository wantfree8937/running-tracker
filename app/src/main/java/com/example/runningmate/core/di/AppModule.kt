package com.example.runningmate.core.di

import android.app.Application
import androidx.room.Room
import com.example.runningmate.core.database.RunningDatabase
import com.example.runningmate.data.repository.RunningRepositoryImpl
import com.example.runningmate.domain.repository.RunningRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// [Location]: core/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(app: Application): RunningDatabase {
        return Room.databaseBuilder(
            app,
            RunningDatabase::class.java,
            "running_db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideRunningRepository(db: RunningDatabase): RunningRepository {
        return RunningRepositoryImpl(db.runningDao)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(app: Application): com.example.runningmate.domain.repository.DeviceRepository {
        return com.example.runningmate.data.repository.DeviceRepositoryImpl(app)
    }
}
