package com.example.runningmate.domain.use_case

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.runningmate.data.source.LocationDataSource
import com.example.runningmate.data.worker.RunningWorker
import com.example.runningmate.domain.repository.RunningRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartRunningUseCase @Inject constructor(
    private val repository: RunningRepository,
    private val locationDataSource: LocationDataSource,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() {
        locationDataSource.clearPathPoints()
        locationDataSource.startTracking()
        val workRequest = OneTimeWorkRequestBuilder<RunningWorker>()
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "RunningTracking",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}