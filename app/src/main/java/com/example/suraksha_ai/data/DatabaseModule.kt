package com.example.suraksha_ai.data

import android.content.Context
import androidx.room.Room

object DatabaseModule {
    fun provideDatabase(context: Context): PanicSafeDatabase {
        return Room.databaseBuilder(
            context,
            PanicSafeDatabase::class.java,
            "panicsafe_database"
        ).build()
    }
}

