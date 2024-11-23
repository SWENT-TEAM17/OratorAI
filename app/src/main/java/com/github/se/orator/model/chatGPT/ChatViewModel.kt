package com.github.se.orator.model.chatGPT

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatRequest
import com.github.se.orator.ui.network.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatGPTService: ChatGPTService,
    private val apiLinkViewModel: ApiLinkViewModel
) : ViewModel() {

  private var isConversationInitialized = false

  private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
  val chatMessages = _chatMessages.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage = _errorMessage.asStateFlow()

  // Change to List<AnalysisData> and update properly
  private val _collectedAnalysisData = MutableStateFlow<List<AnalysisData>>(emptyList())
  val collectedAnalysisData = _collectedAnalysisData.asStateFlow()

  private val practiceContext = apiLinkViewModel.practiceContext

  data class DecisionResult(val message: String, val isSuccess: Boolean)

  init {
    observeAnalysisData()
  }

  fun initializeConversation() {
    if (isConversationInitialized) return
    isConversationInitialized = true

    _collectedAnalysisData.value = emptyList() // Reset the analysis data history
    val practiceContextAsValue = practiceContext.value ?: return
    val systemMessageContent =
        when (practiceContextAsValue) {
          is InterviewContext ->
              """
                    You are simulating a ${practiceContextAsValue.interviewType} for the position of ${practiceContextAsValue.role} at ${practiceContextAsValue.company}. 
                    Focus on the following areas: ${practiceContextAsValue.focusAreas.joinToString(", ")}. 
                    Ask questions one at a time and wait for the user's response before proceeding. 
                    Do not provide feedback until the end.
                """
                  .trimIndent()
          is PublicSpeakingContext ->
              """
                    You are helping the user prepare a speech for a ${practiceContextAsValue.occasion}. 
                    The audience is ${practiceContextAsValue.audienceDemographic}. 
                    The main points of the speech are: ${
                        practiceContextAsValue.mainPoints.joinToString(", ")
                    }.
                    Please guide the user through practicing their speech, asking for their input on each point.
                """
                  .trimIndent()
          is SalesPitchContext ->
              """
                    You are helping the user prepare a sales pitch for the product ${practiceContextAsValue.product}. 
                    The target audience is ${practiceContextAsValue.targetAudience}. 
                    The key features of the product are: ${
                        practiceContextAsValue.keyFeatures.joinToString(", ")
                    }.
                    Please guide the user through practicing their sales pitch, asking for their input on each feature.
                """
                  .trimIndent()
          else -> "You are assisting the user with their speaking practice."
        }

    val systemMessage = Message(role = "system", content = systemMessageContent)

    val userStartMessage = Message(role = "user", content = "I'm ready to begin the session.")

    _chatMessages.value = listOf(systemMessage, userStartMessage)

    getNextGPTResponse()
  }

  fun sendUserResponse(transcript: String, analysisData: AnalysisData) {
    val userMessage = Message(role = "user", content = transcript)
    _chatMessages.value = _chatMessages.value + userMessage

    // Update the collected analysis data list properly
    _collectedAnalysisData.value = _collectedAnalysisData.value + analysisData

    getNextGPTResponse()
  }

  private fun getNextGPTResponse() {
    Log.d("ChatViewModel", "Getting next GPT response")
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

  fun resetPracticeContext() {
    apiLinkViewModel.clearPracticeContext()
  }

  fun endConversation() {
    isConversationInitialized = false
    apiLinkViewModel.clearAnalysisData() // because I don t want to reset before generating Feedback
  }

  private fun getAnalysisSummary(): String {
    return generateAnalysisSummary(_collectedAnalysisData.value)
  }

  private fun generateAnalysisSummary(analysisDataList: List<AnalysisData>): String {
    // Implement logic to summarize analysis data
    return analysisDataList.joinToString("\n") { analysisData ->
      """
            Transcription: ${analysisData.transcription}
            Sentiment Score: ${"%.2f".format(analysisData.sentimentScore)}
            Filler Words Count: ${analysisData.fillerWordsCount}
            Average Pause Duration: ${"%.2f".format(analysisData.averagePauseDuration)}
            """
          .trimIndent()
    }
  }

  // Keep generateFeedback() returning String?
  suspend fun generateFeedback(): String? {
    try {
      Log.d("ChatViewModel", "Starting generateFeedback()")

      val analysisSummary = getAnalysisSummary()
      Log.d("ChatViewModel", "Analysis Summary: $analysisSummary")

      val practiceContextAsValue = practiceContext.value
      if (practiceContextAsValue == null) {
        Log.e("ChatViewModel", "Practice context is null")
        return null
      } else {
        Log.d("ChatViewModel", "Practice context: $practiceContextAsValue")
      }

      // Determine the context-specific request
      val outcomeRequest =
          when (practiceContextAsValue) {
            is InterviewContext ->
                "Based on my performance, would you recommend hiring me for the position? And say specifically Yes I would hire you or No I would not hire you. Please provide reasons for your decision."
            is PublicSpeakingContext ->
                "Based on my performance, would I win the competition? And say specifically 'Yes I would win the competition' or 'No I would not win the competition'. Please provide reasons for your decision."
            is SalesPitchContext ->
                "Based on my performance, did I successfully convince you in the negotiation? And say specifically 'Yes you successfully convinced me' or 'No you did not convince me'. Please provide reasons for your decision."
            else -> "Please evaluate my performance and provide feedback."
          }
      Log.d("ChatViewModel", "Outcome Request: $outcomeRequest")

      val feedbackRequestMessage =
          Message(
              role = "user",
              content =
                  """
                The session is now over. Please provide feedback on my performance, considering the following analysis of my responses:

                $analysisSummary

                $outcomeRequest
            """
                      .trimIndent())

      val messages = _chatMessages.value + feedbackRequestMessage
      Log.d("ChatViewModel", "Total messages: ${messages.size}")

      val request = ChatRequest(model = "gpt-3.5-turbo", messages = messages)
      Log.d("ChatViewModel", "ChatRequest prepared")

      val response = chatGPTService.getChatCompletion(request)
      Log.d("ChatViewModel", "Received response from ChatGPT")

      val content = response.choices.firstOrNull()?.message?.content
      if (content == null) {
        Log.e("ChatViewModel", "Content from ChatGPT is null")
        return null
      } else {
        Log.d("ChatViewModel", "Feedback content received")
        return content
      }
    } catch (e: Exception) {
      Log.e("ChatViewModel", "Exception in generateFeedback(): ${e.localizedMessage}", e)
      return null
    }
  }

  private fun handleError(e: Exception) {
    // Handle exceptions and update _errorMessage
    _errorMessage.value = e.localizedMessage
    Log.e("ChatViewModel", "Error: ${e.localizedMessage}", e)
  }

  /**
   * Observe the analysis data from the ApiLinkViewModel and send the user response to the chat when
   * a new one is received.
   */
  private fun observeAnalysisData() {
    viewModelScope.launch {
      apiLinkViewModel.analysisData.collectLatest { data ->
        data?.let {
          Log.d("ChatViewModel", "Analysis data received: $it")
          sendUserResponse(it.transcription, it)
        }
      }
    }
  }
}
