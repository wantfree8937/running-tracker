package com.example.runningmate.domain.model

import com.google.android.gms.maps.model.LatLng
import java.util.Date

// [Location]: domain/model/RunningPath.kt
data class RunningPath(
    val pathPoints: List<LatLng> = emptyList(),
    val durationMillis: Long = 0L,
    val distanceMeters: Float = 0f,
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val caloriesBurned: Int = 0,
    val avgSpeedKmh: Float = 0f
)
