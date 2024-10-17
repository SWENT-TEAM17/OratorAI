package com.github.se.orator.model.symblAi

// SymblApiClient.kt

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.io.File
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

class SymblApiClient(private val context: Context) {

  // Variables to hold Symbl.ai credentials
  private var symblAppId: String? = null
  private var symblAppSecret: String? = null

  private var accessToken: String? = null

  // Variables to store results
  private var transcribedText: String = ""
  private var sentimentResult: String = ""
  private var fillersResult: String = ""
  private var insightsResult: String = ""

  interface SymblListener {
    fun onProcessingComplete(
        transcribedText: String,
        sentimentResult: String,
        fillersResult: String
    )

    fun onError(message: String)
  }

  private var listener: SymblListener? = null

  fun setListener(symblListener: SymblListener) {
    listener = symblListener
  }

  init {
    // Retrieve Symbl.ai credentials from the manifest
    val appInfo =
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    symblAppId = appInfo.metaData.getString("SYMBL_APP_ID")
    symblAppSecret = appInfo.metaData.getString("SYMBL_APP_SECRET")
  }

  // Function to get access token
  fun getAccessToken(onResult: (String?) -> Unit) {
    if (symblAppId.isNullOrEmpty() || symblAppSecret.isNullOrEmpty()) {
      onResult(null)
      listener?.onError("Symbl.ai credentials are missing.")
      return
    }

    val client = OkHttpClient()

    val url = "https://api.symbl.ai/oauth2/token:generate"

    val requestBody =
        FormBody.Builder()
            .add("type", "application")
            .add("appId", symblAppId!!)
            .add("appSecret", symblAppSecret!!)
            .build()

    val request = Request.Builder().url(url).post(requestBody).build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                listener?.onError("Failed to get access token: ${e.message}")
                onResult(null)
              }

