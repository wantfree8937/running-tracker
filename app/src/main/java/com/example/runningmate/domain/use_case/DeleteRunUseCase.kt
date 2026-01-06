package com.example.runningmate.domain.use_case

import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.repository.RunningRepository
import javax.inject.Inject

class DeleteRunUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke(run: RunEntity) {
        repository.deleteRun(run)
    }
}
