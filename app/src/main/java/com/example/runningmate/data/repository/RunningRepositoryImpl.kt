package com.example.runningmate.data.repository

import com.example.runningmate.core.database.RunningDao
import com.example.runningmate.data.dto.CurrentRunEntity
import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.repository.RunningRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RunningRepositoryImpl @Inject constructor(
    private val runningDao: RunningDao
) : RunningRepository {

    override suspend fun insertRun(run: RunEntity) {
        runningDao.insertRun(run)
    }

    override suspend fun deleteRun(run: RunEntity) {
        runningDao.deleteRun(run)
    }

    override fun getAllRuns(): Flow<List<RunEntity>> {
        return runningDao.getAllRuns()
    }

    override fun getTotalTimeInMillis(): Flow<Long> {
        return runningDao.getTotalTimeInMillis()
    }

    override fun getTotalCaloriesBurned(): Flow<Int> {
        return runningDao.getTotalCaloriesBurned()
    }

    override fun getTotalDistance(): Flow<Int> {
        return runningDao.getTotalDistance()
    }

    override fun getTotalAvgSpeed(): Flow<Float> {
        return runningDao.getTotalAvgSpeed()
    }

    override suspend fun insertCurrentRun(run: CurrentRunEntity) {
        runningDao.insertCurrentRun(run)
    }

    override suspend fun deleteCurrentRun() {
        runningDao.deleteCurrentRun()
    }

    override suspend fun getCurrentRun(): CurrentRunEntity? {
        return runningDao.getCurrentRun()
    }
}