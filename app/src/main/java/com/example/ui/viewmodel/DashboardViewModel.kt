package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BlockEventEntity
import com.example.data.db.LockoutEntity
import com.example.data.repository.FocusGuardRepository
import com.example.util.AppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FocusGuardRepository.getInstance(application)

    val isProtectionEnabled: StateFlow<Boolean> = repository.isProtectionEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val monitoredAppsCount: StateFlow<Int> = repository.monitoredAppsFlow
        .map { list -> list.count { it.isMonitored } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeLockouts: StateFlow<List<LockoutEntity>> = repository.activeLockoutsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBlockEvents: StateFlow<List<BlockEventEntity>> = repository.blockEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    init {
        checkServiceStatus()
    }

    fun checkServiceStatus() {
        val context = getApplication<Application>()
        _isServiceEnabled.value = AppHelper.isAccessibilityServiceEnabled(context)
    }

    fun toggleProtection(enabled: Boolean) {
        viewModelScope.launch {
            repository.setProtectionEnabled(enabled)
        }
    }
}
