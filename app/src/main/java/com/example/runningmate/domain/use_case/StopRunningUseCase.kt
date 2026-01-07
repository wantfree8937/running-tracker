package com.example.runningmate.domain.use_case

import android.content.Context
import android.content.Intent
import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.data.service.RunningService
import com.example.runningmate.data.source.LocationDataSource
import com.example.runningmate.domain.repository.RunningRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopRunningUseCase @Inject constructor(
    private val repository: RunningRepository,
    private val locationDataSource: LocationDataSource,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(path: List<List<com.google.android.gms.maps.model.LatLng>>, duration: Long, distance: Float, calories: Int) {
        // Save the finished run
        val entity = RunEntity(
            timestamp = System.currentTimeMillis(),
            timeInMillis = duration,
            distanceMeters = distance.toInt(),
            pathPoints = path,
            avgSpeedKmh = if (duration > 0) (distance / 1000f) / (duration / 3600000f) else 0f,
            caloriesBurned = calories
        )
        repository.insertRun(entity)

        // Stop Service (which handles stopTracking and deleteCurrentRun)
        val intent = Intent(context, RunningService::class.java).apply {
            action = RunningService.ACTION_STOP
        }
        context.startService(intent)
    }
}