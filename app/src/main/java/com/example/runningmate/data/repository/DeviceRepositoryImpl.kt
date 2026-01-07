package com.example.runningmate.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.runningmate.domain.repository.DeviceRepository
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val context: Context
) : DeviceRepository {

    override fun getBatteryPercentage(): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        val level = calculateBatteryLevel(batteryStatus)
        android.util.Log.d("DeviceRepository", "getBatteryPercentage: status=$batteryStatus, level=$level")
        return level
    }

    override fun getBatteryLevelFlow(): Flow<Int> = callbackFlow {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(calculateBatteryLevel(intent))
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private fun calculateBatteryLevel(intent: Intent?): Int {
        val level: Int = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        android.util.Log.d("DeviceRepository", "calculateBatteryLevel: rawLevel=$level, rawScale=$scale")
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }
}
