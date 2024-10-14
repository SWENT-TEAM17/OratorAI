package com.github.se.orator.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatRequest
import com.github.se.orator.ui.network.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext

class ChatViewModel(private val chatGPTService: ChatGPTService) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val collectedAnalysisData = mutableListOf<AnalysisData>()

    fun initializeConversation(interviewContext: InterviewContext) {
        val systemMessageContent = """
            You are simulating a ${interviewContext.interviewType} for the position of ${interviewContext.role} at ${interviewContext.company}. 
            Focus on the following areas: ${interviewContext.focusAreas.joinToString(", ")}. 
            Ask questions one at a time and wait for the user's response before proceeding. 
            Do not provide feedback until the end.
        """.trimIndent()

        val systemMessage = Message(
            role = "system",
            content = systemMessageContent
        )

        val userStartMessage = Message(
            role = "user",
            content = "I'm ready to begin the interview."
        )

        _chatMessages.value = listOf(systemMessage, userStartMessage)

        getNextGPTResponse()
    }

    fun sendUserResponse(transcript: String, analysisData: AnalysisData) {
        val userMessage = Message(
            role = "user",
            content = transcript
        )
        _chatMessages.value = _chatMessages.value + userMessage

        collectedAnalysisData.add(analysisData)

        getNextGPTResponse()
    }

    private fun getNextGPTResponse() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val request = ChatRequest(
                    model = "gpt-3.5-turbo",
                    messages = _chatMessages.value
                )

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

        val feedbackRequestMessage = Message(
            role = "user",
            content = """
                The interview is now over. Please provide feedback on my performance, considering the following analysis of my responses:

                $analysisSummary
            """.trimIndent()
        )

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

    private fun generateAnalysisSummary(analysisDataList: List<AnalysisData>): String {
        // Implement logic to summarize analysis data
        // For example, count total filler words, average pause duration, etc.
        // Return a formatted string
        return analysisDataList.joinToString("\n") { it.toString() }
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
//                Log.e("ChatViewModel", "HTTP error: ${e.code()} ${e.message()}, Body: $errorBody", e)
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