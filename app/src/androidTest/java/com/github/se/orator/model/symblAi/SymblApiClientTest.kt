package com.github.se.orator.model.symblAi

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class SymblApiClientTest {

  private lateinit var context: Context
  private lateinit var symblApiClient: SymblApiClient
  private lateinit var okHttpClient: OkHttpClient

  private lateinit var callAccessToken: Call
  private lateinit var callFailure: Call
  private lateinit var callJobStatus: Call
  private lateinit var callSendFile: Call
  private lateinit var callTranscription: Call
  private lateinit var callAnalyticsInvalidSize: Call
  private lateinit var callAnalyticsInvalidContent: Call

  private val responseAccessToken = makeResponse(200, "OK", "{\"accessToken\":\"test\"}")
  private val responseFailure = makeResponse(500, "Internal Server Error", "")
  private val responseJobStatus = makeResponse(200, "OK", "{\"status\":\"completed\"}")
  private val responseSendFile =
      makeResponse(200, "OK", "{\"conversationId\":\"0\", \"jobId\":\"0\"}")
  private val responseTranscription =
      makeResponse(
          200,
          "OK",
          "{\"messages\": [{\"text\": \"test text\", \"sentiment\": {\"polarity\": {\"score\": 0.5}, \"suggested\": \"neutral\"}}]}")
  private val responseAnalyticsInvalidArraySize = makeResponse(200, "OK", "{\"members\": []}")
  private val responseAnalyticsInvalidContent = makeResponse(200, "OK", "{\"members\": [{}]}")

  @Before
  fun setUp() {
    // Obtain the application context
    context = ApplicationProvider.getApplicationContext()
    okHttpClient = mock(OkHttpClient::class.java)
    callAccessToken = mock(Call::class.java)
    callFailure = mock(Call::class.java)
    callJobStatus = mock(Call::class.java)
    callSendFile = mock(Call::class.java)
    callTranscription = mock(Call::class.java)
    callAnalyticsInvalidSize = mock(Call::class.java)
    callAnalyticsInvalidContent = mock(Call::class.java)

    `when`(callAccessToken.execute()).thenReturn(responseAccessToken)

    `when`(callFailure.execute()).thenReturn(responseFailure)
    `when`(callFailure.enqueue(any())).then {
      val callback = it.arguments[0] as okhttp3.Callback
      callback.onFailure(callFailure, IOException("Test exception"))
    }

    `when`(callJobStatus.execute()).thenReturn(responseJobStatus)

    `when`(callSendFile.enqueue(any())).then {
      val callback = it.arguments[0] as okhttp3.Callback
      callback.onResponse(callSendFile, responseSendFile)
    }

    `when`(callTranscription.execute()).thenReturn(responseTranscription)

    `when`(callAnalyticsInvalidSize.execute()).thenReturn(responseAnalyticsInvalidArraySize)

    `when`(callAnalyticsInvalidContent.execute()).thenReturn(responseAnalyticsInvalidContent)

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
            Thank you for reaching out to us.
            All lines are currently busy.
            Your call is very important to us.
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
  fun tokenParsingFailureCallsOnError() {
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
                .body("Invalid JSON".toResponseBody("application/json".toMediaType()))
                .build())

    `when`(okHttpClient.newCall(any())).thenReturn(call)

    var retrieval: SpeakingError? = null

    symblApiClient.getTranscription(
        file, { Assert.fail("Should not have succeeded") }, { retrieval = it })

    Assert.assertTrue(retrieval == SpeakingError.JSON_PARSING_ERROR)
  }

  @Test
  fun requestFailsCausesOnFailureCall() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val file = mock(File::class.java)

    `when`(okHttpClient.newCall(any())).then {
      val request = it.arguments[0] as Request
      if (request.url.toString().contains("oauth2/token")) {
        callAccessToken
      } else {
        callFailure
      }
    }

    var retrieval: SpeakingError? = null

    symblApiClient.getTranscription(
        file, { Assert.fail("Should not have succeeded") }, { retrieval = it })

    // Verify that the call to send the file is made
    verify(callFailure).enqueue(any())

    Assert.assertTrue(retrieval == SpeakingError.HTTP_REQUEST_ERROR)
  }

  @Test
  fun transcriptionAndSentimentFailToRetrieveCallsOnFailure() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val file = mock(File::class.java)

    `when`(okHttpClient.newCall(any())).then {
      val request = it.arguments[0] as Request
      if (request.url.toString().contains("oauth2/token")) {
        callAccessToken
      } else if (request.url.toString().contains("process")) {
        callSendFile
      } else if (request.url.toString().contains("job")) {
        callJobStatus
      } else {
        callFailure
      }
    }

    var retrieval: SpeakingError? = null

    symblApiClient.getTranscription(
        file, { Assert.fail("Should not have succeeded") }, { retrieval = it })

    // Verify that the call to retrieve the transcription is made
    verify(callFailure).execute()

    Assert.assertTrue(retrieval == SpeakingError.HTTP_REQUEST_ERROR)
  }

  @Test
  fun transcriptionAndAnalyticsFailToRetrieveCallsOnFailure() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val file = mock(File::class.java)

    `when`(okHttpClient.newCall(any())).then {
      val request = it.arguments[0] as Request
      if (request.url.toString().contains("oauth2/token")) {
        callAccessToken
      } else if (request.url.toString().contains("process")) {
        callSendFile
      } else if (request.url.toString().contains("job")) {
        callJobStatus
      } else if (request.url.toString().contains("messages")) {
        callTranscription
      } else {
        callFailure
      }
    }

    var retrieval: SpeakingError? = null

    symblApiClient.getTranscription(
        file, { Assert.fail("Should not have succeeded") }, { retrieval = it })

    // Verify that the calls to retrieve the transcription and then the analysis fields
    // were indeed made
    verify(callTranscription).execute()
    verify(callFailure).execute()

    Assert.assertTrue(retrieval == SpeakingError.HTTP_REQUEST_ERROR)
  }

  @Test
  fun analyticsArrayInvalidCallsOnFailure() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val file = mock(File::class.java)

    `when`(okHttpClient.newCall(any())).then {
      val request = it.arguments[0] as Request
      if (request.url.toString().contains("oauth2/token")) {
        callAccessToken
      } else if (request.url.toString().contains("process")) {
        callSendFile
      } else if (request.url.toString().contains("job")) {
        callJobStatus
      } else if (request.url.toString().contains("messages")) {
        callTranscription
      } else {
        callAnalyticsInvalidSize
      }
    }

    var retrieval: SpeakingError? = null

    symblApiClient.getTranscription(
        file, { Assert.fail("Should not have succeeded") }, { retrieval = it })

    // Verify that the call to retrieve the analytics was made
    verify(callAnalyticsInvalidSize).execute()

    Assert.assertTrue(retrieval == SpeakingError.NO_ANALYTICS_FOUND_ERROR)
  }

  @Test
  fun analyticsArrayInvalidContentCallsOnFailure() {
    symblApiClient = SymblApiClient(context, okHttpClient)

    val file = mock(File::class.java)

    `when`(okHttpClient.newCall(any())).then {
      val request = it.arguments[0] as Request
      if (request.url.toString().contains("oauth2/token")) {
        callAccessToken
      } else if (request.url.toString().contains("process")) {
        callSendFile
      } else if (request.url.toString().contains("job")) {
        callJobStatus
      } else if (request.url.toString().contains("messages")) {
        callTranscription
      } else {
        callAnalyticsInvalidContent
      }
    }

    var retrieval: SpeakingError? = null

    symblApiClient.getTranscription(
        file, { Assert.fail("Should not have succeeded") }, { retrieval = it })

    // Verify that the call to retrieve the analytics was made
    verify(callAnalyticsInvalidContent).execute()

    Assert.assertTrue(retrieval == SpeakingError.JSON_PARSING_ERROR)
  }

  private fun makeResponse(code: Int, message: String, body: String): Response {
    return Response.Builder()
        .request(Request.Builder().url("http://localhost").build())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(message)
        .body(body.toResponseBody("application/json".toMediaType()))
        .build()
  }
}
