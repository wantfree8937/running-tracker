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
    
    // For speed calculation
    private var lastLocationTime: Long = 0L
    private var lastDistanceForSpeed: Float = 0f

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
            observeLocationUseCase.pathPointsFlow.collect { paths -> // paths is List<List<LatLng>>
                if (paths.isNotEmpty()) {
                    val currentDistance = calculateTotalDistance(paths)
                    val newSpeed = calculateCurrentSpeed(currentDistance)
                    val newCalories = calculateCalories(currentDistance)
                    
                    val lastPoint = paths.lastOrNull()?.lastOrNull()
                    
                    setState { 
                        copy(
                            pathPoints = paths,
                            currentLocation = lastPoint ?: currentLocation,
                            distanceMeters = currentDistance,
                            currentSpeedKmh = newSpeed,
                            caloriesBurned = newCalories
                        )
                    }
                }
            }
        }
    }

    private fun calculateTotalDistance(paths: List<List<LatLng>>): Float {
        var total = 0f
        for (segment in paths) {
            for (i in 0 until segment.size - 1) {
                total += calculateDistance(segment[i], segment[i + 1])
            }
        }
        return total
    }
    
    private fun calculateCurrentSpeed(currentDistance: Float): Float {
        val now = System.currentTimeMillis()
        if (lastLocationTime == 0L) {
            lastLocationTime = now
            lastDistanceForSpeed = currentDistance
            return 0f
        }
        
        val timeDiff = now - lastLocationTime
        if (timeDiff > 2000) { // Update speed if more than 2 seconds passed (to avoid noise)
            val distanceDiff = currentDistance - lastDistanceForSpeed
            if (distanceDiff < 0) return 0f // Should not happen
            
            // Speed in m/s
            val speedMs = distanceDiff / (timeDiff / 1000f)
            // Speed in km/h
            val speedKmh = speedMs * 3.6f
            
            lastLocationTime = now
            lastDistanceForSpeed = currentDistance
            
            // Filter crazy spikes
            return if (speedKmh > 40f) currentState.currentSpeedKmh else speedKmh
        }
        return currentState.currentSpeedKmh // Keep previous speed
    }

    private fun calculateCalories(distanceMeters: Float): Int {
        // Approx 60 kcal per km for average person
        return (distanceMeters / 1000f * 60).toInt()
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
        
        // Reset speed calc helpers on start/resume
        lastLocationTime = System.currentTimeMillis()
        lastDistanceForSpeed = currentState.distanceMeters
        
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                setState { copy(durationMillis = durationMillis + 1000L) }
            }
        }
        
        viewModelScope.launch {
            // If run is already active (paused), do not clear data. Only clear on fresh start.
            val clearData = !currentState.isRunActive
            startRunningUseCase(clearData = clearData)
            setState { copy(isRunning = true, isRunActive = true) }
        }
    }

    private fun pauseRunning() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            pauseRunningUseCase()
            setState { copy(isRunning = false, currentSpeedKmh = 0f) } // Reset speed on pause
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
            setState { 
                copy(
                    isRunning = false, 
                    isRunActive = false, 
                    pathPoints = emptyList(), // Inference should match List<List<LatLng>> 
                    durationMillis = 0L, 
                    distanceMeters = 0f, 
                    currentSpeedKmh = 0f, 
                    caloriesBurned = 0
                ) 
            }
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

