package com.github.se.orator.model.symblAi

// SymblApiClient.kt

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

class SymblApiClient(context: Context) : VoiceAnalysisApi {

  // Variables to hold Symbl.ai credentials
  private lateinit var symblAppId: String

  private lateinit var symblAppSecret: String

  private var accessToken: String? = null

  // Variables to store results
  private var transcribedText: String = ""
  private var sentimentResult: String = ""
  private var fillersResult: String = ""
  private var insightsResult: String = ""

  init {
    val appInfo =
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    try {
      symblAppId = appInfo.metaData.getString("SYMBL_APP_ID")!!
      symblAppSecret = appInfo.metaData.getString("SYMBL_APP_SECRET")!!
    } catch (e: Exception) {
      Log.e("SymblApiClient", "Error getting Symbl.ai credentials", e)
    }
  }

  // Function to get access token
  fun getAccessToken(onFailure: (Exception) -> Unit) {
    if (symblAppId.isNullOrEmpty() || symblAppSecret.isNullOrEmpty()) {
      Log.e("Symbl Error", "Symbl.ai credentials not found.")
      return
    }

    val client = OkHttpClient()

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
      Log.d("Access Token Response", responseData ?: "No Response")
      if (response.isSuccessful && responseData != null) {
        val json = JSONObject(responseData)
        accessToken = json.getString("accessToken")
      } else {
        Log.e("Access Token Error", "Response: $responseData")
        onFailure(Exception("Failed to get access token: ${response.message}"))
      }
    } catch (e: Exception) {
      onFailure(e)
      Log.e("Symbl Error", "Failed to get access token", e)
    }
  }

  private fun parseSentimentResponse(
      sentimentJson: JSONObject,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (Exception) -> Unit
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
          textBuilder.append("You said: $disfluencies$messageText\n")

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
                fillerWordsCount = -1,
                averagePauseDuration = -1.0,
                sentimentScore =
                    sentimentJson
                        .getJSONArray("messages")
                        .getJSONObject(0)
                        .getJSONObject("sentiment")
                        .getJSONObject("polarity")
                        .getDouble("score")))
      } else {
        onFailure(Exception("No messages found in the response."))
        Log.e("Symbl Error", "No messages found in the response.")
      }
    } catch (e: Exception) {
      onFailure(e)
      Log.e("Parsing Error", e.message ?: "Unknown error")
    }
  }

  // In phase of being modified
  private fun parseFillerResponse(fillerJson: JSONObject) {
    try {
      val insightsArray = fillerJson.getJSONArray("insights")
      if (insightsArray.length() > 0) {
        val fillersBuilder = StringBuilder()

        for (i in 0 until insightsArray.length()) {
          val insightObject = insightsArray.getJSONObject(i)
          val fillerWord = insightObject.getString("text")
          fillersBuilder.append("Filler word: $fillerWord\n")
        }

        // Store the fillers result
        fillersResult = fillersBuilder.toString()

        // These will be fixed later on

        /*// Notify listener
          listener?.onProcessingComplete(
              transcribedText = transcribedText,
              sentimentResult = sentimentResult,
              fillersResult = fillersResult)
        } else {
          fillersResult = "No filler words detected."

          listener?.onProcessingComplete(
              transcribedText = transcribedText,
              sentimentResult = sentimentResult,
              fillersResult = fillersResult)*/
      }
    } catch (e: Exception) {
      /*
      listener?.onError("Error parsing filler words: ${e.message}")
      Log.e("Filler Parsing Error", e.message ?: "Unknown error")
       */
    }
  }

  override fun getTranscription(
      audioFile: File,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getAccessToken({})
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

            Log.d("SymblAi", "Job started. Waiting for completion...")
            while (getJobStatus(jobId) == "in_progress") {
              Thread.sleep(2000)
            }

            if (getJobStatus(jobId) == "completed") {
              fetchAnalysis(conversationId, onSuccess, onFailure)
              // pollForFillers(conversationId, accessToken!!)
            } else {
              onFailure(Exception("Job failed to complete."))
              Log.e("Symbl Error", "Job failed to complete.")
            }
            // Start polling for filler words
          } else {
            onFailure(Exception("No valid conversationId found. Response: $response"))
            Log.e("Symbl Error", "No conversationId: $response")
          }
        },
        onFailure = { e ->
          onFailure(e)
          Log.e("Symbl Error", "Request failed: ${e.message}", e)
        })
  }

  private fun fetchAnalysis(
      conversationId: String,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    urlCallRequest(
        request =
            buildUrlGetRequestWithHeader(
                "https://api.symbl.ai/v1/conversations/$conversationId/messages?sentiment=true&enableAllInsights=true",
                accessToken),
        onSuccess = { response ->
          parseSentimentResponse(JSONObject(response), onSuccess, onFailure)
        },
        onFailure = { onFailure(it) })
  }

  /**
   * Function to get the current state of the job.
   *
   * @param jobId The ID of the job to check.
   * @return The status (String?) of the job : either "completed", "failed", "in_progress" or null
   *   if the call to the api was unsuccessful.
   */
  private fun getJobStatus(jobId: String): String? {
    var status: String? = null

    urlCallRequestBlocking(
        request = buildUrlGetRequest("https://api.symbl.ai/v1/job/$jobId"),
        onSuccess = { response ->
          val jsonObject = JSONObject(response)
          status = jsonObject.getString("status")
        },
        onFailure = {})

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
      onFailure: (Exception) -> Unit
  ) {
    val client = OkHttpClient()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                onFailure(e)
                Log.e("Symbl Error", "Failed to fetch data", e)
              }

              override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: "No Response"
                Log.d("Symbl Response", responseData)
                if (response.isSuccessful) {
                  onSuccess(responseData)
                } else {
                  onFailure(Exception("Request failed: $responseData"))
                  Log.e("Symbl Error", "Request failed: $responseData")
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
      onFailure: (Exception) -> Unit
  ) {
    val client = OkHttpClient()

    client.newCall(request).execute().use { response ->
      val responseData = response.body?.string() ?: "No Response"
      Log.d("Symbl Response", responseData)
      if (response.isSuccessful) {
        onSuccess(responseData)
      } else {
        onFailure(Exception("Request failed: $responseData"))
        Log.e("Symbl Error", "Request failed: $responseData")
      }
    }
  }
}
