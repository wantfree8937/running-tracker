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

import com.example.runningmate.domain.use_case.GetCurrentRunUseCase
import com.example.runningmate.domain.use_case.ObserveBatteryLevelUseCase

@HiltViewModel
class RunningViewModel @Inject constructor(
    private val startRunningUseCase: StartRunningUseCase,
    private val pauseRunningUseCase: PauseRunningUseCase,
    private val stopRunningUseCase: StopRunningUseCase,
    private val observeLocationUseCase: ObserveLocationUseCase,
    private val getCurrentRunUseCase: GetCurrentRunUseCase,
    private val observeBatteryLevelUseCase: ObserveBatteryLevelUseCase,
    private val getBatteryLevelUseCase: com.example.runningmate.domain.use_case.GetBatteryLevelUseCase
) : BaseViewModel<RunningState, RunningIntent, RunningEffect>(RunningState()) {

    private var timerJob: Job? = null
    private var locationJob: Job? = null
    private var sharedLocationJob: Job? = null
    private var batteryJob: Job? = null
    private var batteryWarningTimeoutJob: Job? = null
    private var hasShownBatteryWarning = false
    
    // For speed calculation
    private var lastLocationTime: Long = 0L
    private var lastDistanceForSpeed: Float = 0f
    private var lastLocation: LatLng? = null

    init {
        startObservingSharedLocation()
        startObservingBattery()
        restoreRunState()
    }

    private fun restoreRunState() {
        viewModelScope.launch {
            val currentRun = getCurrentRunUseCase()
            if (currentRun != null) {
                // Restore state
                val now = System.currentTimeMillis()
                val duration = System.currentTimeMillis() - currentRun.startTime
                
                setState { 
                    copy(
                        isRunning = true,
                        isRunActive = true,
                        durationMillis = duration,
                        // pathPoints will be updated by flow
                    ) 
                }
                startTimer() // Resume timer
            }
        }
    }

    override fun handleIntent(intent: RunningIntent) {
        android.util.Log.d("RunningViewModel", "handleIntent: $intent")
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

    private fun startRunning() {
        android.util.Log.d("RunningViewModel", "startRunning called. TimerActive: ${timerJob?.isActive}")
        if (timerJob?.isActive == true) {
            android.util.Log.d("RunningViewModel", "startRunning ignored: Timer already active")
            return
        }

        // Reset state for new run segment
        hasShownBatteryWarning = false
        
        // Initial Battery Check
        val batteryLevel = getBatteryLevelUseCase()
        android.util.Log.d("RunningViewModel", "startRunning: Initial Check - level=$batteryLevel")
        
        if (batteryLevel != -1 && batteryLevel <= 20) {
            showBatteryWarning("배터리가 너무 낮아 운동을 시작할 수 없습니다 ($batteryLevel%)", isCritical = true)
            if (currentState.isRunActive) {
                stopRunning()
            }
            return
        }

        if (batteryLevel != -1 && batteryLevel <= 30) {
            android.util.Log.d("RunningViewModel", "startRunning: Initial WARNING TRIGGERED")
            showBatteryWarning("배터리가 부족합니다 ($batteryLevel%)", isCritical = false)
            hasShownBatteryWarning = true
        } else {
            android.util.Log.d("RunningViewModel", "startRunning: Initial Check OK or Unknown")
            setState { copy(batteryWarning = null) }
            hasShownBatteryWarning = false
        }

        // Reset speed calc helpers on start/resume
        lastLocationTime = System.currentTimeMillis()
        lastDistanceForSpeed = currentState.distanceMeters
        
        startTimer()
        
        viewModelScope.launch {
            // If run is already active (paused), do not clear data. Only clear on fresh start.
            val clearData = !currentState.isRunActive
            startRunningUseCase(clearData = clearData)
            setState { copy(isRunning = true, isRunActive = true) }
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                setState { copy(durationMillis = durationMillis + 1000L) }
            }
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
                distance = currentState.distanceMeters,
                calories = currentState.caloriesBurned
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

    private fun startObservingSharedLocation() {
        sharedLocationJob?.cancel()
        sharedLocationJob = viewModelScope.launch {
            observeLocationUseCase.pathPointsFlow.collect { points ->
                setState { copy(pathPoints = points) }
            }
        }
    }

    private fun startObservingLocation() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            observeLocationUseCase().collect { location ->
                if (currentState.isRunActive) {
                    val lastLoc = lastLocation
                    val timestamp = System.currentTimeMillis()

                    if (lastLoc != null) {
                        val distance = calculateDistance(lastLoc, location)
                        val timeDiff = timestamp - lastLocationTime
                        val speed = if (timeDiff > 0) {
                            (distance / 1000f) / (timeDiff / 3600000f) // km / h
                        } else 0f

                        val newDistance = currentState.distanceMeters + distance
                        val newCalories = (newDistance / 1000f) * 60 // Rough estimation: 60kcal/km

                        setState {
                            copy(
                                distanceMeters = newDistance,
                                currentSpeedKmh = speed,
                                caloriesBurned = newCalories.toInt()
                            )
                        }
                    }
                    lastLocation = location
                    lastLocationTime = timestamp
                } else {
                    lastLocation = location
                    lastLocationTime = System.currentTimeMillis()
                }
                
                // Update current location for Map Focus
                setState { copy(currentLocation = location) }
            }
        }
    }

    private fun startObservingBattery() {
        batteryJob?.cancel()
        batteryJob = viewModelScope.launch {
            observeBatteryLevelUseCase().collect { level ->
                android.util.Log.d("RunningViewModel", "Battery Flow Update: $level, isRunning=${currentState.isRunning}, hasShown=$hasShownBatteryWarning")
                
                if (level != -1 && level <= 20 && currentState.isRunActive) {
                    // 20% 이하: 강제 종료 및 저장
                    android.util.Log.d("RunningViewModel", "CRITICAL BATTERY: Auto-stopping run")
                    showBatteryWarning("배터리가 너무 낮아 운동을 종료하고 기록을 저장합니다 ($level%)", isCritical = true)
                    stopRunning()
                    return@collect
                }

                if (currentState.isRunning && level != -1 && level <= 30 && !hasShownBatteryWarning) {
                    android.util.Log.d("RunningViewModel", "Battery Flow WARNING TRIGGERED")
                    showBatteryWarning("배터리가 부족합니다 ($level%)", isCritical = false)
                    hasShownBatteryWarning = true
                } else if (level > 30) {
                    setState { copy(batteryWarning = null) }
                    hasShownBatteryWarning = false
                }
            }
        }
    }

    private fun showBatteryWarning(message: String, isCritical: Boolean) {
        setState { copy(batteryWarning = com.example.runningmate.presentation.feature.running.contract.BatteryWarning(message, isCritical)) }
        batteryWarningTimeoutJob?.cancel()
        batteryWarningTimeoutJob = viewModelScope.launch {
            delay(5000L) // 5초 후 경고 숨김
            setState { copy(batteryWarning = null) }
        }
    }
}

