package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.FocusGuardRepository
import com.example.service.BlockOverlayManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FocusGuardRepository.getInstance(application)

    val isProtectionEnabled: StateFlow<Boolean> = repository.isProtectionEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val sensitivity: StateFlow<String> = repository.sensitivityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEDIUM")

    val lockoutDurationMinutes: StateFlow<Int> = repository.lockoutDurationMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)

    fun setProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setProtectionEnabled(enabled)
        }
    }

    fun setSensitivity(sensitivity: String) {
        viewModelScope.launch {
            repository.setSensitivity(sensitivity)
        }
    }

    fun setLockoutDurationMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.setLockoutDurationMinutes(minutes)
        }
    }

    fun clearBlockHistory() {
        viewModelScope.launch {
            repository.clearBlockHistory()
        }
    }

    fun clearActiveLockouts() {
        viewModelScope.launch {
            repository.clearAllLockouts()
        }
    }

    fun triggerTestLockout(packageName: String, appName: String, context: Context) {
        viewModelScope.launch {
            val durationMinutes = repository.getLockoutDurationMinutesSync()
            val lockout = repository.createLockout(
                packageName = packageName,
                appName = appName,
                durationMinutes = durationMinutes,
                reason = "Manual Test Detection (System Diagnostic)",
                confidence = 1.0f
            )

            BlockOverlayManager.showBlockScreen(
                context = context,
                packageName = packageName,
                appName = appName,
                lockoutReason = lockout.reason
            )
        }
    }
}
