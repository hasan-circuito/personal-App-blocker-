package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isMonitored: Boolean = true,
    val isCategoryDefault: Boolean = false,
    val isUserSelected: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
)
