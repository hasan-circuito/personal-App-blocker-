package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lockouts")
data class LockoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val blockedTimestamp: Long = System.currentTimeMillis(),
    val unlockTimestamp: Long,
    val reason: String,
    val confidence: Float,
    val isActive: Boolean = true
)
