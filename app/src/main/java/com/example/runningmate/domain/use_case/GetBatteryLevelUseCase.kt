package com.example.runningmate.domain.use_case

import com.example.runningmate.domain.repository.DeviceRepository
import javax.inject.Inject

class GetBatteryLevelUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Int {
        return deviceRepository.getBatteryPercentage()
    }
}
