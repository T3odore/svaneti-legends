package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_history")
data class UnlockedHistory(
    @PrimaryKey val factId: String,
    val unlockedAtMillis: Long = System.currentTimeMillis()
)
