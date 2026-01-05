package com.example.runningmate.data.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.runningmate.data.source.LocationDataSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect

@HiltWorker
class RunningWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationDataSource: LocationDataSource
) : CoroutineWorker(context, workerParams) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).apply {
        setMinUpdateIntervalMillis(1000L)
    }.build()

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        
        val locationTracking = callbackFlow<LatLng> {
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.forEach { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        trySend(latLng)
                    }
                }
            }
            
            try {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
                }
            } catch (e: SecurityException) {
                close(e)
            }
            
            awaitClose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }

        locationTracking.collect { latLng ->
            locationDataSource.emitLocation(latLng)
            locationDataSource.addPathPoint(latLng)
        }

        return Result.success()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "running_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Running Notification", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Running Tracker Active")
            .setContentText("Tracking your run...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
            
        // Use type 8 (FOREGROUND_SERVICE_TYPE_LOCATION) if API >= 30, but the const is standard now.
        // In 34 we need to specify. 
        // 8 is location type.
        return if (Build.VERSION.SDK_INT >= 29) {
             ForegroundInfo(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            ForegroundInfo(1, notification)
        }
    }
}
