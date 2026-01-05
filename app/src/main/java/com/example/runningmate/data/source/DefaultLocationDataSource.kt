package com.example.runningmate.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLocationDataSource @Inject constructor(
    private val client: FusedLocationProviderClient
) : LocationDataSource {

    private val _locationFlow = MutableSharedFlow<LatLng>(extraBufferCapacity = 1)
    override val locationFlow = _locationFlow.asSharedFlow()

    private val _pathPointsFlow = MutableStateFlow<List<LatLng>>(emptyList())
    override val pathPointsFlow = _pathPointsFlow.asStateFlow()

    override fun addPathPoint(latLng: LatLng) {
        _pathPointsFlow.value = _pathPointsFlow.value + latLng
    }

    override fun clearPathPoints() {
        _pathPointsFlow.value = emptyList()
    }

    override suspend fun emitLocation(latLng: LatLng) {
        _locationFlow.emit(latLng)
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<LatLng> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).apply {
            setMinUpdateIntervalMillis(500L)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    android.util.Log.d("DefaultLocationDataSource", "Location received: ${location.latitude}, ${location.longitude}")
                    val latLng = LatLng(location.latitude, location.longitude)
                    addPathPoint(latLng) // Add to list even in foreground
                    _locationFlow.tryEmit(latLng)
                    trySend(latLng)
                }
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { e ->
                    android.util.Log.e("DefaultLocationDataSource", "Failed to request location updates", e)
                    close(e)
                }
        } catch (e: SecurityException) {
            android.util.Log.e("DefaultLocationDataSource", "SecurityException: Permission not granted", e)
            close(e)
        } catch (e: Exception) {
            android.util.Log.e("DefaultLocationDataSource", "Error requesting location updates", e)
            close(e)
        }

        awaitClose {
            client.removeLocationUpdates(callback)
        }
    }

    override suspend fun startTracking() {
        // No-op if using flow based approach, handled by collection
    }

    override suspend fun stopTracking() {
        // No-op
    }
}
