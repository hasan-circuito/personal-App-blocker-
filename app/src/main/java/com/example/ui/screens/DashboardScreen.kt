package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.BlockEventEntity
import com.example.data.db.LockoutEntity
import com.example.ui.components.DisableProtectionDialog
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.AppHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToApps: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isProtectionOn by viewModel.isProtectionEnabled.collectAsStateWithLifecycle()
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsStateWithLifecycle()
    val monitoredCount by viewModel.monitoredAppsCount.collectAsStateWithLifecycle()
    val activeLockouts by viewModel.activeLockouts.collectAsStateWithLifecycle()
    val recentEvents by viewModel.recentBlockEvents.collectAsStateWithLifecycle()

    var showDisableDialog by remember { mutableStateOf(false) }

    if (showDisableDialog) {
        DisableProtectionDialog(
            hasActiveLockouts = activeLockouts.isNotEmpty(),
            onConfirm = {
                viewModel.toggleProtection(false)
            },
            onDismiss = {
                showDisableDialog = false
            }
        )
    }

    // Re-check accessibility service on resume when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkServiceStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF1E1B4B), // Indigo 950
                        Color(0xFF020617)  // Slate 950
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                DashboardHeader(isProtectionOn)
            }

            // Protection Master Card
            item {
                ProtectionStatusCard(
                    isProtectionOn = isProtectionOn,
                    isServiceEnabled = isServiceEnabled,
                    onToggleProtection = { enabled ->
                        if (!enabled) {
                            showDisableDialog = true
                        } else {
                            viewModel.toggleProtection(true)
                        }
                    },
                    onEnableServiceClick = { AppHelper.openAccessibilitySettings(context) }
                )
            }

            // Warning Banner if Accessibility Service is not enabled
            if (!isServiceEnabled) {
                item {
                    ServiceWarningCard(
                        onOpenSettings = { AppHelper.openAccessibilitySettings(context) },
                        onOpenWizard = onNavigateToSetup
                    )
                }
            }

            // Overview Metrics Grid
            item {
                MetricsRow(
                    monitoredCount = monitoredCount,
                    activeLockoutsCount = activeLockouts.size,
                    totalEventsCount = recentEvents.size
                )
            }

            // Active Lockouts Section
            if (activeLockouts.isNotEmpty()) {
                item {
                    Text(
                        text = "Active Lockouts (${activeLockouts.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(activeLockouts, key = { it.id }) { lockout ->
                    ActiveLockoutCard(lockout)
                }
            }

            // Quick Navigation Controls
            item {
                Text(
                    text = "Controls & Configuration",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                QuickNavTile(
                    title = "Monitored Applications",
                    subtitle = "$monitoredCount selected for local protection",
                    icon = Icons.Default.Apps,
                    iconTint = Color(0xFF6366F1),
                    onClick = onNavigateToApps
                )
            }

            item {
                QuickNavTile(
                    title = "Settings & Diagnostic Test Mode",
                    subtitle = "Sensitivity, lockout duration, and test suite",
                    icon = Icons.Default.Settings,
                    iconTint = Color(0xFF38BDF8),
                    onClick = onNavigateToSettings
                )
            }

            item {
                QuickNavTile(
                    title = "Blocking Log History",
                    subtitle = "View timestamps and trigger reasons",
                    icon = Icons.Default.History,
                    iconTint = Color(0xFFA855F7),
                    onClick = onNavigateToHistory
                )
            }

            // Recent Events Section
            if (recentEvents.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Intercepts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            color = Color(0xFF818CF8),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToHistory() }
                        )
                    }
                }

                items(recentEvents.take(3), key = { it.id }) { event ->
                    RecentEventCard(event)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DashboardHeader(isProtectionOn: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "FocusGuard",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Personal Content Blocker",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isProtectionOn) Color(0xFF10B981).copy(alpha = 0.2f)
                    else Color(0xFFEF4444).copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isProtectionOn) Icons.Default.Shield else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isProtectionOn) Color(0xFF34D399) else Color(0xFFF87171),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun ProtectionStatusCard(
    isProtectionOn: Boolean,
    isServiceEnabled: Boolean,
    onToggleProtection: (Boolean) -> Unit,
    onEnableServiceClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isProtectionOn) "Protection ACTIVE" else "Protection PAUSED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProtectionOn) Color(0xFF34D399) else Color(0xFFF87171)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isProtectionOn)
                            "Local engine is monitoring selected applications."
                        else "Monitoring paused. Content will not be blocked.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                Switch(
                    checked = isProtectionOn,
                    onCheckedChange = onToggleProtection,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    )
                )
            }
        }
    }
}

@Composable
fun ServiceWarningCard(
    onOpenSettings: () -> Unit,
    onOpenWizard: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7F1D1D).copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFCA5A5),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Accessibility Permission Required",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FocusGuard needs Accessibility Service permission to detect when selected apps are active and inspect screen content locally.",
                fontSize = 13.sp,
                color = Color(0xFFFECACA),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF991B1B)
                    )
                ) {
                    Text("Enable Permission", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenWizard,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF991B1B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Setup Guide", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MetricsRow(
    monitoredCount: Int,
    activeLockoutsCount: Int,
    totalEventsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricTile(
            modifier = Modifier.weight(1f),
            label = "Monitored",
            value = monitoredCount.toString(),
            icon = Icons.Default.Apps,
            color = Color(0xFF6366F1)
        )
        MetricTile(
            modifier = Modifier.weight(1f),
            label = "Active Locks",
            value = activeLockoutsCount.toString(),
            icon = Icons.Default.Lock,
            color = Color(0xFFEF4444)
        )
        MetricTile(
            modifier = Modifier.weight(1f),
            label = "Total Logs",
            value = totalEventsCount.toString(),
            icon = Icons.Default.Security,
            color = Color(0xFF10B981)
        )
    }
}

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun ActiveLockoutCard(lockout: LockoutEntity) {
    var remainingMs by remember(lockout.unlockTimestamp) {
        mutableStateOf((lockout.unlockTimestamp - System.currentTimeMillis()).coerceAtLeast(0L))
    }

    LaunchedEffect(lockout.unlockTimestamp) {
        while (remainingMs > 0) {
            delay(1000L)
            remainingMs = (lockout.unlockTimestamp - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }

    val hours = (remainingMs / (1000 * 60 * 60))
    val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (remainingMs % (1000 * 60)) / 1000

    val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val unlockTimeFormatted = remember(lockout.unlockTimestamp) {
        timeFormatter.format(Date(lockout.unlockTimestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF312E81) // Indigo 900
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lockout.appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Locked until $unlockTimeFormatted",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFF87171)
                )
                Text(
                    text = lockout.reason,
                    fontSize = 11.sp,
                    color = Color(0xFFA5B4FC)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4338CA)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = timeString,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFF43F5E)
                    )
                    Text(
                        text = "Remaining",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun RecentEventCard(event: BlockEventEntity) {
    val formatter = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateString = formatter.format(Date(event.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = event.reason,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Text(
                text = dateString,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
