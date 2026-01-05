package com.example.runningmate.data.source

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

// [Location]: data/source/LocationDataSource.kt
import kotlinx.coroutines.flow.StateFlow

interface LocationDataSource {
    val locationFlow: Flow<LatLng>
    val pathPointsFlow: StateFlow<List<LatLng>>
    
    fun addPathPoint(latLng: LatLng)
    fun clearPathPoints()
    
    fun getLocationUpdates(): Flow<LatLng>
    suspend fun emitLocation(latLng: LatLng)
    suspend fun startTracking()
    suspend fun stopTracking()
}
