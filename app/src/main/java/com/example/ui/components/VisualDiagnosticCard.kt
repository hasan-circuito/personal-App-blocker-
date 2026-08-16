package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.detection.TelegramChannelGuardManager
import com.example.detection.VisualClassification
import com.example.detection.VisualDiagnosticPipeline

@Composable
fun VisualDiagnosticCard() {
    val context = LocalContext.current

    val isActive by VisualDiagnosticPipeline.isDiagnosticActive.collectAsStateWithLifecycle()
    val mediaStateDetected by VisualDiagnosticPipeline.mediaStateDetected.collectAsStateWithLifecycle()
    val frameAcquisitionStatus by VisualDiagnosticPipeline.frameAcquisitionStatus.collectAsStateWithLifecycle()
    val visualClassificationStatus by VisualDiagnosticPipeline.visualClassificationStatus.collectAsStateWithLifecycle()
    val riskConfirmationActive by VisualDiagnosticPipeline.riskConfirmationActive.collectAsStateWithLifecycle()
    val riskConfirmationText by VisualDiagnosticPipeline.riskConfirmationText.collectAsStateWithLifecycle()
    val interventionBridgeStatus by VisualDiagnosticPipeline.interventionBridgeStatus.collectAsStateWithLifecycle()
    val interventionStatus by VisualDiagnosticPipeline.interventionStatus.collectAsStateWithLifecycle()
    val lastResult by VisualDiagnosticPipeline.lastResult.collectAsStateWithLifecycle()
    val sampledCount by VisualDiagnosticPipeline.sampledFramesCount.collectAsStateWithLifecycle()
    val skippedDuplicates by VisualDiagnosticPipeline.skippedDuplicateFramesCount.collectAsStateWithLifecycle()
    val latencyMs by VisualDiagnosticPipeline.lastInterventionLatencyMs.collectAsStateWithLifecycle()
    val currentChannel by TelegramChannelGuardManager.currentChannelContext.collectAsStateWithLifecycle()
    val activeRestrictions by TelegramChannelGuardManager.activeRestrictions.collectAsStateWithLifecycle()
    val logs by VisualDiagnosticPipeline.logs.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F2942)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Telegram Visual Protection & Channel Guard",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Telegram-Only Visual Gating & Session Guard",
                            fontSize = 11.sp,
                            color = Color(0xFF7DD3FC)
                        )
                    }
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { VisualDiagnosticPipeline.setDiagnosticActive(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF0284C7),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Runs 100% on-device visual analysis exclusively when Telegram media viewing is active. Never runs on Chrome or regular browsing. On confirmed adult video detection, it immediately exits the viewer and temporarily restricts that specific channel context without locking Telegram.",
                fontSize = 12.sp,
                color = Color(0xFFBAE6FD),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pipeline Subsystems Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071E33))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TELEGRAM-ONLY GATING & ENGINE SUBSYSTEMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 0.5.sp
                    )

                    StatusRow(
                        label = "Visual Gate:",
                        value = if (isActive) "TELEGRAM-ONLY ACTIVE" else "IDLE",
                        isHighlight = isActive
                    )
                    StatusRow(
                        label = "Media Viewer State:",
                        value = if (mediaStateDetected) "MEDIA_VIEWER_ACTIVE" else "INACTIVE_BROWSING",
                        isHighlight = mediaStateDetected
                    )
                    StatusRow(
                        label = "Risk Confirmation:",
                        value = if (riskConfirmationActive) "TEMPORAL CONSENSUS ACTIVE" else "IDLE",
                        isHighlight = riskConfirmationActive
                    )
                    StatusRow(
                        label = "Intervention Bridge:",
                        value = interventionBridgeStatus,
                        isHighlight = interventionBridgeStatus == "CONNECTED"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Diagnostic Metrics Dashboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071E33))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LIVE CLASSIFICATION & CHANNEL CONTEXT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 0.5.sp
                    )

                    // Last Classification Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last Frame Classification:",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        if (lastResult != null) {
                            val result = lastResult!!
                            val badgeColor = when (result.classification) {
                                VisualClassification.SAFE -> Color(0xFF10B981)
                                VisualClassification.REVIEW -> Color(0xFFF59E0B)
                                VisualClassification.HIGH_RISK -> Color(0xFFEF4444)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${result.classification.name} (${(result.confidence * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "None",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Risk Confirmation State
                    MetricRow("Temporal Evidence:", riskConfirmationText)

                    // Channel Context State
                    val channelLabel = currentChannel?.let {
                        if (it.username != null) "${it.title} (${it.username})" else it.title
                    } ?: "None (Awaiting Telegram navigation)"
                    MetricRow("Channel Context:", channelLabel)

                    // Active Channel Restrictions
                    MetricRow("Restricted Channels:", "${activeRestrictions.size} Active Session(s)")

                    // Intervention State
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intervention State:",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        val statusColor = when {
                            interventionStatus.startsWith("INTERCEPTED") -> Color(0xFF38BDF8)
                            interventionStatus.startsWith("TRIGGERED") || interventionStatus.startsWith("BLOCKED") -> Color(0xFFEF4444)
                            interventionStatus.startsWith("WAITING") -> Color(0xFF10B981)
                            else -> Color(0xFF94A3B8)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = interventionStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (latencyMs > 0) {
                        MetricRow("Intervention Latency:", "$latencyMs ms (Sub-second)")
                    }

                    MetricRow("Acquisition Status:", frameAcquisitionStatus)
                    MetricRow("Sampled Frames Count:", "$sampledCount (Skipped duplicates: $skippedDuplicates)")

                    if (lastResult != null) {
                        val result = lastResult!!
                        MetricRow("Inference Latency:", "${result.inferenceTimeMs} ms")
                        if (result.featuresDetected.isNotEmpty()) {
                            Text(
                                text = "Features: ${result.featuresDetected.joinToString("; ")}",
                                fontSize = 11.sp,
                                color = Color(0xFF7DD3FC)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { VisualDiagnosticPipeline.runManualDiagnosticTest("SIMULATED_HIGH_RISK", context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE11D48),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Adult Frame", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { VisualDiagnosticPipeline.runManualDiagnosticTest("SIMULATED_SAFE", context) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Safe Frame", fontSize = 11.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { VisualDiagnosticPipeline.resetRiskConfirmationState() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Temporal", fontSize = 11.sp, color = Color(0xFFBAE6FD))
                }

                OutlinedButton(
                    onClick = { TelegramChannelGuardManager.clearAllRestrictions() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Restrictions", fontSize = 11.sp, color = Color(0xFFFCA5A5))
                }
            }

            // Diagnostic Event Logs
            if (logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Recent Diagnostic Pipeline Events:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    logs.take(6).forEach { log ->
                        Text(
                            text = log,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, isHighlight: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) Color(0xFF38BDF8) else Color(0xFF64748B)
        )
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
