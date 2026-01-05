package com.example.runningmate.domain.use_case

import javax.inject.Inject

// [Location]: domain/use_case/PauseRunningUseCase.kt
class PauseRunningUseCase @Inject constructor() {
    operator fun invoke() {
        // Business logic for pausing
        // e.g. Updating state in repository to 'PAUSED' if we tracked state there.
    }
}
