package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.detection.OnDeviceVisualContentClassifier
import com.example.detection.VisualClassification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OnDeviceVisualContentClassifierTest {

    private val classifier = OnDeviceVisualContentClassifier()

    @Test
    fun `test neutral frame returns SAFE classification`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(20, 30, 50)) // Blue/Grey background

        val result = classifier.classifyFrame(bitmap)
        assertNotNull(result)
        assertEquals(VisualClassification.SAFE, result.classification)
        assertTrue(result.confidence >= 0.85f)
        assertTrue(result.inferenceTimeMs >= 0L)
    }

    @Test
    fun `test high skin tone density frame with natural texture returns HIGH_RISK classification`() {
        val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        // Fill with skin tones having natural luminance gradient & variance (rejecting flat fabric)
        for (y in 0 until 128) {
            for (x in 0 until 128) {
                val variance = ((x + y) % 30) - 15
                val r = (220 + variance).coerceIn(180, 245)
                val g = (155 + variance / 2).coerceIn(120, 180)
                val b = (125 + variance / 2).coerceIn(90, 150)
                bitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }

        val result = classifier.classifyFrame(bitmap)
        assertNotNull(result)
        assertEquals(VisualClassification.HIGH_RISK, result.classification)
        assertTrue(result.confidence >= 0.75f)
    }

    @Test
    fun `test flat solid beige monochrome fabric is rejected as SAFE`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(220, 160, 130)) // Flat single-color solid fabric

        val result = classifier.classifyFrame(bitmap)
        assertNotNull(result)
        assertEquals(VisualClassification.SAFE, result.classification)
        assertTrue(result.featuresDetected.any { it.contains("Flat texture/fabric") })
    }

    @Test
    fun `test perceptual hash computation is deterministic`() {
        val bitmap1 = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)

        val hash1 = classifier.computePerceptualHash(bitmap1)
        val hash2 = classifier.computePerceptualHash(bitmap2)

        assertEquals(hash1, hash2)
    }

    @Test
    fun `test face and portrait centered frame is rejected as SAFE or REVIEW rather than HIGH_RISK`() {
        val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(40, 50, 60)) // Dark clothing / background

        // Draw portrait face in top-center region only
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { color = Color.rgb(220, 160, 130) }
        canvas.drawCircle(64f, 40f, 25f, paint)

        val result = classifier.classifyFrame(bitmap)
        assertNotNull(result)
        // Upper-body localized face should NOT be flagged as HIGH_RISK full exposure
        assertTrue("Portrait should not be classified as HIGH_RISK", result.classification != VisualClassification.HIGH_RISK)
    }
}
