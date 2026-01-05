package com.example.runningmate.domain.use_case

import android.content.Context
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PauseRunningUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        WorkManager.getInstance(context).cancelUniqueWork("RunningTracking")
    }
}