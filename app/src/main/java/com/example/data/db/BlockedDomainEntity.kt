package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_domains")
data class BlockedDomainEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val packageName: String,
    val blockedTimestamp: Long = System.currentTimeMillis(),
    val unlockTimestamp: Long,
    val reason: String,
    val isActive: Boolean = true
)
