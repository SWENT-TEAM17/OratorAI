package com.github.se.orator.ui.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Define the ChatGPTService interface
interface ChatGPTService {
  @Headers("Content-Type: application/json")
  @POST("v1/chat/completions")
  suspend fun getChatCompletion(@Body request: ChatRequest): ChatResponse
}

// Define the request body structure
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

// Define a data class for messages
data class Message(
    val role: String, // "user", "assistant", or "system"
    val content: String
)

// Define the response structure
data class ChatResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

enum class SessionType {
  PRACTICE,
  BATTLE
}

// Define the choices in the response
data class Choice(val index: Int, val message: Message, val finish_reason: String?)

// Define usage data
data class Usage(val prompt_tokens: Int, val completion_tokens: Int, val total_tokens: Int)

// Create a Retrofit instance with logging and API key header
fun createChatGPTService(apiKey: String, organizationId: String): ChatGPTService {
  // Configure logging for debugging network calls
  val logging = HttpLoggingInterceptor()
  logging.level = HttpLoggingInterceptor.Level.BODY

  // Create an OkHttpClient with logging and the API key header
  val client =
      OkHttpClient.Builder()
          .addInterceptor(logging)
          .addInterceptor { chain ->
            val request =
                chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Openai-Organization", organizationId)
                    .build()
            chain.proceed(request)
          }
          .build()

  // Create a Retrofit instance with the base URL and the OkHttpClient
  val retrofit =
      Retrofit.Builder()
          .baseUrl("https://api.openai.com/")
          .client(client)
          .addConverterFactory(GsonConverterFactory.create())
          .build()

  // Return an implementation of the ChatGPTService
  return retrofit.create(ChatGPTService::class.java)
}
