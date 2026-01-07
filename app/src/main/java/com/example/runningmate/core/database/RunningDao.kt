package com.example.runningmate.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.data.dto.CurrentRunEntity
import kotlinx.coroutines.flow.Flow

// [Location]: core/database/RunningDao.kt
@Dao
interface RunningDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity)

    @androidx.room.Delete
    suspend fun deleteRun(run: RunEntity)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<RunEntity>>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): Flow<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): Flow<Int>

    @Query("SELECT SUM(distanceMeters) FROM running_table")
    fun getTotalDistance(): Flow<Int>

    @Query("SELECT AVG(avgSpeedKmh) FROM running_table")
    fun getTotalAvgSpeed(): Flow<Float>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentRun(run: CurrentRunEntity)

    @Query("DELETE FROM current_run_table")
    suspend fun deleteCurrentRun()

    @Query("SELECT * FROM current_run_table WHERE id = 0")
    suspend fun getCurrentRun(): CurrentRunEntity?
}
