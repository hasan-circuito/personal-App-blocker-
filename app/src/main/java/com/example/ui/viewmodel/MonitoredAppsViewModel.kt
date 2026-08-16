package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.MonitoredAppEntity
import com.example.data.repository.FocusGuardRepository
import com.example.util.AppHelper
import com.example.util.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonitoredAppUiState(
    val info: InstalledAppInfo,
    val isMonitored: Boolean
)

enum class AppFilterMode {
    ALL,
    MONITORED_ONLY,
    BROWSERS_AND_SOCIAL
}

class MonitoredAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FocusGuardRepository.getInstance(application)

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterMode = MutableStateFlow(AppFilterMode.ALL)
    val filterMode: StateFlow<AppFilterMode> = _filterMode.asStateFlow()

    val appListState: StateFlow<List<MonitoredAppUiState>> = combine(
        _installedApps,
        repository.monitoredAppsFlow,
        _searchQuery,
        _filterMode
    ) { installedList, dbList, query, filter ->
        val monitoredMap = dbList.associateBy { it.packageName }

        installedList
            .map { app ->
                val isMonitored = monitoredMap[app.packageName]?.isMonitored ?: app.isBrowserOrSocial
                MonitoredAppUiState(app, isMonitored)
            }
            .filter { item ->
                val matchesQuery = query.isBlank() ||
                        item.info.appName.contains(query, ignoreCase = true) ||
                        item.info.packageName.contains(query, ignoreCase = true)

                val matchesFilter = when (filter) {
                    AppFilterMode.ALL -> true
                    AppFilterMode.MONITORED_ONLY -> item.isMonitored
                    AppFilterMode.BROWSERS_AND_SOCIAL -> item.info.isBrowserOrSocial
                }

                matchesQuery && matchesFilter
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val apps = AppHelper.getInstalledApps(getApplication())
            _installedApps.value = apps

            // Pre-populate DB with common browsers default if DB empty
            val currentDb = repository.monitoredAppsFlow.stateIn(viewModelScope).value
            if (currentDb.isEmpty()) {
                val initialEntities = apps.map { app ->
                    MonitoredAppEntity(
                        packageName = app.packageName,
                        appName = app.appName,
                        isMonitored = app.isBrowserOrSocial,
                        isCategoryDefault = app.isBrowserOrSocial
                    )
                }
                repository.saveMonitoredAppsList(initialEntities)
            }

            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterMode(mode: AppFilterMode) {
        _filterMode.value = mode
    }

    fun toggleAppMonitored(packageName: String, appName: String, isMonitored: Boolean) {
        viewModelScope.launch {
            repository.setAppMonitored(packageName, appName, isMonitored)
        }
    }

    fun selectAllBrowsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = _installedApps.value
            val browserApps = apps.filter { it.isBrowserOrSocial }
            browserApps.forEach { app ->
                repository.setAppMonitored(app.packageName, app.appName, true)
            }
        }
    }

    fun deselectAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = _installedApps.value
            apps.forEach { app ->
                repository.setAppMonitored(app.packageName, app.appName, false)
            }
        }
    }
}
