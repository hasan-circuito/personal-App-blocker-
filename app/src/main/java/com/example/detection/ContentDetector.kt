package com.example.detection

interface ContentDetector {
    fun detectContent(
        nodesText: List<String>,
        packageName: String,
        sensitivity: String
    ): DetectionResult
}
