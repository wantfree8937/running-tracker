package com.example.runningmate.data.source

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

// [Location]: data/source/LocationDataSource.kt
interface LocationDataSource {
    fun getLocationUpdates(): Flow<LatLng>
    suspend fun startTracking()
    suspend fun stopTracking()
}
