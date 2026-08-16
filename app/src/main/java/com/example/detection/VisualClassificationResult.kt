package com.example.detection

enum class VisualClassification {
    SAFE,
    REVIEW,
    HIGH_RISK
}

data class VisualClassificationResult(
    val classification: VisualClassification,
    val confidence: Float,
    val inferenceTimeMs: Long,
    val featuresDetected: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val frameHash: Long = 0L
)
