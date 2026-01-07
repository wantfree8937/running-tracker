package com.example.runningmate.domain.use_case

import com.example.runningmate.data.dto.CurrentRunEntity
import com.example.runningmate.domain.repository.RunningRepository
import javax.inject.Inject

class GetCurrentRunUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke(): CurrentRunEntity? {
        return repository.getCurrentRun()
    }
}
