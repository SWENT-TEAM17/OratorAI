package com.github.se.orator.model.profile

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String,
    val name: String,
    val age: Int,
    val profilePic: String? = null,
    val statistics: UserStatistics,
    // Temp list of all users for hardcoding
    val allUsers: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val bio: String? = null,
    val currentStreak: Long = 0L,
    val lastLoginDate: String? = "1970-10-10"
)

data class UserStatistics(
    val speechesGiven: Int = 0,
    val improvement: Float = 0.0f,
    val previousRuns: List<SpeechStats> = emptyList()
)

data class SpeechStats(
    val title: String,
    val duration: Int,
    val date: Timestamp,
    val accuracy: Float,
    val wordsPerMinute: Int
    // Add more fields depending on API
)
