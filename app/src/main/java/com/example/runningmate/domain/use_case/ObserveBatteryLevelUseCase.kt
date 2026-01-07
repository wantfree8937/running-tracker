package com.example.runningmate.domain.use_case

import com.example.runningmate.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBatteryLevelUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<Int> {
        return deviceRepository.getBatteryLevelFlow()
    }
}
