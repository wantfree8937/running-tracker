package com.example.runningmate.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.runningmate.R
import com.example.runningmate.data.dto.CurrentRunEntity
import com.example.runningmate.data.source.LocationDataSource
import com.example.runningmate.domain.repository.RunningRepository
import com.example.runningmate.presentation.MainActivity
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RunningService : Service() {

    @Inject
    lateinit var repository: RunningRepository

    @Inject
    lateinit var locationDataSource: LocationDataSource

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private var startTime = 0L
    private var isFirstRun = true
    private var isKilled = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            // System restarted the service
            restoreState()
        } else {
            when (intent.action) {
                ACTION_START -> startService()
                ACTION_STOP -> stopService()
            }
        }
        return START_STICKY
    }

    private fun startService() {
        startForegroundService()
        if (isFirstRun) {
            isFirstRun = false
            startTime = System.currentTimeMillis()
            
            serviceScope.launch {
                locationDataSource.startTracking()
            }

            // Observe location updates to save state
            locationDataSource.pathPointsFlow
                .onEach { pathPoints ->
                    saveCurrentRun(pathPoints)
                    updateNotification(pathPoints)
                }
                .launchIn(serviceScope)
        }
    }

    private fun restoreState() {
        serviceScope.launch {
            val currentRun = repository.getCurrentRun()
            if (currentRun != null) {
                startTime = currentRun.startTime
                locationDataSource.restoreState(currentRun.pathPoints)
                startService() // Resume tracking
            } else {
                stopSelf() // No active run to restore
            }
        }
    }

    private fun saveCurrentRun(pathPoints: List<List<LatLng>>) {
        if (pathPoints.isEmpty()) return
        
        val duration = System.currentTimeMillis() - startTime
        val distance = 0 // Calculate distance ideally
        // Simplified saving for now, assuming external calculation for distance/calories if needed
        // Or we can inject a calculator. For now, we save the critical pathPoints path.
        
        val run = CurrentRunEntity(
            startTime = startTime,
            durationInMillis = duration,
            pathPoints = pathPoints
        )
        serviceScope.launch {
            repository.insertCurrentRun(run)
        }
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running Tracker",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = createNotification("Tracking Running...")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(pathPoints: List<List<LatLng>>) {
         val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
         val notification = createNotification("Points: ${pathPoints.flatten().size}")
         notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Running Mate")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher) // Verify icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun stopService() {
        isFirstRun = true
        isKilled = true
        serviceScope.launch {
            repository.deleteCurrentRun()
            locationDataSource.stopTracking()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val CHANNEL_ID = "running_channel"
        const val NOTIFICATION_ID = 1
    }
}
