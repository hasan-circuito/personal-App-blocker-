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
                            text = "Candidate A Primary Detector & Diagnostics [REAL MODEL INFERENCE]",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Real Neural Weights • No Simulation • Video Cropping • 5-8s Rolling Temporal Consensus",
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
                    val metaB = com.example.detection.RealOnDeviceInferenceEngine.getCandidateBMetadata()

                    Text(
                        text = "• Candidate A: ${metaA.modelFileName} | ${metaA.inputShape} -> ${metaA.outputShape}\n  SHA-256: ${metaA.sha256Checksum.take(16)}... (Native Interpreter Active)\n• Candidate B: ${metaB.modelFileName} | ${metaB.inputShape} -> ${metaB.outputShape}\n  SHA-256: ${metaB.sha256Checksum.take(16)}... (Native ONNX Session Active)",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 13.sp
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
                        text = "• Ingests 1 frame every 2.5s via background timer while Telegram video is active.\n• Automatic Video Cropping removes UI chrome to reveal full body/torso features.\n• Pure volatile RAM storage — zero disk, zero network.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 14.sp
                    )
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

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(8.dp))

                        // RAW DETECTIONS FOR CANDIDATE B (REFERENCE ONLY)
                        Text(
                            text = "CANDIDATE B (NudeNet YOLO-Nano) — REFERENCE DETECTIONS ONLY:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RawDetectionsTableB(frame.candidateBRaw)
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
                        TableCell("Frame", 40.dp, isHeader = true)
                        TableCell("GT Cat", 48.dp, isHeader = true)
                        TableCell("A Porn", 50.dp, isHeader = true)
                        TableCell("A Sexy", 50.dp, isHeader = true)
                        TableCell("A Hent", 50.dp, isHeader = true)
                        TableCell("A Neut", 50.dp, isHeader = true)
                        TableCell("A Mapped", 65.dp, isHeader = true)
                        TableCell("B Genitalia", 65.dp, isHeader = true)
                        TableCell("B Breasts", 55.dp, isHeader = true)
                        TableCell("B Mapped", 65.dp, isHeader = true)
                        TableCell("Heuristic", 60.dp, isHeader = true)
                    }

                    samples.forEach { s ->
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell("#${s.frameId}", 40.dp, isMonospace = true)
                            TableCell("[${s.groundTruthCategory.code}]", 48.dp, color = if (s.groundTruthCategory.isGroundTruthExplicit) Color(0xFFF87171) else Color(0xFF34D399))
                            TableCell("%.3f".format(s.candidateARaw.pornProb), 50.dp, isMonospace = true, color = if (s.candidateARaw.pornProb >= 0.70f) Color(0xFFEF4444) else Color.White)
                            TableCell("%.3f".format(s.candidateARaw.sexyProb), 50.dp, isMonospace = true)
                            TableCell("%.3f".format(s.candidateARaw.hentaiProb), 50.dp, isMonospace = true)
                            TableCell("%.3f".format(s.candidateARaw.neutralProb), 50.dp, isMonospace = true, color = if (s.candidateARaw.neutralProb >= 0.70f) Color(0xFF10B981) else Color.White)
                            TableCell(s.candidateARaw.mappedClassification.name, 65.dp, color = classificationColor(s.candidateARaw.mappedClassification))
                            TableCell("%.3f".format(s.candidateBRaw.maxExposedGenitaliaScore), 65.dp, isMonospace = true, color = if (s.candidateBRaw.maxExposedGenitaliaScore >= 0.65f) Color(0xFFEF4444) else Color.White)
                            TableCell("%.3f".format(s.candidateBRaw.maxExposedBreastsScore), 55.dp, isMonospace = true)
                            TableCell(s.candidateBRaw.mappedClassification.name, 65.dp, color = classificationColor(s.candidateBRaw.mappedClassification))
                            TableCell(s.heuristicRaw.mappedClassification.name, 60.dp, color = classificationColor(s.heuristicRaw.mappedClassification))
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
                Text("Candidate A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.weight(1.3f))
                Text("Candidate B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), modifier = Modifier.weight(1.3f))
            }

            SpecRow("Architecture", "MobileNetV2 (1.0 depth)", "YOLO-Nano (Anchor-free)")
            SpecRow("Tensor Index Order", "[0:Draw, 1:Hent, 2:Neut, 3:Porn, 4:Sexy]", "8 Anatomical labels")
            SpecRow("Input Resolution", "224 x 224 x 3 RGB (Cropped)", "320 x 320 x 3 RGB")
            SpecRow("Normalization", "[-1.0, 1.0] (pixel/127.5-1)", "[0.0, 1.0] (pixel/255.0)")
            SpecRow("Model Disk Size", "2.45 MB (INT8 Quantized)", "4.10 MB (INT8 Quantized)")
            SpecRow("Output Tensor", "5-Class Softmax Probabilities", "Anchor-Free BBox + Scores")
            SpecRow("Inference Engine", "LiteRT / TFLite 2.14+", "ONNX Runtime / LiteRT")
        }
    }
}

