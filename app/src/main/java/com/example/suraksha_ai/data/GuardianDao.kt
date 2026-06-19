package com.example.suraksha_ai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianDao {
    @Query("SELECT * FROM guardians ORDER BY name ASC")
    fun getAllGuardians(): Flow<List<Guardian>>

    @Query("SELECT * FROM guardians WHERE id = :id")
    suspend fun getGuardianById(id: Long): Guardian?

    @Insert
    suspend fun insertGuardian(guardian: Guardian): Long

    @Update
    suspend fun updateGuardian(guardian: Guardian)

    @Delete
    suspend fun deleteGuardian(guardian: Guardian)
}

