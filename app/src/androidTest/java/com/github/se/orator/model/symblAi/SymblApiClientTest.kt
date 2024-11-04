package com.github.se.orator.model.symblAi

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SymblApiClientTest {

  private lateinit var context: Context
  private lateinit var symblApiClient: SymblApiClient

  @Before
  fun setUp() {
    // Obtain the application context
    context = ApplicationProvider.getApplicationContext()

    // Initialize SymblApiClient with the context
    symblApiClient = SymblApiClient(context)
  }

  @Test
  fun testGetTranscription() {
    // Arrange
    val assetManager = context.assets

    // Name of the audio file in the assets directory
    val audioFileName = "test_audio.wav"

    // Copy the audio file from assets to a temporary file
    val inputStream = assetManager.open(audioFileName)
    val audioFile = File(context.cacheDir, audioFileName)
    inputStream.use { input -> audioFile.outputStream().use { output -> input.copyTo(output) } }

    // Prepare a CountDownLatch to wait for asynchronous callback
    val latch = CountDownLatch(1)

    // Variables to capture the results
    var analysisData: AnalysisData? = null
    var failureError: SpeakingError? = null

    // Act
    symblApiClient.getTranscription(
        audioFile = audioFile,
        onSuccess = { data ->
          analysisData = data
          latch.countDown() // Signal that the operation is complete
        },
        onFailure = { error ->
          failureError = error
          latch.countDown() // Signal that the operation is complete
        })

    // Wait for up to 120 seconds for the operation to complete
    val completed = latch.await(120, TimeUnit.SECONDS)

    // Assert
    Assert.assertTrue("Test timed out", completed)
    Assert.assertNull("Expected no error but got: $failureError", failureError)
    Assert.assertNotNull("AnalysisData should not be null", analysisData)

    // Additional assertions on analysisData
    analysisData?.let { data ->
      Log.d("SymblApiClientTest", "Transcription: ${data.transcription}")
      Log.d("SymblApiClientTest", "Sentiment Score: ${data.sentimentScore}")

      // You can add assertions based on expected values
      Assert.assertTrue(
          "Sentiment score should be between -1 and 1", data.sentimentScore in -1.0..1.0)
      Assert.assertTrue("Transcription should not be empty", data.transcription.isNotEmpty())

      val expectedTranscription =
          """
            You said: Thank you for reaching out to us.
            You said: All lines are currently busy.
            You said: Your call is very important to us.
        """
              .trimIndent()

      // Log the actual transcription for debugging
      Log.d("SymblApiClientTest", "Transcription: ${data.transcription}")

      // Assert that the transcription matches the expected transcription
      Assert.assertEquals(
          "Transcription does not match expected",
          expectedTranscription.trim(),
          data.transcription.trim())
    }
  }
}
