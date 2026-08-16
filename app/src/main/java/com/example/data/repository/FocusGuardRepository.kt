package com.example.data.repository

import android.content.Context
import com.example.data.db.AppSettingEntity
import com.example.data.db.BlockEventEntity
import com.example.data.db.BlockedDomainEntity
import com.example.data.db.FocusGuardDao
import com.example.data.db.FocusGuardDatabase
import com.example.data.db.LockoutEntity
import com.example.data.db.MonitoredAppEntity
import com.example.data.model.ProtectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FocusGuardRepository(private val dao: FocusGuardDao) {

    companion object {
        private const val KEY_PROTECTION_ENABLED = "is_protection_enabled"
        private const val KEY_SENSITIVITY = "sensitivity"
        private const val KEY_LOCKOUT_DURATION = "lockout_duration_minutes"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"

        @Volatile
        private var INSTANCE: FocusGuardRepository? = null

        fun getInstance(context: Context): FocusGuardRepository {
            return INSTANCE ?: synchronized(this) {
                val db = FocusGuardDatabase.getDatabase(context)
                val repo = FocusGuardRepository(db.focusGuardDao())
                INSTANCE = repo
                // Automatically clear any prior visual-triggered lockouts for test mode
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        repo.clearVisualLockouts()
                    } catch (e: Exception) {
                        android.util.Log.e("FocusGuardRepo", "Failed to clear visual lockouts on init", e)
                    }
                }
                repo
            }
        }
    }

    // --- Monitored Apps ---
    val monitoredAppsFlow: Flow<List<MonitoredAppEntity>> = dao.getAllMonitoredAppsFlow()

    suspend fun getActiveMonitoredAppsSync(): List<MonitoredAppEntity> {
        return dao.getActiveMonitoredAppsSync()
    }

    suspend fun isAppMonitored(packageName: String): Boolean {
        val app = dao.getMonitoredApp(packageName)
        return app?.isMonitored == true
    }

    suspend fun setAppMonitored(packageName: String, appName: String, isMonitored: Boolean) {
        val existing = dao.getMonitoredApp(packageName)
        if (existing != null) {
            dao.setMonitoredState(packageName, isMonitored)
        } else {
            dao.insertMonitoredApp(
                MonitoredAppEntity(
                    packageName = packageName,
                    appName = appName,
                    isMonitored = isMonitored,
                    isUserSelected = true
                )
            )
        }
    }

    suspend fun saveMonitoredAppsList(apps: List<MonitoredAppEntity>) {
        dao.insertMonitoredApps(apps)
    }

    // --- Lockout Management ---
    val activeLockoutsFlow: Flow<List<LockoutEntity>> = dao.getActiveLockoutsFlow()

    suspend fun getActiveLockout(packageName: String): LockoutEntity? {
        return dao.getActiveLockoutForPackage(packageName)
    }

    suspend fun createLockout(
        packageName: String,
        appName: String,
        durationMinutes: Int,
        reason: String,
        confidence: Float
    ): LockoutEntity {
        val now = System.currentTimeMillis()
        val unlockTime = now + (durationMinutes * 60 * 1000L)
        val lockout = LockoutEntity(
            packageName = packageName,
            appName = appName,
            blockedTimestamp = now,
            unlockTimestamp = unlockTime,
            reason = reason,
            confidence = confidence,
            isActive = true
        )
        val id = dao.insertLockout(lockout)
        
        // Log event history
        dao.insertBlockEvent(
            BlockEventEntity(
                packageName = packageName,
                appName = appName,
                timestamp = now,
                reason = reason,
                confidence = confidence,
                lockoutDurationMinutes = durationMinutes
            )
        )

        return lockout.copy(id = id)
    }

    suspend fun recordBlockEvent(
        packageName: String,
        appName: String,
        reason: String,
        confidence: Float = 1.0f
    ) {
        dao.insertBlockEvent(
            BlockEventEntity(
                packageName = packageName,
                appName = appName,
                timestamp = System.currentTimeMillis(),
                reason = reason,
                confidence = confidence,
                lockoutDurationMinutes = 0
            )
        )
    }

    suspend fun clearAllLockouts() {
        dao.clearAllLockouts()
    }

    suspend fun clearVisualLockouts() {
        dao.clearVisualLockouts()
    }

    // --- Blocked Domains ---
    val activeBlockedDomainsFlow: Flow<List<BlockedDomainEntity>> = dao.getActiveBlockedDomainsFlow()

    suspend fun getActiveBlockedDomain(domain: String): BlockedDomainEntity? {
        return dao.getActiveBlockedDomain(domain)
    }

    suspend fun createDomainLockout(
        domain: String,
        packageName: String,
        durationMinutes: Int,
        reason: String
    ): BlockedDomainEntity {
        val now = System.currentTimeMillis()
        val unlockTime = now + (durationMinutes * 60 * 1000L)
        val entity = BlockedDomainEntity(
            domain = domain,
            packageName = packageName,
            blockedTimestamp = now,
            unlockTimestamp = unlockTime,
            reason = reason,
            isActive = true
        )
        val id = dao.insertBlockedDomain(entity)

        dao.insertBlockEvent(
            BlockEventEntity(
                packageName = packageName,
                appName = "Browser ($domain)",
                timestamp = now,
                reason = "Blocked website: $domain - $reason",
                confidence = 1.0f,
                lockoutDurationMinutes = durationMinutes
            )
        )

        return entity.copy(id = id)
    }

    // --- History Events ---
    val blockEventsFlow: Flow<List<BlockEventEntity>> = dao.getAllBlockEventsFlow()

    suspend fun clearBlockHistory() {
        dao.clearBlockEvents()
    }

    // --- Settings & Protection State ---
    val protectionStateFlow: Flow<ProtectionState> = dao.getSettingFlow(KEY_PROTECTION_ENABLED)
        .map { setting ->
            val value = setting?.value
            if (value == "false" || value == "PROTECTION_DISABLED") {
                ProtectionState.PROTECTION_DISABLED
            } else {
                ProtectionState.PROTECTION_ENABLED
            }
        }

    val isProtectionEnabledFlow: Flow<Boolean> = protectionStateFlow
        .map { it == ProtectionState.PROTECTION_ENABLED }

    suspend fun getProtectionStateSync(): ProtectionState {
        val value = dao.getSetting(KEY_PROTECTION_ENABLED)?.value
        return if (value == "false" || value == "PROTECTION_DISABLED") {
            ProtectionState.PROTECTION_DISABLED
        } else {
            ProtectionState.PROTECTION_ENABLED
        }
    }

    suspend fun isProtectionEnabledSync(): Boolean {
        return getProtectionStateSync() == ProtectionState.PROTECTION_ENABLED
    }

    suspend fun setProtectionState(state: ProtectionState) {
        val valueStr = if (state == ProtectionState.PROTECTION_ENABLED) "true" else "false"
        dao.setSetting(AppSettingEntity(KEY_PROTECTION_ENABLED, valueStr))
    }

    suspend fun setProtectionEnabled(enabled: Boolean) {
        setProtectionState(if (enabled) ProtectionState.PROTECTION_ENABLED else ProtectionState.PROTECTION_DISABLED)
    }

    val sensitivityFlow: Flow<String> = dao.getSettingFlow(KEY_SENSITIVITY)
        .map { it?.value ?: "MEDIUM" }

    suspend fun getSensitivitySync(): String {
        return dao.getSetting(KEY_SENSITIVITY)?.value ?: "MEDIUM"
    }

    suspend fun setSensitivity(sensitivity: String) {
        dao.setSetting(AppSettingEntity(KEY_SENSITIVITY, sensitivity))
    }

    val lockoutDurationMinutesFlow: Flow<Int> = dao.getSettingFlow(KEY_LOCKOUT_DURATION)
        .map { it?.value?.toIntOrNull() ?: 120 }

    suspend fun getLockoutDurationMinutesSync(): Int {
        return dao.getSetting(KEY_LOCKOUT_DURATION)?.value?.toIntOrNull() ?: 120
    }

    suspend fun setLockoutDurationMinutes(minutes: Int) {
        dao.setSetting(AppSettingEntity(KEY_LOCKOUT_DURATION, minutes.toString()))
    }

    val isFirstLaunchFlow: Flow<Boolean> = dao.getSettingFlow(KEY_FIRST_LAUNCH)
        .map { it?.value?.toBoolean() ?: true }

    suspend fun setFirstLaunchCompleted() {
        dao.setSetting(AppSettingEntity(KEY_FIRST_LAUNCH, "false"))
    }
}
