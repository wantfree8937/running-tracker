package com.example.runningmate.presentation.feature.running.contract

import com.example.runningmate.presentation.base.UiEffect
import com.example.runningmate.presentation.base.UiIntent
import com.example.runningmate.presentation.base.UiState
import com.example.runningmate.domain.model.RunningPath
import com.google.android.gms.maps.model.LatLng

// [Location]: presentation/feature/running/contract/RunningContract.kt

data class RunningState(
    val isRunning: Boolean = false,
    val pathPoints: List<LatLng> = emptyList(),
    val durationMillis: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentSpeedKmh: Float = 0f,
    val caloriesBurned: Int = 0,
    val currentLocation: LatLng? = null
) : UiState

sealed class RunningIntent : UiIntent {
    object StartRunning : RunningIntent()
    object PauseRunning : RunningIntent()
    object StopRunning : RunningIntent()
    object ToggleRun : RunningIntent()
    object PermissionGranted : RunningIntent()
}

sealed class RunningEffect : UiEffect {
    object NavigateToSummary : RunningEffect()
    data class ShowToast(val message: String) : RunningEffect()
}
