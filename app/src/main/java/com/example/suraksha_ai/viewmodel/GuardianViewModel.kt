package com.example.suraksha_ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suraksha_ai.data.Guardian
import com.example.suraksha_ai.data.GuardianDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GuardianViewModel(
    private val guardianDao: GuardianDao
) : ViewModel() {

    val guardians = guardianDao.getAllGuardians()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addGuardian(name: String, phone: String, relation: String) {
        viewModelScope.launch {
            guardianDao.insertGuardian(
                Guardian(
                    name = name,
                    phone = phone,
                    relation = relation
                )
            )
        }
    }

    fun deleteGuardian(guardian: Guardian) {
        viewModelScope.launch {
            guardianDao.deleteGuardian(guardian)
        }
    }

    fun updateGuardian(guardian: Guardian) {
        viewModelScope.launch {
            guardianDao.updateGuardian(guardian)
        }
    }
}

