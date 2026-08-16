package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String,
    val confidence: Float,
    val lockoutDurationMinutes: Int
)
