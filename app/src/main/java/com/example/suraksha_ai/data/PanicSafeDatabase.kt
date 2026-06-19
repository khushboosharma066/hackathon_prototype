package com.example.suraksha_ai.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Guardian::class],
    version = 1,
    exportSchema = false
)
abstract class PanicSafeDatabase : RoomDatabase() {
    abstract fun guardianDao(): GuardianDao
}

