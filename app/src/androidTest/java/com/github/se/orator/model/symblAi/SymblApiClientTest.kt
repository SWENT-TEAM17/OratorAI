package com.github.se.orator.model.symblAi

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@RunWith(AndroidJUnit4::class)
class SymblApiClientTest {

  private lateinit var context: Context
  private lateinit var symblApiClient: SymblApiClient
  private lateinit var okHttpClient: OkHttpClient

  @Before
  fun setUp() {
    // Obtain the application context
    context = ApplicationProvider.getApplicationContext()
    okHttpClient = mock(OkHttpClient::class.java)

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

  @Test
  fun metricsReceived() {
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

    // Act
    symblApiClient.getTranscription(
        audioFile = audioFile,
        onSuccess = { data ->
          analysisData = data
          latch.countDown() // Signal that the operation is complete
        },
        onFailure = {
          latch.countDown() // Signal that the operation is complete
        })

    // Wait for up to 20 seconds for the operation to complete
    latch.await(20, TimeUnit.SECONDS)
    // Assert
    analysisData?.let {
      Assert.assertTrue(
          "Talk time (percentage) should have been received", it.talkTimePercentage != -1.0)
      Assert.assertTrue("Talk time (seconds) should have been received", it.talkTimeSeconds != -1.0)
      Assert.assertTrue("Pace should have been received", it.pace != -1)
    }
  }

  @Test
  fun messageParsingFailureCallsOnError() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val call = mock(Call::class.java)
    val file = mock(File::class.java)

    `when`(call.execute())
        .thenReturn(
            Response.Builder()
                .request(Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("application/json".toMediaType(), "Invalid JSON"))
                .build())

    `when`(okHttpClient.newCall(any())).thenReturn(call)

    symblApiClient.getTranscription(
        file,
        { Assert.fail("Should not have succeeded") },
        { Assert.assertTrue(it == SpeakingError.JSON_PARSING_ERROR) })
  }

  @Test
  fun requestFailsCausesOnFailureCall() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val file = mock(File::class.java)
    val callAccessToken = mock(Call::class.java)
    val callTranscription = mock(Call::class.java)

    `when`(callAccessToken.execute())
        .thenReturn(
            Response.Builder()
                .request(Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(
                    ResponseBody.create(
                        "application/json".toMediaType(), "{\"accessToken\":\"test\"}"))
                .build())

    `when`(callTranscription.execute())
        .thenReturn(
            Response.Builder()
                .request(Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .body(ResponseBody.create("application/json".toMediaType(), ""))
                .build())

    `when`(okHttpClient.newCall(any())).then {
      val request = it.arguments[0] as Request
      if (request.url.toString().contains("oauth2/token")) {
        callAccessToken
      } else {
        callTranscription
      }
    }

    symblApiClient.getTranscription(
        file,
        { Assert.fail("Should not have succeeded") },
        { Assert.assertTrue(it == SpeakingError.HTTP_REQUEST_ERROR) })
  }
}
