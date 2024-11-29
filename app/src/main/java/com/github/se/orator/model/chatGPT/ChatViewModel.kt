package com.github.se.orator.model.chatGPT

import android.util.Log
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatGPTService: ChatGPTService,
    private val apiLinkViewModel: ApiLinkViewModel
) : ViewModel() {

  private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
  val chatMessages = _chatMessages.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage = _errorMessage.asStateFlow()

  private val _collectedAnalysisData = MutableStateFlow<MutableList<AnalysisData>>(mutableListOf())
  val collectedAnalysisData = _collectedAnalysisData.asStateFlow()

  private val practiceContext = apiLinkViewModel.practiceContext

    private val has_responded = MutableStateFlow(false)
    private val _response = MutableStateFlow("")
    val response: StateFlow<String> get() = _response
  init {
    observeAnalysisData()
  }

  fun initializeConversation() {

    _collectedAnalysisData.value.clear() // Resets the analysis data history
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
                        practiceContextAsValue.mainPoints.joinToString(
                            ", "
                        )
                    }.
                Please guide the user through practicing their speech, asking for their input on each point.
            """
                  .trimIndent()
          is SalesPitchContext ->
              """
                You are helping the user prepare a sales pitch for the product ${practiceContextAsValue.product}. 
                The target audience is ${practiceContextAsValue.targetAudience}. 
                The key features of the product are: ${
                        practiceContextAsValue.keyFeatures.joinToString(
                            ", "
                        )
                    }.
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
    _chatMessages.value += userMessage

    _collectedAnalysisData.value.add(analysisData)

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
          _chatMessages.value += responseMessage
        }
      } catch (e: Exception) {
        handleError(e)
      } finally {
        _isLoading.value = false
      }
    }
  }
    /**
     * Function to send individual requests to GPT to get feedback for the offline queries
     * @param msg: What the user said and wishes to get feedback on
     */

    fun offlineRequest(
        msg: String,
        company: String,
        position: String
    ) {
        Log.d("ChatViewModel", "Getting next GPT response")
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val gptQuery = "For the upcoming query you will respond as if you are a very strict interviewer who is interviewing someone applying for a $position position at $company that is very competitive. " +
                        "For example if the user only gives a few strengths you will tell him to cite more. If his strengths don't aline with a job as a $position or are off topic you will mention that." +
                        "Do not ask questions for the user to provide more skills or strengths. Assume this is a one time exchange where you can only give remarks without expecting any more messages." +
                        "The query the interviewee has said that you will critique is: $msg"
                Log.d("mr smith", gptQuery)

                val request = ChatRequest(model = "gpt-3.5-turbo", messages = listOf(Message(role = "system", content = gptQuery)))

                val response = chatGPTService.getChatCompletion(request)

                if (!has_responded.value) {
                    response.choices.firstOrNull()?.message?.let { responseMessage ->
                        _response.value = responseMessage.content
                        has_responded.value = true
                        Log.d("aa", "$responseMessage.content")
                    }
                }

            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

  fun endConversation() {
    apiLinkViewModel.resetAllPracticeData()
  }

  /*fun requestFeedback() {
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

    _chatMessages.value += feedbackRequestMessage

    getNextGPTResponse()
  }*/

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
    return generateAnalysisSummary(_collectedAnalysisData.value)
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
