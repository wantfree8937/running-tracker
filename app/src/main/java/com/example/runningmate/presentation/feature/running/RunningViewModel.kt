package com.example.runningmate.presentation.feature.running

import androidx.lifecycle.viewModelScope
import com.example.runningmate.presentation.base.BaseViewModel
import com.example.runningmate.presentation.feature.running.contract.RunningEffect
import com.example.runningmate.presentation.feature.running.contract.RunningIntent
import com.example.runningmate.presentation.feature.running.contract.RunningState
import com.example.runningmate.domain.use_case.ObserveLocationUseCase
import com.example.runningmate.domain.use_case.PauseRunningUseCase
import com.example.runningmate.domain.use_case.StartRunningUseCase
import com.example.runningmate.domain.use_case.StopRunningUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// [Location]: presentation/feature/running/RunningViewModel.kt
@HiltViewModel
class RunningViewModel @Inject constructor(
    private val startRunningUseCase: StartRunningUseCase,
    private val pauseRunningUseCase: PauseRunningUseCase,
    private val stopRunningUseCase: StopRunningUseCase,
    private val observeLocationUseCase: ObserveLocationUseCase
) : BaseViewModel<RunningState, RunningIntent, RunningEffect>(RunningState()) {

    private var timerJob: Job? = null
    private var lastLocation: LatLng? = null

    init {
        viewModelScope.launch {
            observeLocationUseCase()
                .collect { location ->
                    setState {
                        if (!isRunning) {
                            copy(currentLocation = location)
                        } else {
                            // Filter jitter (e.g., < 3 meters)
                            val lastLoc = pathPoints.lastOrNull() ?: lastLocation
                            val distance = if (lastLoc != null) {
                                calculateDistance(lastLoc, location)
                            } else 0f

                            if (lastLoc == null || distance >= 3f) {
                                val newPath = pathPoints + location
                                copy(
                                    currentLocation = location,
                                    pathPoints = newPath,
                                    distanceMeters = distanceMeters + distance
                                )
                            } else {
                                this // No change if jitter
                            }
                        }
                    }
                }
        }
    }

    override fun handleIntent(intent: RunningIntent) {
        when (intent) {
            is RunningIntent.StartRunning -> startRunning()
            is RunningIntent.PauseRunning -> pauseRunning()
            is RunningIntent.StopRunning -> stopRunning()
            is RunningIntent.ToggleRun -> {
                if (currentState.isRunning) pauseRunning() else startRunning()
            }
        }
    }

    private fun startRunning() {
        if (timerJob?.isActive == true) return
        
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                setState { copy(durationMillis = durationMillis + 1000L) }
            }
        }
        
        viewModelScope.launch {
            startRunningUseCase()
            setState { copy(isRunning = true) }
        }
    }

    private fun pauseRunning() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            pauseRunningUseCase()
            setState { copy(isRunning = false) }
        }
    }

    private fun stopRunning() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            stopRunningUseCase(
                path = currentState.pathPoints,
                duration = currentState.durationMillis,
                distance = currentState.distanceMeters
            )
            setState { copy(isRunning = false, pathPoints = emptyList(), durationMillis = 0L, distanceMeters = 0f) }
            setEffect(RunningEffect.NavigateToSummary)
        }
    }
    
    // Simple Haversine or Location.distanceBetween wrapper
    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            result
        )
        return result[0]
    }
}

