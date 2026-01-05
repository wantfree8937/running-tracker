package com.example.runningmate.domain.use_case

import com.example.runningmate.domain.repository.RunningRepository
import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.model.RunningPath
import javax.inject.Inject

// [Location]: domain/use_case/StopRunningUseCase.kt
class StopRunningUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke(path: List<com.google.android.gms.maps.model.LatLng>, duration: Long, distance: Float) {
        // When stopping, we save the run to DB.
        // Map Domain model to Entity
        val entity = RunEntity(
            timestamp = System.currentTimeMillis(),
            timeInMillis = duration,
            distanceMeters = distance.toInt(),
            pathPoints = path,
            avgSpeedKmh = if (duration > 0) (distance / 1000f) / (duration / 3600000f) else 0f
            // Calories calc logic could go here
        )
        repository.insertRun(entity)
    }
}
