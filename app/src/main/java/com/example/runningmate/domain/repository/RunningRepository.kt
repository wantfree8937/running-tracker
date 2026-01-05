package com.example.runningmate.domain.repository

import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.model.RunningPath
import kotlinx.coroutines.flow.Flow

// [Location]: domain/repository/RunningRepository.kt
interface RunningRepository {
    suspend fun insertRun(run: RunEntity)
    fun getAllRuns(): Flow<List<RunEntity>>
    fun getTotalTimeInMillis(): Flow<Long>
    fun getTotalCaloriesBurned(): Flow<Int>
    fun getTotalDistance(): Flow<Int>
    fun getTotalAvgSpeed(): Flow<Float>
}
