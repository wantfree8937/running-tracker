package com.example.runningmate.core.di

import android.app.Application
import com.example.runningmate.data.source.DefaultLocationDataSource
import com.example.runningmate.data.source.LocationDataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(app)
    }

    @Provides
    @Singleton
    fun provideLocationDataSource(client: FusedLocationProviderClient): LocationDataSource {
        return DefaultLocationDataSource(client)
    }
}
