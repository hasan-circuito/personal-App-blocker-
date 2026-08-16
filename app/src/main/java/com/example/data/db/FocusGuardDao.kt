package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusGuardDao {

    // --- Monitored Apps ---
    @Query("SELECT * FROM monitored_apps ORDER BY appName ASC")
    fun getAllMonitoredAppsFlow(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT * FROM monitored_apps WHERE isMonitored = 1")
    suspend fun getActiveMonitoredAppsSync(): List<MonitoredAppEntity>

    @Query("SELECT * FROM monitored_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getMonitoredApp(packageName: String): MonitoredAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonitoredApps(apps: List<MonitoredAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonitoredApp(app: MonitoredAppEntity)

    @Update
    suspend fun updateMonitoredApp(app: MonitoredAppEntity)

    @Query("UPDATE monitored_apps SET isMonitored = :isMonitored WHERE packageName = :packageName")
    suspend fun setMonitoredState(packageName: String, isMonitored: Boolean)

    @Query("DELETE FROM monitored_apps WHERE packageName = :packageName")
    suspend fun deleteMonitoredApp(packageName: String)

    // --- Lockouts ---
    @Query("SELECT * FROM lockouts WHERE isActive = 1 AND unlockTimestamp > :now ORDER BY unlockTimestamp DESC")
    fun getActiveLockoutsFlow(now: Long = System.currentTimeMillis()): Flow<List<LockoutEntity>>

    @Query("SELECT * FROM lockouts WHERE isActive = 1 AND unlockTimestamp > :now")
    suspend fun getActiveLockoutsSync(now: Long = System.currentTimeMillis()): List<LockoutEntity>

    @Query("SELECT * FROM lockouts WHERE packageName = :packageName AND isActive = 1 AND unlockTimestamp > :now LIMIT 1")
    suspend fun getActiveLockoutForPackage(packageName: String, now: Long = System.currentTimeMillis()): LockoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockout(lockout: LockoutEntity): Long

    @Query("UPDATE lockouts SET isActive = 0 WHERE id = :id")
    suspend fun expireLockout(id: Long)

    @Query("DELETE FROM lockouts")
    suspend fun clearAllLockouts()

    @Query("UPDATE lockouts SET isActive = 0 WHERE (reason LIKE '%Visual%' OR reason LIKE '%visual%') AND isActive = 1")
    suspend fun clearVisualLockouts()

    // --- Block Events ---
    @Query("SELECT * FROM block_events ORDER BY timestamp DESC LIMIT 100")
    fun getAllBlockEventsFlow(): Flow<List<BlockEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockEvent(event: BlockEventEntity)

    @Query("DELETE FROM block_events")
    suspend fun clearBlockEvents()

    // --- Blocked Domains ---
    @Query("SELECT * FROM blocked_domains WHERE isActive = 1 AND unlockTimestamp > :now ORDER BY unlockTimestamp DESC")
    fun getActiveBlockedDomainsFlow(now: Long = System.currentTimeMillis()): Flow<List<BlockedDomainEntity>>

    @Query("SELECT * FROM blocked_domains WHERE domain = :domain AND isActive = 1 AND unlockTimestamp > :now LIMIT 1")
    suspend fun getActiveBlockedDomain(domain: String, now: Long = System.currentTimeMillis()): BlockedDomainEntity?

    @Query("SELECT * FROM blocked_domains WHERE packageName = :packageName AND isActive = 1 AND unlockTimestamp > :now")
    suspend fun getActiveBlockedDomainsForPackage(packageName: String, now: Long = System.currentTimeMillis()): List<BlockedDomainEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedDomain(domain: BlockedDomainEntity): Long

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<AppSettingEntity?>

    @Query("SELECT * FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
