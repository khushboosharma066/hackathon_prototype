package com.example.suraksha_ai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guardians")
data class Guardian(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val relation: String
)

