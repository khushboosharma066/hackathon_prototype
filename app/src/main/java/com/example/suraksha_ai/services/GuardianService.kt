package com.example.suraksha_ai.services

import com.example.suraksha_ai.data.Guardian
import com.example.suraksha_ai.data.GuardianDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GuardianService(private val guardianDao: GuardianDao) {
    suspend fun getGuardians(): List<Guardian> {
        return guardianDao.getAllGuardians().first()
    }
}

