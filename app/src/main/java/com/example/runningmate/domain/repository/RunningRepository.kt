package com.example.runningmate.domain.repository

import com.example.runningmate.data.dto.CurrentRunEntity
import com.example.runningmate.data.dto.RunEntity
import kotlinx.coroutines.flow.Flow

interface RunningRepository {
    suspend fun insertRun(run: RunEntity)
    suspend fun deleteRun(run: RunEntity)
    fun getAllRuns(): Flow<List<RunEntity>>
    fun getTotalTimeInMillis(): Flow<Long>
    fun getTotalCaloriesBurned(): Flow<Int>
    fun getTotalDistance(): Flow<Int>
    fun getTotalAvgSpeed(): Flow<Float>
    
    suspend fun insertCurrentRun(run: CurrentRunEntity)
    suspend fun deleteCurrentRun()
    suspend fun getCurrentRun(): CurrentRunEntity?
}