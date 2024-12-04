package com.github.se.orator.model.speaking

import android.util.Log

data class AnalysisData(
    val transcription: String = "",
    val fillerWordsCount: Int = -1,
    val averagePauseDuration: Double = -1.0,
    val sentimentScore: Double = 0.0,
    val talkTimePercentage: Double = -1.0,
    val talkTimeSeconds: Double = -1.0,
    var pace: Int = -1
) {
    /**
     * Validates the AnalysisData object to ensure all fields meet expected constraints.
     *
     * @return True if the object is valid, false otherwise.
     */
    fun isValid(): Boolean {
        if (transcription.isBlank()) {
            Log.e("AnalysisData", "Invalid transcription: cannot be blank.")
            return false
        }
        if (fillerWordsCount < 0) {
            Log.e("AnalysisData", "Invalid fillerWordsCount: cannot be negative.")
            return false
        }
        if (averagePauseDuration < 0.0) {
            Log.e("AnalysisData", "Invalid averagePauseDuration: cannot be negative.")
            return false
        }
        if (talkTimePercentage < 0.0 || talkTimePercentage > 100.0) {
            Log.e("AnalysisData", "Invalid talkTimePercentage: must be between 0 and 100.")
            return false
        }
        if (talkTimeSeconds < 0.0) {
            Log.e("AnalysisData", "Invalid talkTimeSeconds: cannot be negative.")
            return false
        }
        if (pace < 0) {
            Log.e("AnalysisData", "Invalid pace: cannot be negative.")
            return false
        }
        // Add other field validations as necessary

        return true
    }
}
