package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.FocusGuardRepository
import com.example.ui.screens.BlockHistoryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MonitoredAppsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupWizardScreen
import com.example.ui.theme.FocusGuardTheme
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.MonitoredAppsViewModel
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val monitoredAppsViewModel: MonitoredAppsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = FocusGuardRepository.getInstance(applicationContext)

        setContent {
            FocusGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A)
                ) {
                    val navController = rememberNavController()
                    val isFirstLaunch by repository.isFirstLaunchFlow.collectAsStateWithLifecycle(initialValue = false)

                    NavHost(
                        navController = navController,
                        startDestination = if (isFirstLaunch) "setup" else "dashboard"
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigateToApps = { navController.navigate("monitored_apps") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToSetup = { navController.navigate("setup") }
                            )
                        }

                        composable("monitored_apps") {
                            MonitoredAppsScreen(
                                viewModel = monitoredAppsViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBackClick = { navController.popBackStack() },
                                onRerunSetupClick = { navController.navigate("setup") }
                            )
                        }

                        composable("history") {
                            BlockHistoryScreen(
                                viewModel = dashboardViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("setup") {
                            SetupWizardScreen(
                                onCompleteSetup = {
                                    navController.navigate("dashboard") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
