package com.example.policy

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.detection.DetectionLevel
import com.example.detection.DetectionResult
import com.example.detection.HeuristicContentDetector
import java.util.concurrent.ConcurrentHashMap

object InputProtectionEngine {

    private const val TAG = "InputProtectionEngine"
    private val recentlySanitizedTexts = ConcurrentHashMap<String, Long>()

    data class ProtectionActionResult(
        val isHandled: Boolean,
        val sanitizedText: String? = null,
        val detectionResult: DetectionResult? = null,
        val wasTextModified: Boolean = false
    )

    fun processInputProtection(
        service: AccessibilityService,
        rootNode: AccessibilityNodeInfo?,
        packageName: String,
        sensitivity: String,
        detector: HeuristicContentDetector
    ): ProtectionActionResult {
        if (rootNode == null) {
            return ProtectionActionResult(isHandled = false)
        }

        val editableNodes = mutableListOf<AccessibilityNodeInfo>()
        try {
            findEditableNodes(rootNode, editableNodes)

            if (editableNodes.isNotEmpty()) {
                for (node in editableNodes) {
                    val currentText = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
                    if (currentText.isBlank() || currentText.length < 2) continue

                    // Anti-recursion check: if this text was recently produced by our sanitization, skip
                    val cacheKey = "$packageName:$currentText"
                    val lastSanitizedTime = recentlySanitizedTexts[cacheKey]
                    if (lastSanitizedTime != null && (System.currentTimeMillis() - lastSanitizedTime) < 1200) {
                        continue
                    }

                    val detectionResult = detector.detectContent(listOf(currentText), packageName, sensitivity)
                    if (detectionResult.level == DetectionLevel.ADULT) {
                        Log.w(TAG, "[FG-INPUT] Adult text detected in input field ($packageName): '$currentText'")

                        val cleanText = sanitizeText(currentText, detectionResult.detectedKeywords)
                        val textToSet = if (cleanText == currentText) "" else cleanText

                        val arguments = Bundle().apply {
                            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToSet)
                        }

                        val actionSuccess = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        Log.i(TAG, "[FG-INPUT] ACTION_SET_TEXT result on $packageName: success=$actionSuccess, newText='$textToSet'")

                        if (textToSet.isNotBlank()) {
                            recentlySanitizedTexts["$packageName:$textToSet"] = System.currentTimeMillis()
                        }

                        if (!actionSuccess) {
                            // Fallback if node does not support ACTION_SET_TEXT directly
                            Log.w(TAG, "[FG-ACTION] ACTION_SET_TEXT failed, executing GLOBAL_ACTION_BACK")
                            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                        }

                        return ProtectionActionResult(
                            isHandled = true,
                            sanitizedText = textToSet,
                            detectionResult = detectionResult,
                            wasTextModified = true
                        )
                    }
                }
            }

            // Fallback for window content in input-protected apps (browsers, Telegram)
            val allTexts = mutableListOf<String>()
            extractAllNodeTexts(rootNode, allTexts)

            Log.d(TAG, "[FG-EXTRACT] App=$packageName | Total Extracted Nodes=${allTexts.size} | Texts=$allTexts")

            if (allTexts.isNotEmpty()) {
                val detectionResult = detector.detectContent(allTexts, packageName, sensitivity)
                Log.d(TAG, "[FG-DETECTOR] App=$packageName | Score=${detectionResult.confidence} | Level=${detectionResult.level} | Reason=${detectionResult.triggerReason}")

                if (detectionResult.level == DetectionLevel.ADULT) {
                    Log.w(TAG, "[FG-POLICY] App=$packageName | ContentType=WINDOW | RiskScore=${detectionResult.confidence} | Decision=BLOCK | Action=GLOBAL_ACTION_BACK")
                    val backRequested = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                    Log.i(TAG, "[FG-ACTION] GLOBAL_ACTION_BACK requested: success=$backRequested")

                    return ProtectionActionResult(
                        isHandled = true,
                        sanitizedText = null,
                        detectionResult = detectionResult,
                        wasTextModified = false
                    )
                }
            }

            return ProtectionActionResult(isHandled = false)
        } finally {
            // Safely recycle node copies
            editableNodes.forEach { node ->
                try {
                    node.recycle()
                } catch (_: Exception) {}
            }
        }
    }

    private fun findEditableNodes(node: AccessibilityNodeInfo, result: MutableList<AccessibilityNodeInfo>) {
        val className = node.className?.toString() ?: ""
        val isEditable = node.isEditable ||
                className.contains("EditText", ignoreCase = true) ||
                className.contains("AutoCompleteTextView", ignoreCase = true)

        if (isEditable) {
            result.add(AccessibilityNodeInfo.obtain(node))
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findEditableNodes(child, result)
            child.recycle()
        }
    }

    private fun extractAllNodeTexts(node: AccessibilityNodeInfo, textList: MutableList<String>) {
        node.text?.let { text ->
            val str = text.toString().trim()
            if (str.isNotBlank()) {
                textList.add(str)
            }
        }

        node.contentDescription?.let { desc ->
            val str = desc.toString().trim()
            if (str.isNotBlank() && !textList.contains(str)) {
                textList.add(str)
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractAllNodeTexts(child, textList)
            child.recycle()
        }
    }

    fun sanitizeText(originalText: String, keywords: List<String>): String {
        var sanitized = originalText
        for (kw in keywords) {
            val trimmedKw = kw.trim()
            if (trimmedKw.isEmpty()) continue

            val pattern = if (trimmedKw.length <= 3) {
                "(?i)\\b${Regex.escape(trimmedKw)}\\b"
            } else {
                "(?i)${Regex.escape(trimmedKw)}"
            }
            sanitized = sanitized.replace(Regex(pattern), "")
        }

        return sanitized.replace(Regex("\\s+"), " ").trim()
    }
}
