package com.example.runningmate.domain.use_case

import com.example.runningmate.domain.repository.RunningRepository
import javax.inject.Inject

// [Location]: domain/use_case/StartRunningUseCase.kt
class StartRunningUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke() {
        // Here we would trigger the Worker or ensure the repository is ready to receive data.
        // For a pure MVI + Worker setup, the Worker might be triggered by the ViewModel via WorkManager,
        // or this UseCase can talk to a 'ServiceManager' or 'WorkerManager' abstraction.
        // Since we don't have a separate WorkerManager, this might be a placeholder for logic 
        // like "Reset current run ID" or "Prepare DB".
        
        // In a strict sense, starting the foreground service usually happens in presentation (Service Intent) 
        // or via WorkManager enqueue.
        // We will keep it empty or add logging/prep logic.
        
        // If we want to move WorkManager logic here, we need to inject WorkManager.
        // For now, I will treat this as "Business Logic for Starting", e.g. nothing specific yet.
    }
}
