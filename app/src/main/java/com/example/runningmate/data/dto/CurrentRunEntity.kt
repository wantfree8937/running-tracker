package com.example.runningmate.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "current_run_table")
data class CurrentRunEntity(
    @PrimaryKey
    val id: Int = 0, // Always 0 as we only have one current run
    val startTime: Long = 0L,
    val durationInMillis: Long = 0L,
    val distanceMeters: Int = 0,
    val caloriesBurned: Int = 0,
    val avgSpeedKmh: Float = 0f,
    val pathPoints: List<List<LatLng>> = emptyList() // Uses existing TypeConverter
)
