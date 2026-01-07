package com.example.runningmate.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.runningmate.data.dto.CurrentRunEntity

// [Location]: core/database/RunningDatabase.kt
@Database(entities = [RunEntity::class, CurrentRunEntity::class], version = 3, exportSchema = false)
@TypeConverters(RunEntityConverters::class)
abstract class RunningDatabase : RoomDatabase() {
    abstract val runningDao: RunningDao
}
