package com.example.runningmate.data.dto

import androidx.room.TypeConverter
import com.example.runningmate.domain.model.RunningPath
import com.google.android.gms.maps.model.LatLng
import java.util.Date

// Note: I will use manual parsing or basic string representation to avoid Gson dependency if not added, 
// BUT JSON is cleaner. I will assume Gson or standard serialization. 
// For simplicity and strictness without extra libs (unless I added Gson?), I'll use simple string splitting for LatLng.
// Actually, I didn't add Gson. I'll use manual string formatting.

// [Location]: data/dto/RunEntityConverters.kt
class RunEntityConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toLatLngList(value: String): List<List<LatLng>> {
        if (value.isBlank()) return emptyList()
        val segments = mutableListOf<List<LatLng>>()
        val segmentStrings = value.split("|")
        
        for (segmentString in segmentStrings) {
             val list = mutableListOf<LatLng>()
             if (segmentString.isNotBlank()) {
                 val pairs = segmentString.split(";")
                 for (pair in pairs) {
                    val coords = pair.split(",")
                    if (coords.size == 2) {
                        try {
                            val lat = coords[0].toDouble()
                            val lng = coords[1].toDouble()
                            list.add(LatLng(lat, lng))
                        } catch (e: Exception) {
                            // Ignore malformed
                        }
                    }
                 }
             }
             if (list.isNotEmpty()) {
                 segments.add(list)
             }
        }
        return segments
    }

    @TypeConverter
    fun fromLatLngList(list: List<List<LatLng>>): String {
        return list.joinToString("|") { segment ->
            segment.joinToString(";") { "${it.latitude},${it.longitude}" }
        }
    }
}
