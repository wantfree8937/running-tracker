package com.example.runningmate.domain.use_case

import android.content.Context
import android.content.Intent
import com.example.runningmate.data.service.RunningService
import com.example.runningmate.data.source.LocationDataSource
import com.example.runningmate.domain.repository.RunningRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartRunningUseCase @Inject constructor(
    private val repository: RunningRepository,
    private val locationDataSource: LocationDataSource,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(clearData: Boolean = true) {
        if (clearData) {
            locationDataSource.clearPathPoints()
        }
        // locationDataSource.startTracking() is called by Service
        
        val intent = Intent(context, RunningService::class.java).apply {
            action = RunningService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}