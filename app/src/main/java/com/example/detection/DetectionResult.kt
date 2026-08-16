package com.example.detection

enum class DetectionLevel {
    SAFE,
    POSSIBLY_ADULT,
    ADULT
}

data class DetectionResult(
    val level: DetectionLevel,
    val confidence: Float,
    val triggerReason: String,
    val detectedKeywords: List<String> = emptyList()
)
