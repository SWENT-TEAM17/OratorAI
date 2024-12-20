package com.github.se.orator.model.symblAi

// SymblApiClient.kt

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONObject

private const val CLASS_LOG_ID = "SymblApiClient"

// Timeout duration for API calls
private const val TIMEOUT_DURATION = 20L

/**
 * The SymblApiClient class is responsible for making API calls to the Symbl.ai API.
 *
 * @param context The context of the application.
 * @param client The OkHttpClient instance to use for making API calls.
 */
class SymblApiClient(
    context: Context,
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS)
            .build()
) : VoiceAnalysisApi {

  // Variables to hold Symbl.ai credentials
  private var symblAppId: String

  private var symblAppSecret: String

  private var accessToken: String? = null

  // Variables to store results
  private var fillersResult: String = ""
  private var insightsResult: String = ""

  init {
    val appInfo =
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    try {
      symblAppId = appInfo.metaData.getString("SYMBL_APP_ID")!!
      symblAppSecret = appInfo.metaData.getString("SYMBL_APP_SECRET")!!
    } catch (e: Exception) {
      Log.e(CLASS_LOG_ID, e.message, e)
      throw e
    }
  }

  // Function to get access token
  private fun getAccessToken(onFailure: (SpeakingError) -> Unit) {
    if (symblAppId.isEmpty() || symblAppSecret.isEmpty()) {
      Log.e(CLASS_LOG_ID, "Symbl.ai credentials are missing or invalid")
      onFailure(SpeakingError.CREDENTIALS_ERROR)
      return
    }

    val url = "https://api.symbl.ai/oauth2/token:generate"

    val requestBody =
        FormBody.Builder()
            .add("type", "application")
            .add("appId", symblAppId)
            .add("appSecret", symblAppSecret)
            .build()

    val request = Request.Builder().url(url).post(requestBody).build()

    try {
      val response: Response = client.newCall(request).execute()

      val responseData = response.body?.string()
      Log.d(CLASS_LOG_ID, "Access token received")
      if (response.isSuccessful && responseData != null) {
        try {
          val json = JSONObject(responseData)
          accessToken = json.getString("accessToken")
        } catch (e: Exception) {
          Log.e(CLASS_LOG_ID, "Failed to parse access token", e)
          onFailure(SpeakingError.JSON_PARSING_ERROR)
        }
      } else {
        Log.e(CLASS_LOG_ID, "Failed to retrieve access token. Response: $responseData")
        onFailure(SpeakingError.ACCESS_TOKEN_ERROR)
      }
    } catch (e: Exception) {
      Log.e(CLASS_LOG_ID, "Failed to get access token", e)
      onFailure(SpeakingError.ACCESS_TOKEN_ERROR)
    }
  }

  private fun parseSentimentResponse(
      sentimentJson: JSONObject,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    try {
      val messagesArray = sentimentJson.getJSONArray("messages")
      if (messagesArray.length() > 0) {
        val textBuilder = StringBuilder()
        val sentimentBuilder = StringBuilder()
        val insightsBuilder = StringBuilder()

        for (i in 0 until messagesArray.length()) {
          val messageObject = messagesArray.getJSONObject(i)
          val messageText = messageObject.getString("text")

          // Log insights to verify if disfluencies are being returned
          val insights = messageObject.optJSONArray("insights")
          Log.d("Message Insights", insights?.toString() ?: "No insights")

          // Collect disfluencies (e.g., filler words) from insights
          val disfluencies = StringBuilder()
          insights?.let {
            for (j in 0 until it.length()) {
              val insightObject = it.getJSONObject(j)
              val insightType = insightObject.getString("type")
              Log.d("Insight Type", insightType) // Log insight type
              insightsBuilder.append("Insight: ${insightType}\n") // Collect insights
              if (insightType == "filler_word") { // Use the correct insight type
                val fillerWord = insightObject.getString("text")
                disfluencies.append("[$fillerWord] ") // Add disfluency with brackets
              }
            }
          }

          // Append the disfluencies before the transcribed message
          textBuilder.append("$disfluencies$messageText\n")

          val sentiment = messageObject.optJSONObject("sentiment")
          if (sentiment != null) {
            val polarity = sentiment.getString("suggested")
            val score = sentiment.getJSONObject("polarity").getDouble("score")
            sentimentBuilder.append("Sentiment: $polarity (Score: $score)\n")
          } else {
            sentimentBuilder.append("No sentiment data available.\n")
          }
        }

        insightsResult = insightsBuilder.toString()

        onSuccess(
            AnalysisData(
                transcription = textBuilder.toString(),
                sentimentScore =
                    sentimentJson
                        .getJSONArray("messages")
                        .getJSONObject(0)
                        .getJSONObject("sentiment")
                        .getJSONObject("polarity")
                        .getDouble("score")))
        Log.d("in symblai", "What you said is: $sentimentJson.getJSONArray(\"messages\")")
      } else {
        onFailure(SpeakingError.NO_MESSAGES_FOUND_ERROR)
        Log.e(CLASS_LOG_ID, "No messages found in the response.")
      }
    } catch (e: Exception) {
      Log.e(CLASS_LOG_ID, "Failed to parse the response: ${e.message}", e)
      onFailure(SpeakingError.JSON_PARSING_ERROR)
    }
  }

  /**
   * Function to parse the analytics response from the Symbl API.
   *
   * @param analyticsJSONObject The JSON object containing the analytics data.
   * @param onSuccess The function to be called on success. The `AnalysisData` object passed is set
   *   with non-default values for fields `talkTimePercentage`, `talkTimeSeconds`, and `pace`.
   */
  private fun parseAnalyticsResponse(
      analyticsJSONObject: JSONObject,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    try {
      val userAnalyticsArray = analyticsJSONObject.getJSONArray("members")

      if (userAnalyticsArray.length() != 1) {
        onFailure(SpeakingError.NO_ANALYTICS_FOUND_ERROR)
        Log.e(CLASS_LOG_ID, "No analytics found in the response.")
        return
      }

      val userAnalytics = userAnalyticsArray.getJSONObject(0)

      // Pace
      val pace = userAnalytics.getJSONObject("pace").getInt("wpm")
      // Talk time
      val talkTimePercentage = userAnalytics.getJSONObject("talkTime").getDouble("percentage")
      val talkTimeSeconds = userAnalytics.getJSONObject("talkTime").getDouble("seconds")

      // Pass the result on success
      onSuccess(
          AnalysisData(
              talkTimePercentage = talkTimePercentage,
              talkTimeSeconds = talkTimeSeconds,
              pace = pace))
    } catch (e: Exception) {
      Log.e(
          CLASS_LOG_ID,
          "Failed to parse the response while trying to retrieve analytics: ${e.message}",
          e)
      onFailure(SpeakingError.JSON_PARSING_ERROR)
    }
  }

  /**
   * Function to get the transcription of an audio file.
   *
   * @param audioFile The audio file to be transcribed.
   * @param onSuccess The function to be called on success.
   * @param onFailure The function to be called on failure.
   */
  override fun getTranscription(
      audioFile: File,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    getAccessToken(onFailure)
    accessToken ?: return // Return if access token is null

    // Specify the correct media type for the audio file
    val mediaType = "audio/wav".toMediaTypeOrNull() // For WAV files
    val requestBody = audioFile.asRequestBody(mediaType)

    // Corrected API request URL with 'filler_words'
    val request =
        buildUrlPostRequest(
            url =
                "https://api.symbl.ai/v1/process/audio?enableAllInsights=true&insightTypes=filler_words",
            accessToken = accessToken,
            requestBody = requestBody)

    urlCallRequest(
        request = request,
        onSuccess = { response ->
          val jsonObject = JSONObject(response)
          if (jsonObject.has("conversationId")) {
            val conversationId = jsonObject.getString("conversationId")
            val jobId = jsonObject.getString("jobId")

            Log.d(CLASS_LOG_ID, "Job started. Waiting for completion...")

            var status: String?
            do {
              status = getJobStatus(jobId, onFailure)
              Thread.sleep(2000)
            } while (status == "in_progress")

            if (status == "completed") {
              fetchAnalysis(conversationId, onSuccess, onFailure)
              // pollForFillers(conversationId, accessToken!!)
            } else {
              Log.e(CLASS_LOG_ID, "Job failed to complete.")
              onFailure(SpeakingError.JOB_PROCESSING_ERROR)
            }
          } else {
            Log.e(CLASS_LOG_ID, "No conversationId: $response")
            onFailure(SpeakingError.MISSING_CONV_ID_ERROR)
          }
        },
        onFailure = { _ ->
          Log.e(CLASS_LOG_ID, "Request failed")
          onFailure(SpeakingError.HTTP_REQUEST_ERROR)
        })
  }

  private fun fetchAnalysis(
      conversationId: String,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {

    try {

      var tempAnalysisData: AnalysisData? = null
      urlCallRequestBlocking(
          request =
              buildUrlGetRequestWithHeader(
                  "https://api.symbl.ai/v1/conversations/$conversationId/messages?sentiment=true&enableAllInsights=true",
                  accessToken),
          onSuccess = { response ->
            parseSentimentResponse(JSONObject(response), { tempAnalysisData = it }, onFailure)
          },
          onFailure = {
            Log.e(CLASS_LOG_ID, "Failed to fetch message data online")
            onFailure(SpeakingError.HTTP_REQUEST_ERROR)
          })

      if (tempAnalysisData == null) {
        Log.e(
            CLASS_LOG_ID, "Failed to fetch message data online (analysisData is unexpectedly null)")
        onFailure(SpeakingError.HTTP_REQUEST_ERROR)
        return
      }

      Log.d(CLASS_LOG_ID, "Successfully parsed speech's transcription and sentiment data")

      urlCallRequestBlocking(
          request =
              buildUrlGetRequestWithHeader(
                  "https://api.symbl.ai/v1/conversations/$conversationId/analytics", accessToken),
          onSuccess = { response ->
            parseAnalyticsResponse(
                JSONObject(response),
                {
                  tempAnalysisData =
                      tempAnalysisData!!.copy(
                          talkTimePercentage = it.talkTimePercentage,
                          talkTimeSeconds = it.talkTimeSeconds,
                          pace = it.pace)
                },
                onFailure)
          },
          onFailure = {
            Log.e(CLASS_LOG_ID, "Failed to fetch analysis data online)")
            onFailure(SpeakingError.HTTP_REQUEST_ERROR)
          })

      if (tempAnalysisData!!.pace ==
          -1) { // Meaning the processing of analytics data was unsuccessful
        return
      }

      Log.d(CLASS_LOG_ID, "Successfully parsed speech's analytics data: $tempAnalysisData")

      onSuccess(tempAnalysisData!!)
    } catch (e: Exception) {
      Log.e("Failed to parse response", e.message, e)
      onFailure(SpeakingError.JSON_PARSING_ERROR)
    }
  }

  /**
   * Function to get the current state of the job.
   *
   * @param jobId The ID of the job to check.
   * @param onFailure The function to be called on failure.
   * @return The status (String?) of the job : either "completed", "failed", "in_progress" or null
   *   if the call to the api was unsuccessful.
   */
  private fun getJobStatus(jobId: String, onFailure: (SpeakingError) -> Unit): String? {
    var status: String? = null

    urlCallRequestBlocking(
        request = buildUrlGetRequest("https://api.symbl.ai/v1/job/$jobId"),
        onSuccess = { response ->
          try {
            val jsonObject = JSONObject(response)
            status = jsonObject.getString("status")
          } catch (e: Exception) {
            status = null
          }
        },
        onFailure = onFailure)

    return status
  }

  /**
   * Builds a URL get request.
   *
   * @param url The URL to build the request for.
   */
  private fun buildUrlGetRequest(url: String): Request {
    return Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .get()
        .build()
  }

  /**
   * Builds a URL post request with a body, a header and using an access token.
   *
   * @param url The URL to build the request for.
   * @param accessToken The access token to use for the request.
   * @param requestBody The request body to use for the request.
   */
  private fun buildUrlPostRequest(
      url: String,
      accessToken: String?,
      requestBody: RequestBody
  ): Request {
    return Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .post(requestBody)
        .build()
  }

  /**
   * Builds a URL get request with a header and using an access token.
   *
   * @param url The URL to build the request for.
   * @param accessToken The access token to use for the request.
   */
  private fun buildUrlGetRequestWithHeader(url: String, accessToken: String?): Request {
    return Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer $accessToken")
        .get()
        .build()
  }

  /**
   * Makes a URL call request (to the Symbl API).
   *
   * @param request The request to be made.
   * @param onSuccess The function to be called on success.
   * @param onFailure The function to be called on failure.
   */
  private fun urlCallRequest(
      request: Request,
      onSuccess: (String) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                Log.e(CLASS_LOG_ID, "Failed to fetch data", e)
                onFailure(SpeakingError.HTTP_REQUEST_ERROR)
              }

              override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: "No Response"
                Log.d(CLASS_LOG_ID, responseData)
                if (response.isSuccessful) {
                  onSuccess(responseData)
                } else {
                  onFailure(SpeakingError.HTTP_REQUEST_ERROR)
                  Log.e(CLASS_LOG_ID, "HTTP request failed: $responseData")
                }
              }
            })
  }

  /**
   * Makes a blocking URL call request (to the Symbl API).
   *
   * @param request The request to be made.
   * @param onSuccess The function to be called on success.
   * @param onFailure The function to be called on failure.
   */
  private fun urlCallRequestBlocking(
      request: Request,
      onSuccess: (String) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    try {
      client.newCall(request).execute().use { response ->
        val responseData = response.body?.string() ?: "No Response"
        Log.d(CLASS_LOG_ID, responseData)
        if (response.isSuccessful && responseData.isNotEmpty()) {
          onSuccess(responseData)
        } else {
          onFailure(SpeakingError.HTTP_REQUEST_ERROR)
          Log.e(CLASS_LOG_ID, "HTTP request failed: $responseData")
        }
      }
    } catch (e: IOException) {
      Log.e(CLASS_LOG_ID, "Failed to fetch data", e)
      onFailure(SpeakingError.HTTP_REQUEST_ERROR)
    }
  }
}
