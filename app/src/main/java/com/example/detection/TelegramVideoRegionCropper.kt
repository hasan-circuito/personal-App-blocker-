package com.example.detection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import kotlin.math.max
import kotlin.math.min

/**
 * Telegram Video Region Detector & Aspect-Ratio Preserving Cropper.
 *
 * Problem Solved:
 * When taking a full Android screenshot in Telegram, up to 60-70% of the screen consists of
 * UI bars (chat top bar, bottom message input / scrubber, navigation bars, chat bubbles).
 * Feeding the entire uncropped screenshot into Candidate A severely dilutes features and
 * causes faceless/upper-body adult video frames to be misclassified as Neutral/SAFE.
 *
 * This module:
 * 1. Scans the full phone screenshot to identify the active Telegram video/media bounding box
 * 2. Crops out Telegram UI chrome (top bars, bottom scrubbers, navigation buttons)
 * 3. Applies letterbox/pillarbox aspect-ratio preserving downsampling to 224x224 RGB
 * 4. Extracts both the Full Screenshot and the Cropped Video Region for direct comparison.
 */
object TelegramVideoRegionCropper {

    private const val TAG = "VideoCropper"

    data class CropResult(
        val croppedVideoBitmap: Bitmap,
        val videoBounds: Rect,
        val originalWidth: Int,
        val originalHeight: Int,
        val wasCropApplied: Boolean,
        val cropDescription: String
    )

    /**
     * Identifies the Telegram video frame bounding box and produces a cropped bitmap.
     */
    fun extractVideoRegion(fullScreenshot: Bitmap): CropResult {
        val origW = fullScreenshot.width
        val origH = fullScreenshot.height

        if (origW < 100 || origH < 100) {
            return CropResult(
                croppedVideoBitmap = fullScreenshot,
                videoBounds = Rect(0, 0, origW, origH),
                originalWidth = origW,
                originalHeight = origH,
                wasCropApplied = false,
                cropDescription = "Too small for cropping"
            )
        }

        // 1. Detect Letterboxing/Pillarboxing and UI Bars
        // In Telegram Media Viewer (PhotoViewer / TextureView):
        // Top 8-12% is usually dark header bar with back arrow and channel title.
        // Bottom 10-18% is scrubber / caption / action bar.
        // Left/Right may have black pillarbox bars if video is vertical 9:16 or horizontal 16:9.

        val sampleStep = 8
        val thumbW = origW / sampleStep
        val thumbH = origH / sampleStep

        var topBoundary = 0
        var bottomBoundary = origH - 1
        var leftBoundary = 0
        var rightBoundary = origW - 1

        // Heuristic Boundary Detection:
        // Top UI exclusion (exclude top status bar + Telegram action bar ~ 12% if in portrait mode)
        val defaultTopMargin = (origH * 0.08f).toInt()
        val defaultBottomMargin = (origH * 0.12f).toInt()

        // Check for black letterbox borders
        var foundContentTop = false
        for (y in 0 until origH step 16) {
            var rowLumaSum = 0
            for (x in (origW * 0.2).toInt() until (origW * 0.8).toInt() step 16) {
                val pixel = fullScreenshot.getPixel(x, y)
                val luma = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)).toInt()
                rowLumaSum += luma
            }
            val avgLuma = rowLumaSum / max(1, ((origW * 0.6) / 16).toInt())
            if (avgLuma > 15 && !foundContentTop && y > defaultTopMargin) {
                topBoundary = y
                foundContentTop = true
                break
            }
        }

        if (!foundContentTop) {
            topBoundary = defaultTopMargin
        }

        var foundContentBottom = false
        for (y in (origH - 1) downTo defaultTopMargin step 16) {
            var rowLumaSum = 0
            for (x in (origW * 0.2).toInt() until (origW * 0.8).toInt() step 16) {
                val pixel = fullScreenshot.getPixel(x, y)
                val luma = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)).toInt()
                rowLumaSum += luma
            }
            val avgLuma = rowLumaSum / max(1, ((origW * 0.6) / 16).toInt())
            if (avgLuma > 15 && !foundContentBottom && y < (origH - defaultBottomMargin)) {
                bottomBoundary = y
                foundContentBottom = true
                break
            }
        }

        if (!foundContentBottom) {
            bottomBoundary = origH - defaultBottomMargin
        }

        // Clamp boundaries
        topBoundary = topBoundary.coerceIn(0, (origH * 0.35f).toInt())
        bottomBoundary = bottomBoundary.coerceIn((origH * 0.65f).toInt(), origH)

        val cropW = rightBoundary - leftBoundary + 1
        val cropH = bottomBoundary - topBoundary + 1

        val bounds = Rect(leftBoundary, topBoundary, rightBoundary, bottomBoundary)

        return try {
            val cropped = Bitmap.createBitmap(fullScreenshot, bounds.left, bounds.top, bounds.width(), bounds.height())
            CropResult(
                croppedVideoBitmap = cropped,
                videoBounds = bounds,
                originalWidth = origW,
                originalHeight = origH,
                wasCropApplied = true,
                cropDescription = "Cropped Telegram UI -> [${bounds.left},${bounds.top} to ${bounds.right},${bounds.bottom}] (${bounds.width()}x${bounds.height()})"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to crop video region, falling back to full bitmap", e)
            CropResult(
                croppedVideoBitmap = fullScreenshot,
                videoBounds = Rect(0, 0, origW, origH),
                originalWidth = origW,
                originalHeight = origH,
                wasCropApplied = false,
                cropDescription = "Crop fallback: Fullscreen"
            )
        }
    }

    /**
     * Resizes the cropped bitmap to target 224x224 while strictly preserving the aspect ratio
     * using letterboxing/pillarboxing (black pad) instead of stretching distortion.
     */
    fun resizePreservingAspectRatio(bitmap: Bitmap, targetSize: Int = 224): Bitmap {
        val srcW = bitmap.width
        val srcH = bitmap.height

        if (srcW == targetSize && srcH == targetSize) {
            return bitmap
        }

        val targetBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(targetBitmap)
        canvas.drawColor(Color.BLACK)

        val scale = min(targetSize.toFloat() / srcW, targetSize.toFloat() / srcH)
        val dstW = (srcW * scale).toInt()
        val dstH = (srcH * scale).toInt()

        val dstX = (targetSize - dstW) / 2
        val dstY = (targetSize - dstH) / 2

        val scaledSrc = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(scaledSrc, dstX.toFloat(), dstY.toFloat(), paint)

        if (scaledSrc != bitmap && !scaledSrc.isRecycled) {
            scaledSrc.recycle()
        }

        return targetBitmap
    }
}
