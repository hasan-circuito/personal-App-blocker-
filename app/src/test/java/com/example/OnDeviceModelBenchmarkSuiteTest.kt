package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.detection.OnDeviceModelBenchmarkSuite
import com.example.detection.VisualClassification
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OnDeviceModelBenchmarkSuiteTest {

    @Test
    fun testCandidateModelMetadataVerification() {
        val candA = OnDeviceModelBenchmarkSuite.CANDIDATE_A_SPEC
        val candB = OnDeviceModelBenchmarkSuite.CANDIDATE_B_SPEC
        val heuristic = OnDeviceModelBenchmarkSuite.HEURISTIC_SPEC

        assertNotNull(candA.name)
        assertEquals("github.com/infinitered/nsfwjs (Gant Laborde)", candA.sourceRepo)
        assertEquals("MIT License (Code) / Apache 2.0 (MobileNetV2)", candA.license)
        assertTrue(candA.modelSizeMb < 5.0f)
        assertTrue(candA.outputLabels.contains("Porn"))
        assertTrue(candA.outputLabels.contains("Sexy"))
        assertTrue(candA.outputLabels.contains("Neutral"))
        assertTrue(candA.outputLabels.contains("Hentai"))
        assertTrue(candA.outputLabels.contains("Drawing"))

        assertNotNull(candB.name)
        assertEquals("github.com/notbed/nudenet (NudeNet v3 detector)", candB.sourceRepo)
        assertTrue(candB.outputLabels.contains("EXPOSED_GENITALIA"))

        assertNotNull(heuristic.name)
        assertEquals(0.05f, heuristic.modelSizeMb)
    }

    @Test
    fun testRawTensorExposureAndSessionIsolation() {
        // 1. Start Session 1: Explicit Video
        OnDeviceModelBenchmarkSuite.startNewSession(OnDeviceModelBenchmarkSuite.RealWorldCategory.CATEGORY_A)
        val session1Id = OnDeviceModelBenchmarkSuite.activeSessionId.value
        assertTrue(session1Id.startsWith("SESSION_"))

        // Explicit Frame (Warm flesh pixels)
        val explicitBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                explicitBitmap.setPixel(x, y, Color.rgb(225, 158, 128))
            }
        }
        OnDeviceModelBenchmarkSuite.ingestRealTelegramFrame(explicitBitmap, OnDeviceModelBenchmarkSuite.RealWorldCategory.CATEGORY_A)
        assertEquals(1, OnDeviceModelBenchmarkSuite.collectedFrameCount.value)

        val explicitSample = OnDeviceModelBenchmarkSuite.latestFrameSample.value
        assertNotNull(explicitSample)
        assertTrue(explicitSample!!.candidateARaw.pornProb >= 0.70f)
        assertEquals(VisualClassification.HIGH_RISK, explicitSample.candidateARaw.mappedClassification)
        assertTrue(explicitSample.candidateBRaw.maxExposedGenitaliaScore >= 0.65f)
        assertEquals(VisualClassification.HIGH_RISK, explicitSample.candidateBRaw.mappedClassification)

        // 2. Start Session 2: Quran Video (Clean Session Reset)
        OnDeviceModelBenchmarkSuite.startNewSession(OnDeviceModelBenchmarkSuite.RealWorldCategory.CATEGORY_J)
        val session2Id = OnDeviceModelBenchmarkSuite.activeSessionId.value
        assertNotSame(session1Id, session2Id)
        assertEquals(0, OnDeviceModelBenchmarkSuite.collectedFrameCount.value) // Clean reset!

        // Quran / Normal Video Frame (Green background with gold calligraphy/text)
        val quranBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(quranBitmap)
        val paint = Paint()
        paint.color = Color.rgb(20, 75, 45) // Islamic Green
        canvas.drawRect(0f, 0f, 100f, 100f, paint)
        paint.color = Color.rgb(235, 200, 120) // Gold Text
        canvas.drawRect(20f, 30f, 80f, 40f, paint)

        // Ingest Quran frame
        OnDeviceModelBenchmarkSuite.ingestRealTelegramFrame(quranBitmap, OnDeviceModelBenchmarkSuite.RealWorldCategory.CATEGORY_J)

        // 3. Verify that Quran frame is independently evaluated as SAFE with raw probabilities exposed
        val quranSample = OnDeviceModelBenchmarkSuite.latestFrameSample.value
        assertNotNull(quranSample)
        assertEquals(session2Id, quranSample?.sessionId)
        assertEquals(VisualClassification.SAFE, quranSample?.candidateARaw?.mappedClassification)
        assertEquals(VisualClassification.SAFE, quranSample?.candidateBRaw?.mappedClassification)
        assertEquals(VisualClassification.SAFE, quranSample?.heuristicRaw?.mappedClassification)
        assertTrue(quranSample?.candidateARaw?.neutralProb ?: 0f > 0.85f)
        assertTrue(quranSample?.candidateARaw?.pornProb ?: 0f < 0.05f)

        // 4. Flush RAM
        OnDeviceModelBenchmarkSuite.clearEphemeralBuffer()
        assertEquals(0, OnDeviceModelBenchmarkSuite.collectedFrameCount.value)
    }

    @Test
    fun test10CategoryDiagnosticSuiteExecution() = runTest {
        val report = OnDeviceModelBenchmarkSuite.runStandard10CategoryDiagnostic()

        assertNotNull(report)
        assertFalse(OnDeviceModelBenchmarkSuite.isBenchmarkRunning.value)
        assertEquals(1.0f, OnDeviceModelBenchmarkSuite.benchmarkProgress.value, 0.01f)

        // Latencies should be measurable
        assertTrue(report.candidateALatency.warmLatencyP50Ms > 0)
        assertTrue(report.candidateBLatency.warmLatencyP50Ms > 0)
        assertTrue(report.heuristicLatency.warmLatencyP50Ms > 0)

        // 10 categories should be evaluated
        assertEquals(10, report.realFrameSamples.size)

        // Check frame 1 (Category A Explicit)
        val frame1 = report.realFrameSamples[0]
        assertTrue(frame1.candidateARaw.pornProb >= 0.70f)
        assertEquals(VisualClassification.HIGH_RISK, frame1.candidateARaw.mappedClassification)

        // Clear ephemeral buffer
        OnDeviceModelBenchmarkSuite.clearEphemeralBuffer()
        assertEquals(0, OnDeviceModelBenchmarkSuite.collectedFrameCount.value)
    }
}
