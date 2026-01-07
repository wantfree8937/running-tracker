package com.example.runningmate.domain.repository

import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getBatteryPercentage(): Int
    fun getBatteryLevelFlow(): Flow<Int>
}
