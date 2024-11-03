package com.github.se.orator.model.speaking

data class AnalysisData(
    val transcription: String,
    val fillerWordsCount: Int,
    val averagePauseDuration: Double,
    val sentimentScore: Double,
    // Add other relevant fields
)
