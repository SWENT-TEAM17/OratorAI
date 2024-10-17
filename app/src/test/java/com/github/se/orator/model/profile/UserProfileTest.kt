package com.github.se.orator.model.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileTest {

  @Test
  fun `UserProfile data class should hold correct values`() {
    val speechStats =
        SpeechStats(
            title = "Inauguration Speech",
            duration = 15,
            date = com.google.firebase.Timestamp.now(),
            accuracy = 98.5f,
            wordsPerMinute = 120)

    val userStatistics =
        UserStatistics(speechesGiven = 10, improvement = 20.0f, previousRuns = listOf(speechStats))

    val userProfile =
        UserProfile(
            uid = "user123",
            name = "John Doe",
            age = 30,
            profilePic = "http://example.com/john.jpg",
            statistics = userStatistics,
            friends = listOf("friend1", "friend2"),
            bio = "An experienced speaker.")

    assertEquals("user123", userProfile.uid)
    assertEquals("John Doe", userProfile.name)
    assertEquals(30, userProfile.age)
    assertEquals("http://example.com/john.jpg", userProfile.profilePic)
    assertEquals(userStatistics, userProfile.statistics)
    assertEquals(listOf("friend1", "friend2"), userProfile.friends)
    assertEquals("An experienced speaker.", userProfile.bio)
  }
}
