package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.detection.OnDeviceModelBenchmarkSuite
import com.example.detection.TelegramVideoRegionCropper
import com.example.detection.VisualClassification
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OnDeviceModelBenchmarkCard() {
    val coroutineScope = rememberCoroutineScope()
    val isLiveSamplingActive by OnDeviceModelBenchmarkSuite.isLiveCaptureModeActive.collectAsStateWithLifecycle()
    val activeSessionId by OnDeviceModelBenchmarkSuite.activeSessionId.collectAsStateWithLifecycle()
    val selectedLiveCategory by OnDeviceModelBenchmarkSuite.selectedLiveCategory.collectAsStateWithLifecycle()
    val collectedFrameCount by OnDeviceModelBenchmarkSuite.collectedFrameCount.collectAsStateWithLifecycle()
    val latestFrame by OnDeviceModelBenchmarkSuite.latestFrameSample.collectAsStateWithLifecycle()
    val latestTemporalState by OnDeviceModelBenchmarkSuite.latestTemporalEvidence.collectAsStateWithLifecycle()
    val videoSessionEvidence by OnDeviceModelBenchmarkSuite.videoSessionEvidence.collectAsStateWithLifecycle()
    val isBenchmarkRunning by OnDeviceModelBenchmarkSuite.isBenchmarkRunning.collectAsStateWithLifecycle()
    val benchmarkProgress by OnDeviceModelBenchmarkSuite.benchmarkProgress.collectAsStateWithLifecycle()
    val report by OnDeviceModelBenchmarkSuite.activeReport.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C31))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "On-Device Neural AI Inspector (MobileNetV2)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "TensorFlow Lite Engine • Live Softmax Probabilities • Video Region Cropping",
                            fontSize = 11.sp,
                            color = Color(0xFFD8B4FE)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Diagnostic Mode Active: Zero automatic intervention, no video close, no lockout. Full exposure of raw softmax probabilities for Candidate A, automatic Telegram UI cropping, and rolling temporal evidence aggregation.",
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // MODEL INTEGRITY & CRYPTOGRAPHIC PROVENANCE PANEL
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MODEL INTEGRITY VERIFICATION (REAL INFERENCE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0369A1)
                        ) {
                            Text(
                                text = "SIMULATION BYPASS: ON",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val metaA = com.example.detection.RealOnDeviceInferenceEngine.getCandidateAMetadata()

                    Text(
                        text = "• Production Model: ${metaA.modelFileName} | Input: ${metaA.inputShape} -> Output: ${metaA.outputShape}\n  Tensor Index: [0:Draw, 1:Hent, 2:Neut, 3:Porn, 4:Sexy]\n  SHA-256: ${metaA.sha256Checksum.take(16)}... (Native TensorFlow Lite C++ Engine Active)",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // LIVE TELEGRAM FRAME SAMPLING & SESSION PANEL
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D182E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (isLiveSamplingActive) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isLiveSamplingActive) "Session: $activeSessionId (ACTIVE)" else "Session: $activeSessionId (IDLE)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveSamplingActive) Color(0xFFF87171) else Color(0xFF94A3B8)
                            )
                        }

                        Text(
                            text = "$collectedFrameCount frames in RAM",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ground-Truth Category Selector Chips
                    Text(
                        text = "1. SELECT REAL-WORLD TEST CATEGORY (METADATA ONLY):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OnDeviceModelBenchmarkSuite.RealWorldCategory.values().forEach { cat ->
                            val isSelected = selectedLiveCategory == cat
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF7E22CE) else Color(0xFF1E293B),
                                modifier = Modifier.clickable {
                                    OnDeviceModelBenchmarkSuite.setSelectedLiveCategory(cat)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "[${cat.code}] ${cat.displayName}",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Controls: Start, Stop, Flush
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isLiveSamplingActive) {
                            Button(
                                onClick = { OnDeviceModelBenchmarkSuite.startNewSession() },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF16A34A),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Timer Sampling Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { OnDeviceModelBenchmarkSuite.stopCurrentSession() },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Timer Sampling Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { OnDeviceModelBenchmarkSuite.clearEphemeralBuffer() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Flush All", fontSize = 11.sp, color = Color(0xFFEF4444))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Ingests 1 frame every 2.5s (adaptive 1.0s fast re-sampling after seek/controls) while Telegram video is active.\n• Automatic Video Cropping removes UI chrome to reveal full body/torso features.\n• Pure volatile RAM storage — zero disk, zero network.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )

                    // Video Session Evidence Summary Card
                    if (videoSessionEvidence.totalFrames > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            border = BorderStroke(1.dp, if (videoSessionEvidence.isStickyHighRisk) Color(0xFFEF4444) else Color(0xFF6366F1))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VIDEO-SESSION EVIDENCE STATUS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA5B4FC)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            videoSessionEvidence.videoRiskLevel.contains("HIGH_RISK") -> Color(0xFFEF4444)
                                            videoSessionEvidence.videoRiskLevel.contains("REVIEW") -> Color(0xFFF59E0B)
                                            else -> Color(0xFF10B981)
                                        }
                                    ) {
                                        Text(
                                            text = videoSessionEvidence.videoRiskLevel,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TensorClassPill("Total Captured", videoSessionEvidence.totalFrames.toFloat(), Color(0xFF38BDF8))
                                    TensorClassPill("Stable Video", videoSessionEvidence.totalStableFrames.toFloat(), Color(0xFF10B981))
                                    TensorClassPill("Seek/Overlay/Blank", (videoSessionEvidence.transitionFrames + videoSessionEvidence.loadingFrames + videoSessionEvidence.playerControlsFrames).toFloat(), Color(0xFF94A3B8))
                                    TensorClassPill("High Risk Stable", videoSessionEvidence.highRiskStableFrames.toFloat(), Color(0xFFEF4444))
                                    TensorClassPill("HR Ratio", videoSessionEvidence.highRiskRatio, Color(0xFFA855F7), isHighlight = true)
                                }

                                Text(
                                    text = "Sampling: ${videoSessionEvidence.activeSamplingMode} • Sticky Status: ${if (videoSessionEvidence.isStickyHighRisk) "STICKY HIGH RISK (Sustained through seeks/transitions)" else "Normal"}",
                                    fontSize = 8.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // LIVE IN-MEMORY FRAME PROVENANCE INSPECTION
            // ==========================================
            if (latestFrame != null) {
                val frame = latestFrame!!
                val thumb = frame.thumbnailBitmap
                val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LATEST EVALUATED FRAME (IN-MEMORY RAW PROVENANCE)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Frame #${frame.frameId}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Thumbnail Preview
                            if (thumb != null && !thumb.isRecycled) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(Color.Black, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = thumb.asImageBitmap(),
                                        contentDescription = "Diagnostic Frame Thumbnail",
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            // Provenance Metadata
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                ProvenanceText("Session ID", frame.sessionId)
                                ProvenanceText("Timestamp", timeFormat.format(Date(frame.timestamp)))
                                ProvenanceText("Input Size", "${frame.inputWidth}x${frame.inputHeight}")
                                ProvenanceText("Video Crop", frame.candidateARaw.cropDescription)
                                ProvenanceText("Perceptual Hash", frame.pHashHex)
                                ProvenanceText("Ground Truth", "[${frame.groundTruthCategory.code}] ${frame.groundTruthCategory.displayName}")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(8.dp))

                        // RAW TENSOR OUTPUT FOR CANDIDATE A
                        Text(
                            text = "CANDIDATE A (MobileNetV2) — RAW 5-CLASS SOFTMAX TENSOR:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                        Text(
                            text = "Preprocessing: ${frame.candidateARaw.preprocessingLog}",
                            fontSize = 8.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RawTensorGridA(frame.candidateARaw)

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(8.dp))

                        // TEMPORAL EVIDENCE WINDOW STATE
                        Text(
                            text = "5-8 SECOND ROLLING TEMPORAL CONSENSUS STATE:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA855F7)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TensorClassPill("Window Frames", frame.temporalState.framesInWindow.toFloat(), Color(0xFF38BDF8))
                            TensorClassPill("Max Porn in Window", frame.temporalState.maxPornScore, Color(0xFFEF4444))
                            TensorClassPill("Max Sexy in Window", frame.temporalState.maxSexyScore, Color(0xFFF59E0B))
                            TensorClassPill("Accumulation Score", frame.temporalState.temporalAccumulationScore, Color(0xFFA855F7), isHighlight = true)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: ${frame.temporalState.diagnosticSummary}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (frame.temporalState.isConfirmedRisk) Color(0xFFEF4444) else Color(0xFF34D399)
                        )

                        // PRODUCTION MODEL CONFIDENCE SUMMARY
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Model Architecture: MobileNetV2 • 5-Class Softmax • Zero Cloud Dependency",
                            fontSize = 8.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Standard Diagnostic Benchmark Trigger
            if (isBenchmarkRunning) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Evaluating 10-Category Diagnostic Frames...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC084FC)
                        )
                        Text(
                            text = "${(benchmarkProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    LinearProgressIndicator(
                        progress = { benchmarkProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFFA855F7),
                        trackColor = Color(0xFF334155)
                    )
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            OnDeviceModelBenchmarkSuite.runStandard10CategoryDiagnostic()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7E22CE),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run Standard 10-Category Diagnostic Suite",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ==========================================
            // COMPARISON REPORT & SESSION TABLE
            // ==========================================
            if (report != null) {
                val rep = report!!
                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: All Session Frames Raw Table
                Text(
                    text = "1. SESSION FRAMES RAW OUTPUT TABLE (${rep.realFrameSamples.size} FRAMES)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC084FC),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                SessionFramesRawTableCard(rep.realFrameSamples)

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Specifications & Model Metadata
                Text(
                    text = "2. VERIFIED MODEL SPECIFICATIONS & TENSOR INDEXING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC084FC),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                ModelSpecCard(rep)

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Real-World Latency & Hardware Overhead
                Text(
                    text = "3. MEASURED LATENCY & HARDWARE OVERHEAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC084FC),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                HardwareMetricsCard(
                    heuristic = rep.heuristicLatency,
                    candidateA = rep.candidateALatency,
                    candidateB = rep.candidateBLatency
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Performance & Accuracy Metrics
                Text(
                    text = "4. EMPIRICAL ACCURACY & CLASSIFICATION METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC084FC),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                EmpiricalMetricsCard(
                    heuristic = rep.heuristicPerf,
                    candidateA = rep.candidateAPerf,
                    candidateB = rep.candidateBPerf
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 5: Diagnostic Conclusion
                RecommendationCard()
            }
        }
    }
}

@Composable
private fun RawTensorGridA(raw: OnDeviceModelBenchmarkSuite.CandidateARawOutput) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TensorClassPill("Index 0: Drawing", raw.drawingProb, Color(0xFF94A3B8))
        TensorClassPill("Index 1: Hentai", raw.hentaiProb, Color(0xFFF43F5E))
        TensorClassPill("Index 2: Neutral", raw.neutralProb, Color(0xFF10B981))
        TensorClassPill("Index 3: Porn", raw.pornProb, Color(0xFFEF4444))
        TensorClassPill("Index 4: Sexy", raw.sexyProb, Color(0xFFF59E0B))
        TensorClassPill("Mapped -> ${raw.mappedClassification.name}", raw.derivedRiskScore, Color(0xFFA855F7), isHighlight = true)
    }
}

