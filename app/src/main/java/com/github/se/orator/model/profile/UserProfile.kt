package com.github.se.orator.model.profile

import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.google.firebase.Timestamp

/**
 * Data class representing a user profile.
 *
 * @param uid The unique identifier of the user.
 * @param name The name of the user.
 * @param age The age of the user.
 * @param profilePic The URL of the user's profile picture.
 * @param statistics The statistics of the user.
 * @param allUsers A list of all users.
 * @param friends A list of friends.
 * @param bio The user's bio.
 */
data class UserProfile(
    val uid: String,
    val name: String,
    val age: Int,
    val profilePic: String? = null,
    val statistics: UserStatistics,
    // Temp list of all users for hardcoding
    val allUsers: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val sentReq: List<String> = emptyList(),
    val recReq: List<String> = emptyList(),
    val bio: String? = null,
    val currentStreak: Long = 0L,
    val lastLoginDate: String? = "1970-10-10"
)

/**
 * Enum class representing the type of session.
 *
 * @param positiveResponse The positive response for the session type.
 * @param negativeResponse The negative response for the session type.
 * @param successMessage The success messages
 * @param failureMessage The failure messages
 */
enum class SessionType(
    val positiveResponse: String,
    val negativeResponse: String,
    val successMessage: String,
    val failureMessage: String
) {
  SPEECH(
      positiveResponse = "you were effective",
      negativeResponse = "you were not effective",
      successMessage = "Great job! Your speech was effective.",
      failureMessage = "You might need to improve to achieve your speech's purpose."),
  INTERVIEW(
      positiveResponse = "would recommend hiring",
      negativeResponse = "would not recommend hiring",
      successMessage = "Congratulations! You would be hired.",
      failureMessage = "Unfortunately, you would not be hired."),
  NEGOTIATION(
      positiveResponse = "achieved your sales goal",
      negativeResponse = "did not achieve your sales goal",
      successMessage = "Success! You achieved your sales goal.",
      failureMessage = "You did not achieve your sales goal this time.")
}

/**
 * Data class representing the statistics of a user.
 *
 * @param sessionsGiven The number of sessions given by the user.
 * @param successfulSessions The number of successful sessions by the user.
 * @param improvement The improvement of the user.
 * @param battleStats The battle stats
 * @param previousRuns The list of previous runs.
 * @param recentData The list containing the recording data
 * @param talkTimeSecMean Mean of the last 10 talkTimeSeconds
 * @param paceMean Mean of the last 10 paces
 */
data class UserStatistics(
    val sessionsGiven: Map<String, Int> = SessionType.values().associate { it.name to 0 },
    val successfulSessions: Map<String, Int> = SessionType.values().associate { it.name to 0 },
    val improvement: Float = 0.0f,
    val battleStats: List<SpeechBattle> = emptyList(),
    val previousRuns: List<SpeechStats> = emptyList(),
    val recentData: List<AnalysisData> = emptyList(),
    val talkTimeSecMean: Double = 0.0,
    val paceMean: Double = 0.0
)

/**
 * Data class representing the statistics of a speech.
 *
 * @param title The title of the speech.
 * @param duration The duration of the speech.
 * @param date The date of the speech.
 * @param accuracy The accuracy of the speech.
 * @param wordsPerMinute The words per minute of the speech.
 */
data class SpeechStats(
    val title: String,
    val duration: Int,
    val date: Timestamp,
    val accuracy: Float,
    val wordsPerMinute: Int
    // Add more fields depending on API
)
