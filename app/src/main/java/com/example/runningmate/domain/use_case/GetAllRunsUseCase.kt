package com.example.runningmate.domain.use_case

import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.repository.RunningRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRunsUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    operator fun invoke(): Flow<List<RunEntity>> {
        return repository.getAllRuns()
    }
}