@Composable
private fun TensorClassPill(label: String, prob: Float, baseColor: Color, isHighlight: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isHighlight) baseColor.copy(alpha = 0.25f) else Color(0xFF1E293B)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = baseColor)
            Text("${"%.3f".format(prob)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun RawDetectionsTableB(raw: OnDeviceModelBenchmarkSuite.CandidateBRawOutput) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TensorClassPill("Exposed Genitalia", raw.maxExposedGenitaliaScore, Color(0xFFEF4444))
            TensorClassPill("Exposed Breasts", raw.maxExposedBreastsScore, Color(0xFFF43F5E))
            TensorClassPill("Exposed Buttocks", raw.maxExposedButtocksScore, Color(0xFFF97316))
            TensorClassPill("Covered Breasts", raw.maxCoveredBreastsScore, Color(0xFF10B981))
            TensorClassPill("Face", raw.maxFaceScore, Color(0xFF38BDF8))
            TensorClassPill("App Risk Mapping -> ${raw.mappedClassification.name}", raw.derivedRiskScore, Color(0xFFA855F7), isHighlight = true)
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "RAW DETECTIONS LIST (${raw.detections.size} Bounding Boxes):",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                if (raw.detections.isEmpty()) {
                    Text(
                        text = "Zero detections returned by model for this frame.",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                } else {
                    raw.detections.forEachIndexed { idx, det ->
                        val isExplicit = det.label.startsWith("EXPOSED_")
                        Text(
                            text = "[#$idx] ${det.label} • Conf=${"%.3f".format(det.confidence)} • Box(ymin=${"%.2f".format(det.box.yMin)}, xmin=${"%.2f".format(det.box.xMin)}, ymax=${"%.2f".format(det.box.yMax)}, xmax=${"%.2f".format(det.box.xMax)})",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isExplicit) FontWeight.Bold else FontWeight.Normal,
                            color = if (isExplicit) Color(0xFFF87171) else Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Risk Rule: Requires EXPOSED_GENITALIA >= 0.65 OR EXPOSED_BREASTS >= 0.65 OR EXPOSED_BUTTOCKS >= 0.65. Clothed/Face/Covered labels are 100% ignored in HIGH_RISK mapping.",
                    fontSize = 8.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun SessionFramesRawTableCard(samples: List<OnDeviceModelBenchmarkSuite.RealFrameSample>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier.background(Color(0xFF1E293B), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TableCell("Frame", 36.dp, isHeader = true)
                        TableCell("State", 56.dp, isHeader = true)
                        TableCell("Neutral", 48.dp, isHeader = true)
                        TableCell("Porn", 46.dp, isHeader = true)
                        TableCell("Sexy", 46.dp, isHeader = true)
                        TableCell("Hentai", 46.dp, isHeader = true)
                        TableCell("Draw", 42.dp, isHeader = true)
                        TableCell("P+0.85S", 50.dp, isHeader = true)
                        TableCell("Frame Risk", 85.dp, isHeader = true)
                        TableCell("Video Risk", 110.dp, isHeader = true)
                        TableCell("HR Ratio", 55.dp, isHeader = true)
                    }

                    samples.forEach { s ->
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("#${s.frameId}", 36.dp, isMonospace = true)
                            TableCell(
                                text = s.stabilityState.name.take(6),
                                width = 56.dp,
                                color = when (s.stabilityState) {
                                    TelegramVideoRegionCropper.FrameStabilityState.STABLE -> Color(0xFF10B981)
                                    TelegramVideoRegionCropper.FrameStabilityState.PLAYER_CONTROLS -> Color(0xFFF59E0B)
                                    TelegramVideoRegionCropper.FrameStabilityState.LOADING -> Color(0xFF38BDF8)
                                    TelegramVideoRegionCropper.FrameStabilityState.TRANSITION -> Color(0xFF94A3B8)
                                },
                                isMonospace = true
                            )
                            TableCell("%.3f".format(s.candidateARaw.neutralProb), 48.dp, isMonospace = true, color = if (s.candidateARaw.neutralProb >= 0.70f) Color(0xFF10B981) else Color.White)
                            TableCell("%.3f".format(s.candidateARaw.pornProb), 46.dp, isMonospace = true, color = if (s.candidateARaw.pornProb >= 0.70f) Color(0xFFEF4444) else if (s.candidateARaw.pornProb >= 0.35f) Color(0xFFF59E0B) else Color.White)
                            TableCell("%.3f".format(s.candidateARaw.sexyProb), 46.dp, isMonospace = true, color = if (s.candidateARaw.sexyProb >= 0.50f) Color(0xFFF59E0B) else Color.White)
                            TableCell("%.3f".format(s.candidateARaw.hentaiProb), 46.dp, isMonospace = true)
                            TableCell("%.3f".format(s.candidateARaw.drawingProb), 42.dp, isMonospace = true)
                            TableCell("%.3f".format(s.diagnosticCombinedScore), 50.dp, isMonospace = true, color = Color(0xFFA855F7))
                            TableCell(
                                text = s.frameRisk.name,
                                width = 85.dp,
                                color = classificationColor(s.frameRisk),
                                isMonospace = true
                            )
                            TableCell(
                                text = s.videoRiskLevel,
                                width = 110.dp,
                                color = when {
                                    s.videoRiskLevel.contains("HIGH_RISK") -> Color(0xFFEF4444)
                                    s.videoRiskLevel.contains("REVIEW") -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                },
                                isMonospace = true
                            )
                            TableCell(
                                text = "${"%.1f".format(s.sessionHighRiskRatio * 100)}%",
                                width = 55.dp,
                                isMonospace = true,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(text: String, width: androidx.compose.ui.unit.Dp, isHeader: Boolean = false, isMonospace: Boolean = false, color: Color = Color.Unspecified) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontSize = if (isHeader) 8.sp else 9.sp,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
        color = if (color != Color.Unspecified) color else if (isHeader) Color(0xFF94A3B8) else Color(0xFFE2E8F0)
    )
}

private fun classificationColor(c: VisualClassification): Color = when (c) {
    VisualClassification.SAFE -> Color(0xFF10B981)
    VisualClassification.REVIEW -> Color(0xFFF59E0B)
    VisualClassification.HIGH_RISK -> Color(0xFFEF4444)
}

@Composable
private fun ProvenanceText(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF94A3B8)
        )
        Text(
            text = value,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE2E8F0)
        )
    }
}

@Composable
private fun ModelSpecCard(report: OnDeviceModelBenchmarkSuite.ComprehensiveBenchmarkReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Specification", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
                Text("Production Model (Candidate A)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.weight(2f))
            }

            SpecRow2("Architecture", "MobileNetV2 (1.0 depth, Pre-trained on NSFW dataset)")
            SpecRow2("Tensor Index Order", "[0: Drawing, 1: Hentai, 2: Neutral, 3: Porn, 4: Sexy]")
            SpecRow2("Input Resolution", "224 x 224 x 3 RGB (Cropped from Telegram Media)")
            SpecRow2("Normalization", "[-1.0, 1.0] (Formula: pixel / 127.5 - 1.0)")
            SpecRow2("Model Asset Size", "24.4 MB (Full Precision FP32 Weights in APK)")
            SpecRow2("Output Tensor", "5-Class Softmax Probabilities (Float32 Array)")
            SpecRow2("Inference Engine", "TensorFlow Lite Native C++ Interpreter (libtensorflowlite_jni.so)")
        }
    }
}

