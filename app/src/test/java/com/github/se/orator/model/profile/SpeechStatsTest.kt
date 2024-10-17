package com.github.se.orator.model.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechStatsTest {

  @Test
  fun `SpeechStats data class should hold correct values`() {
    val timestamp = com.google.firebase.Timestamp.now()
    val speechStats =
        SpeechStats(
            title = "Motivational Speech",
            duration = 20,
            date = timestamp,
            accuracy = 97.5f,
            wordsPerMinute = 110)

    assertEquals("Motivational Speech", speechStats.title)
    assertEquals(20, speechStats.duration)
    assertEquals(timestamp, speechStats.date)
    assertEquals(97.5f, speechStats.accuracy, 0.0f)
    assertEquals(110, speechStats.wordsPerMinute)
  }
}
