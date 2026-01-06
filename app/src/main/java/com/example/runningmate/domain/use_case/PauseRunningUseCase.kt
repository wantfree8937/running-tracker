package com.example.runningmate.domain.use_case

import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import com.example.runningmate.data.source.LocationDataSource

class PauseRunningUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDataSource: LocationDataSource
) {
    suspend operator fun invoke() {
        locationDataSource.stopTracking()
        WorkManager.getInstance(context).cancelUniqueWork("RunningTracking")
    }
}