@Composable
private fun SpecRow2(label: String, valA: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.2f))
        Text(valA, fontSize = 9.sp, color = Color(0xFFE2E8F0), modifier = Modifier.weight(2f))
    }
}

@Composable
private fun HardwareMetricsCard(
    heuristic: OnDeviceModelBenchmarkSuite.LatencyAndResourceMetrics,
    candidateA: OnDeviceModelBenchmarkSuite.LatencyAndResourceMetrics,
    candidateB: OnDeviceModelBenchmarkSuite.LatencyAndResourceMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hardware Metric", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
                Text("Text Heuristic", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.weight(1.2f))
                Text("Production Model (A)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.weight(1.5f))
            }

            SpecRow2Cols("Warm P50 Latency", "${heuristic.warmLatencyP50Ms} ms", "${candidateA.warmLatencyP50Ms} ms")
            SpecRow2Cols("Warm P95 Latency", "${heuristic.warmLatencyP95Ms} ms", "${candidateA.warmLatencyP95Ms} ms")
            SpecRow2Cols("Cold Start / Init", "${heuristic.initTimeMs} ms", "${candidateA.initTimeMs} ms")
            SpecRow2Cols("RAM Delta", "+${heuristic.ramDeltaMb} MB", "+${candidateA.ramDeltaMb} MB")
            SpecRow2Cols("CPU Usage", "${heuristic.cpuLoadPercent}%", "${candidateA.cpuLoadPercent}%")
            SpecRow2Cols("Battery / hr", heuristic.batteryImpactPerHour, candidateA.batteryImpactPerHour)
        }
    }
}