              override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("Access Token Response", responseData ?: "No Response")
                if (response.isSuccessful && responseData != null) {
                  val json = JSONObject(responseData)
                  accessToken = json.getString("accessToken")
                  onResult(accessToken)
                } else {
                  listener?.onError("Failed to get access token: ${response.message}")
                  Log.e("Access Token Error", "Response: $responseData")
                  onResult(null)
                }
              }
            })
  }

  // Function to send the audio file to Symbl API
  fun sendAudioToSymbl(audioFile: File) {
    getAccessToken { token ->
      if (token != null) {
        val client = OkHttpClient()

        // Specify the correct media type for the audio file
        val mediaType = "audio/wav".toMediaTypeOrNull() // For WAV files
        val requestBody = audioFile.asRequestBody(mediaType)

        // Corrected API request URL with 'filler_words'
        val request =
            Request.Builder()
                .url(
                    "https://api.symbl.ai/v1/process/audio?enableAllInsights=true&insightTypes=filler_words")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

        client
            .newCall(request)
            .enqueue(
                object : Callback {
                  override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    listener?.onError("Request failed: ${e.message}")
                  }

                  override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string() ?: "No Response"
                    Log.d("Symbl Response", responseData)
                    if (response.isSuccessful) {
                      val jsonObject = JSONObject(responseData)
                      if (jsonObject.has("conversationId")) {
                        val conversationId = jsonObject.getString("conversationId")
                        // Start polling for messages
                        pollForMessages(conversationId, token)
                        // Start polling for filler words
                        pollForFillers(conversationId, token)
                      } else {
                        listener?.onError("No valid conversationId found. Response: $responseData")
                        Log.e("Symbl Error", "No conversationId: $responseData")
                      }
                    } else {
                      listener?.onError("Request failed: $responseData")
                      Log.e("Symbl Error", "Request failed: $responseData")
                    }
                  }
                })
      } else {
        listener?.onError("Cannot proceed without access token.")
      }
    }
  }

  // Polling function to check for message availability
  private fun pollForMessages(conversationId: String, accessToken: String, attempts: Int = 0) {
    val maxAttempts = 10
    val delayMillis = 2000L // Wait 2 seconds between attempts

    val client = OkHttpClient()

    val request =
        Request.Builder()
            .url(
                "https://api.symbl.ai/v1/conversations/$conversationId/messages?sentiment=true&enableAllInsights=true")
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                listener?.onError("Polling failed: ${e.message}")
              }

              override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""
                Log.d("Polling Response", responseData)

                if (response.isSuccessful && responseData.isNotEmpty()) {
                  val sentimentJson = JSONObject(responseData)
                  val messagesArray = sentimentJson.getJSONArray("messages")

                  if (messagesArray.length() > 0) {
                    // Messages are available, proceed to parse them
                    parseSentimentResponse(sentimentJson)
                  } else if (attempts < maxAttempts) {
                    // Messages not yet available, wait and try again
                    Log.d("Polling", "Messages not available yet. Attempt ${attempts + 1}")
                    Thread.sleep(delayMillis)
                    pollForMessages(conversationId, accessToken, attempts + 1)
                  } else {
                    // Max attempts reached, handle accordingly
                    listener?.onError("No messages found after polling.")
                  }
                } else {
                  listener?.onError("Polling failed: ${response.message}")
                  Log.e("Polling Error", "Response: $responseData")
                }
              }
            })
  }

  private fun parseSentimentResponse(sentimentJson: JSONObject) {
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

        // Store the results in class variables
        transcribedText = textBuilder.toString()
        sentimentResult = sentimentBuilder.toString()
        insightsResult = insightsBuilder.toString()

        listener?.onProcessingComplete(
            transcribedText = transcribedText,
            sentimentResult = sentimentResult,
            fillersResult = fillersResult // Fillers are handled separately
            )
      } else {
        listener?.onError("No messages found in the response.")
      }
    } catch (e: Exception) {
      listener?.onError("Error parsing response: ${e.message}")
      Log.e("Parsing Error", e.message ?: "Unknown error")
    }
  }

  // Polling function to check for filler words availability
  private fun pollForFillers(conversationId: String, accessToken: String, attempts: Int = 0) {
    val maxAttempts = 10
    val delayMillis = 2000L // Wait 2 seconds between attempts

    val client = OkHttpClient()

    val request =
        Request.Builder()
            .url(
                "https://api.symbl.ai/v1/conversations/$conversationId/insights?insightTypes=filler_words")
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

    client
        .newCall(request)
        .enqueue(
            object : Callback {
              override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                listener?.onError("Filler words polling failed: ${e.message}")
              }

              override fun onResponse(call: Call, response: Response) {
                val fillerData = response.body?.string() ?: ""
                Log.d("Filler Words Polling Response", fillerData)

                if (response.isSuccessful && fillerData.isNotEmpty()) {
                  val fillerJson = JSONObject(fillerData)
                  val insightsArray = fillerJson.getJSONArray("insights")

                  if (insightsArray.length() > 0) {
                    // Fillers are available, proceed to parse them
                    parseFillerResponse(fillerJson)
                  } else if (attempts < maxAttempts) {
                    // Fillers not yet available, wait and try again
                    Log.d("Filler Polling", "Fillers not available yet. Attempt ${attempts + 1}")
                    Thread.sleep(delayMillis)
                    pollForFillers(conversationId, accessToken, attempts + 1)
                  } else {
                    // Max attempts reached, handle accordingly
                    listener?.onError("No filler words found after polling.")
                  }
                } else {
                  listener?.onError("Filler words polling failed: ${response.message}")
                  Log.e("Filler Polling Error", "Response: $fillerData")
                }
              }
            })
  }

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

        // Notify listener
        listener?.onProcessingComplete(
            transcribedText = transcribedText,
            sentimentResult = sentimentResult,
            fillersResult = fillersResult)
      } else {
        fillersResult = "No filler words detected."

        listener?.onProcessingComplete(
            transcribedText = transcribedText,
            sentimentResult = sentimentResult,
            fillersResult = fillersResult)
      }
    } catch (e: Exception) {
      listener?.onError("Error parsing filler words: ${e.message}")
      Log.e("Filler Parsing Error", e.message ?: "Unknown error")
    }
  }

  // Getter for transcribed text
  fun getTranscribedText(): String {
    return transcribedText
  }

  // Getter for sentiment result
  fun getSentimentResult(): String {
    return sentimentResult
  }

  // Getter for fillers result
  fun getFillersResult(): String {
    return fillersResult
  }

  // Getter for insights result
  fun getInsightsResult(): String {
    return insightsResult
  }
}