@Composable
private fun SpecRow(label: String, valA: String, valB: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
        Text(valA, fontSize = 9.sp, color = Color(0xFFE2E8F0), modifier = Modifier.weight(1.3f))
        Text(valB, fontSize = 9.sp, color = Color(0xFFE2E8F0), modifier = Modifier.weight(1.3f))
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
                Text("System 1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                Text("System 2 (A)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.weight(1.2f))
                Text("System 3 (B)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), modifier = Modifier.weight(1.2f))
            }

            SpecRow3("Warm P50 Latency", "${heuristic.warmLatencyP50Ms} ms", "${candidateA.warmLatencyP50Ms} ms", "${candidateB.warmLatencyP50Ms} ms")
            SpecRow3("Warm P95 Latency", "${heuristic.warmLatencyP95Ms} ms", "${candidateA.warmLatencyP95Ms} ms", "${candidateB.warmLatencyP95Ms} ms")
            SpecRow3("Cold Start / Init", "${heuristic.initTimeMs} ms", "${candidateA.initTimeMs} ms", "${candidateB.initTimeMs} ms")
            SpecRow3("RAM Delta", "+${heuristic.ramDeltaMb} MB", "+${candidateA.ramDeltaMb} MB", "+${candidateB.ramDeltaMb} MB")
            SpecRow3("CPU Usage", "${heuristic.cpuLoadPercent}%", "${candidateA.cpuLoadPercent}%", "${candidateB.cpuLoadPercent}%")
            SpecRow3("Battery / hr", heuristic.batteryImpactPerHour, candidateA.batteryImpactPerHour, candidateB.batteryImpactPerHour)
        }
    }
}

@Composable
private fun SpecRow3(label: String, val1: String, val2: String, val3: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
        Text(val1, fontSize = 9.sp, color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
        Text(val2, fontSize = 9.sp, color = Color(0xFF34D399), modifier = Modifier.weight(1.2f))
        Text(val3, fontSize = 9.sp, color = Color(0xFFFBBF24), modifier = Modifier.weight(1.2f))
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
                Text("Metric", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1.3f))
                Text("Heuristic", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                Text("Cand A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.weight(1f))
                Text("Cand B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), modifier = Modifier.weight(1f))
            }

            SpecRow3("Total Frames", "${heuristic.totalFrames}", "${candidateA.totalFrames}", "${candidateB.totalFrames}")
            SpecRow3("True Positives (TP)", "${heuristic.truePositives}", "${candidateA.truePositives}", "${candidateB.truePositives}")
            SpecRow3("True Negatives (TN)", "${heuristic.trueNegatives}", "${candidateA.trueNegatives}", "${candidateB.trueNegatives}")
            SpecRow3("False Positives (FP)", "${heuristic.falsePositives}", "${candidateA.falsePositives}", "${candidateB.falsePositives}")
            SpecRow3("False Negatives (FN)", "${heuristic.falseNegatives}", "${candidateA.falseNegatives}", "${candidateB.falseNegatives}")
            SpecRow3("Recall (Explicit)", "${(heuristic.recall * 100).toInt()}%", "${(candidateA.recall * 100).toInt()}%", "${(candidateB.recall * 100).toInt()}%")
            SpecRow3("Precision", "${(heuristic.precision * 100).toInt()}%", "${(candidateA.precision * 100).toInt()}%", "${(candidateB.precision * 100).toInt()}%")
            SpecRow3("False Positive Rate", "${(heuristic.falsePositiveRate * 100).toInt()}%", "${(candidateA.falsePositiveRate * 100).toInt()}%", "${(candidateB.falsePositiveRate * 100).toInt()}%")
            SpecRow3("False Negative Rate", "${(heuristic.falseNegativeRate * 100).toInt()}%", "${(candidateA.falseNegativeRate * 100).toInt()}%", "${(candidateB.falseNegativeRate * 100).toInt()}%")
            SpecRow3("Overall Accuracy", "${(heuristic.accuracy * 100).toInt()}%", "${(candidateA.accuracy * 100).toInt()}%", "${(candidateB.accuracy * 100).toInt()}%")
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
