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
    private var locationJob: Job? = null
    private var sharedLocationJob: Job? = null
    private var lastLocation: LatLng? = null

    init {
        startObservingSharedLocation()
    }

    override fun handleIntent(intent: RunningIntent) {
        when (intent) {
            is RunningIntent.PermissionGranted -> {
                startObservingLocation()
            }
            is RunningIntent.StartRunning -> startRunning()
            is RunningIntent.PauseRunning -> pauseRunning()
            is RunningIntent.StopRunning -> stopRunning()
            is RunningIntent.ToggleRun -> {
                if (currentState.isRunning) pauseRunning() else startRunning()
            }
        }
    }

    private fun startObservingSharedLocation() {
        if (sharedLocationJob?.isActive == true) return
        
        sharedLocationJob = viewModelScope.launch {
            // Collect the entire path list regardless of isRunning state
            observeLocationUseCase.pathPointsFlow.collect { path ->
                if (path.isNotEmpty()) {
                    setState { 
                        copy(
                            pathPoints = path,
                            currentLocation = path.lastOrNull() ?: currentLocation,
                            distanceMeters = calculateTotalDistance(path)
                        )
                    }
                }
            }
        }
    }

    private fun calculateTotalDistance(path: List<LatLng>): Float {
        var total = 0f
        for (i in 0 until path.size - 1) {
            total += calculateDistance(path[i], path[i + 1])
        }
        return total
    }

    private fun startObservingLocation() {
        if (locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            try {
                android.util.Log.d("RunningViewModel", "Starting location observation")
                observeLocationUseCase()
                    .collect { location ->
                        setState { copy(currentLocation = location) }
                    }
            } catch (e: Exception) {
                android.util.Log.e("RunningViewModel", "Error observing location", e)
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
            setState { copy(isRunning = true, isRunActive = true) }
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
            setState { copy(isRunning = false, isRunActive = false, pathPoints = emptyList(), durationMillis = 0L, distanceMeters = 0f) }
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

