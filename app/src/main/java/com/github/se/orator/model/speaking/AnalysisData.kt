package com.github.se.orator.model.speaking

data class AnalysisData(
    val transcription: String = "",
    val fillerWordsCount: Int = -1,
    val averagePauseDuration: Double = -1.0,
    val sentimentScore: Double = 0.0,
    val talkTimePercentage: Double = -1.0,
    val talkTimeSeconds: Double = -1.0,
    var pace: Int = -1
    // Add other relevant fields
)
