package com.github.se.orator.model.chatGPT

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PracticeContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatRequest
import com.github.se.orator.ui.network.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatGPTService: ChatGPTService,
    val practiceContext: PracticeContext,
    val feedbackType: String
) : ViewModel() {

  private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
  val chatMessages = _chatMessages.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage = _errorMessage.asStateFlow()

  private val collectedAnalysisData = mutableListOf<AnalysisData>()

  init {
    initializeConversation()
  }

  fun initializeConversation() {
    val systemMessageContent =
        when (practiceContext) {
          is InterviewContext ->
              """
                You are simulating a ${practiceContext.interviewType} for the position of ${practiceContext.role} at ${practiceContext.company}. 
                Focus on the following areas: ${practiceContext.focusAreas.joinToString(", ")}. 
                Ask questions one at a time and wait for the user's response before proceeding. 
                Do not provide feedback until the end.
            """
                  .trimIndent()
          is PublicSpeakingContext ->
              """
                You are helping the user prepare a speech for a ${practiceContext.occasion}. 
                The audience is ${practiceContext.audienceDemographic}. 
                The main points of the speech are: ${practiceContext.mainPoints.joinToString(", ")}.
                Please guide the user through practicing their speech, asking for their input on each point.
            """
                  .trimIndent()
          is SalesPitchContext ->
              """
                You are helping the user prepare a sales pitch for the product ${practiceContext.product}. 
                The target audience is ${practiceContext.targetAudience}. 
                The key features of the product are: ${practiceContext.keyFeatures.joinToString(", ")}.
                Please guide the user through practicing their sales pitch, asking for their input on each feature.
            """
                  .trimIndent()
          // Add cases for other context types like SalesPitchContext
          else -> "You are assisting the user with their speaking practice."
        }

    val systemMessage = Message(role = "system", content = systemMessageContent)

    val userStartMessage = Message(role = "user", content = "I'm ready to begin the interview.")

    _chatMessages.value = listOf(systemMessage, userStartMessage)

    getNextGPTResponse()
  }

  fun sendUserResponse(transcript: String, analysisData: AnalysisData) {
    val userMessage = Message(role = "user", content = transcript)
    _chatMessages.value = _chatMessages.value + userMessage

    collectedAnalysisData.add(analysisData)

    getNextGPTResponse()
  }

  private fun getNextGPTResponse() {
    viewModelScope.launch {
      try {
        _isLoading.value = true

        val request = ChatRequest(model = "gpt-3.5-turbo", messages = _chatMessages.value)

        val response = chatGPTService.getChatCompletion(request)

        response.choices.firstOrNull()?.message?.let { responseMessage ->
          _chatMessages.value = _chatMessages.value + responseMessage
        }
      } catch (e: Exception) {
        handleError(e)
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun requestFeedback() {
    val analysisSummary = generateAnalysisSummary(collectedAnalysisData)

    val feedbackRequestMessage =
        Message(
            role = "user",
            content =
                """
                The interview is now over. Please provide feedback on my performance, considering the following analysis of my responses:

                $analysisSummary
            """
                    .trimIndent())

    _chatMessages.value = _chatMessages.value + feedbackRequestMessage

    getNextGPTResponse()
  }

  //    fun sendUserResponse(transcript: String, analysisData: AnalysisData) {
  //        val userMessage = Message(
  //            role = "user",
  //            content = transcript
  //        )
  //        _chatMessages.value = _chatMessages.value + userMessage
  //
  //        collectedAnalysisData.add(analysisData)
  //
  //        getNextGPTResponse()
  //    }

  private fun getAnalysisSummary(): String {
    return generateAnalysisSummary(collectedAnalysisData)
  }

  private fun generateAnalysisSummary(analysisDataList: List<AnalysisData>): String {
    // Implement logic to summarize analysis data
    return analysisDataList.joinToString("\n") { it.toString() }
  }

  // Add a method to generate feedback
  suspend fun generateFeedback(): String? {
    val analysisSummary = getAnalysisSummary()
    val feedbackRequestMessage =
        Message(
            role = "user",
            content =
                """
                The interview is now over. Please provide feedback on my performance, considering the following analysis of my responses:

                $analysisSummary
            """
                    .trimIndent())

    val messages = _chatMessages.value + feedbackRequestMessage

    val request = ChatRequest(model = "gpt-3.5-turbo", messages = messages)

    val response = chatGPTService.getChatCompletion(request)
    return response.choices.firstOrNull()?.message?.content
  }

  private fun handleError(e: Exception) {
    // Handle exceptions and update _errorMessage
    _errorMessage.value = e.localizedMessage
  }

  //    fun sendMessage(userMessage: String) {
  //        val newMessage = Message(role = "user", content = userMessage)
  //        _chatMessages.value = _chatMessages.value + newMessage
  //
  //        viewModelScope.launch {
  //            try {
  //                _isLoading.value = true
  //
  //                // Create a ChatRequest with the chat history
  //                val request = ChatRequest(
  //                    messages = _chatMessages.value
  //                )
  //
  //                // Make the API call
  //                val response = chatGPTService.getChatCompletion(request)
  //
  //                // Add the assistant's response to the chat history
  //                response.choices.firstOrNull()?.message?.let { responseMessage ->
  //                    _chatMessages.value = _chatMessages.value + responseMessage
  //                }
  //            } catch (e: HttpException) {
  //                val errorBody = e.response()?.errorBody()?.string()
  //                Log.e("ChatViewModel", "HTTP error: ${e.code()} ${e.message()}, Body:
  // $errorBody", e)
  //                _errorMessage.value = "Failed to send message: ${e.message()}"
  //            } catch (e: Exception) {
  //                Log.e("ChatViewModel", "Error sending message: ${e.message}", e)
  //                _errorMessage.value = "Failed to send message: ${e.message}"
  //            } finally {
  //                _isLoading.value = false
  //            }
  //        }
  //    }
}
