package com.example.runningmate.domain.use_case

import android.content.Context
import androidx.work.WorkManager
import com.example.runningmate.domain.repository.RunningRepository
import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.model.RunningPath
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopRunningUseCase @Inject constructor(
    private val repository: RunningRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(path: List<com.google.android.gms.maps.model.LatLng>, duration: Long, distance: Float) {
        WorkManager.getInstance(context).cancelUniqueWork("RunningTracking")
        
        val entity = RunEntity(
            timestamp = System.currentTimeMillis(),
            timeInMillis = duration,
            distanceMeters = distance.toInt(),
            pathPoints = path,
            avgSpeedKmh = if (duration > 0) (distance / 1000f) / (duration / 3600000f) else 0f
        )
        repository.insertRun(entity)
    }
}