@Composable
private fun SpecRow2Cols(label: String, val1: String, val2: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
        Text(val1, fontSize = 9.sp, color = Color(0xFF38BDF8), modifier = Modifier.weight(1.2f))
        Text(val2, fontSize = 9.sp, color = Color(0xFF34D399), modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun EmpiricalMetricsCard(
    heuristic: OnDeviceModelBenchmarkSuite.PerformanceMetrics,
    candidateA: OnDeviceModelBenchmarkSuite.PerformanceMetrics,
    candidateB: OnDeviceModelBenchmarkSuite.PerformanceMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Accuracy Metric", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
                Text("Heuristic", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.weight(1.2f))
                Text("Production Model (A)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.weight(1.5f))
            }

            SpecRow2Cols("Total Frames", "${heuristic.totalFrames}", "${candidateA.totalFrames}")
            SpecRow2Cols("True Positives (TP)", "${heuristic.truePositives}", "${candidateA.truePositives}")
            SpecRow2Cols("True Negatives (TN)", "${heuristic.trueNegatives}", "${candidateA.trueNegatives}")
            SpecRow2Cols("False Positives (FP)", "${heuristic.falsePositives}", "${candidateA.falsePositives}")
            SpecRow2Cols("False Negatives (FN)", "${heuristic.falseNegatives}", "${candidateA.falseNegatives}")
            SpecRow2Cols("Recall (Explicit)", "${(heuristic.recall * 100).toInt()}%", "${(candidateA.recall * 100).toInt()}%")
            SpecRow2Cols("Precision", "${(heuristic.precision * 100).toInt()}%", "${(candidateA.precision * 100).toInt()}%")
            SpecRow2Cols("False Positive Rate", "${(heuristic.falsePositiveRate * 100).toInt()}%", "${(candidateA.falsePositiveRate * 100).toInt()}%")
            SpecRow2Cols("False Negative Rate", "${(heuristic.falseNegativeRate * 100).toInt()}%", "${(candidateA.falseNegativeRate * 100).toInt()}%")
            SpecRow2Cols("Overall Accuracy", "${(heuristic.accuracy * 100).toInt()}%", "${(candidateA.accuracy * 100).toInt()}%")
        }
    }
}

@Composable
private fun RecommendationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Candidate A Diagnostic Pipeline Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Text(
                text = "Primary Detector (Candidate A) Diagnostic Ready",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34D399)
            )

            Text(
                text = "1. Raw 5-class softmax probabilities are exposed for every frame without masking.\n2. Automatic Video Region Cropper discards Telegram top/bottom bars before inference.\n3. Periodic sampling timer captures 1 frame every 2.5s throughout video playback.\n4. 5-8 second rolling evidence window accumulates sequential probabilities for difficult faceless/upper-body adult videos.\n5. Zero automated blocking or lockout active.",
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 15.sp
            )
        }
    }
}
