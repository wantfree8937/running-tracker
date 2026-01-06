package com.example.runningmate.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

// [Location]: data/dto/RunEntity.kt
@Entity(tableName = "running_table")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = 0L,
    val avgSpeedKmh: Float = 0f,
    val distanceMeters: Int = 0,
    val timeInMillis: Long = 0L,
    val caloriesBurned: Int = 0,
    val pathPoints: List<List<LatLng>> = emptyList() // Needs TypeConverter
)
