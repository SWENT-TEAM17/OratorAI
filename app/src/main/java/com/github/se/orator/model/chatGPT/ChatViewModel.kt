package com.github.se.orator.model.chatGPT

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions
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
    You are simulating a realistic ${practiceContextAsValue.interviewType} for the position of ${practiceContextAsValue.targetPosition} at ${practiceContextAsValue.companyName}. The candidate has ${practiceContextAsValue.experienceLevel} experience in this field. Your goal is to create an authentic and challenging interview simulation. Follow these detailed guidelines:

            1. **Research-Driven Context**:
               - Tailor your questions to align with ${practiceContextAsValue.companyName}'s industry, values, and common interview practices.
               - Incorporate questions that focus on ${practiceContextAsValue.focusArea}, as well as key skills required for the position.

            2. **Question Structure**:
               - Begin with an icebreaker or introductory question to set the tone.
               - Only ask **one question at a time** and wait for the user's response before proceeding to the next.
               - Ensure questions increase in complexity and cover both technical and behavioral aspects relevant to the ${practiceContextAsValue.targetPosition} role.

            3. **Professional Tone**:
               - Maintain a neutral, professional demeanor throughout.
               - Be courteous but do not show bias or leniency.

            4. **No Feedback During the Session**:
               - Do not provide feedback, hints, or reactions during the session.
               - Focus on conducting the interview as realistically as possible.

            Start the session by introducing yourself, the position, and setting expectations for the user.
        """
                  .trimIndent()
          is PublicSpeakingContext ->
              """
                You are a professional public speaking coach assisting the user in preparing a speech for a ${practiceContextAsValue.occasion} with the purpose to ${practiceContextAsValue.purpose}. The audience is a ${practiceContextAsValue.audienceSize} group of ${practiceContextAsValue.audienceDemographic}. The user has a ${practiceContextAsValue.experienceLevel} level of public speaking experience. Your objective is to guide the user in structuring and delivering a compelling speech. Follow these detailed guidelines:

            1. **Speech Structure and Content**:
               - Help the user structure their speech according to a ${practiceContextAsValue.presentationStyle} style.
               - Focus on the main points: ${practiceContextAsValue.mainPoints.joinToString(", ")}.
               - Address any anticipated challenges: ${practiceContextAsValue.anticipatedChallenges.joinToString(", ")}.

            2. **Delivery Improvement**:
               - Provide guidance on improving ${practiceContextAsValue.focusArea}.
               - Offer tips on ${practiceContextAsValue.feedbackType}.

            3. **Interactive Coaching**:
               - Encourage the user to rehearse parts of their speech.
               - Provide constructive suggestions without overwhelming them.

            4. **Session Flow**:
               - Work through the speech step-by-step.
               - Summarize progress after each section.

            Start by introducing yourself and discussing the goals for the session.
        """
                  .trimIndent()
          is SalesPitchContext ->
              """
                You are simulating a sales negotiation practice session for the user, who is preparing to pitch the product/service "${practiceContextAsValue.product}" to ${practiceContextAsValue.targetAudience}. The primary goal is to ${practiceContextAsValue.salesGoal}. Your objective is to provide a realistic and challenging sales scenario. Follow these detailed guidelines:

            1. **Scenario Setup**:
               - Role-play as a potential client from the target audience (${practiceContextAsValue.targetAudience}).
               - Incorporate anticipated challenges: ${practiceContextAsValue.anticipatedChallenges.joinToString(", ")}.
               - Focus on the negotiation aspect: ${practiceContextAsValue.negotiationFocus}.

            2. **Session Structure**:
               - Allow the user to deliver their pitch, emphasizing key features: ${practiceContextAsValue.keyFeatures.joinToString(", ")}.
               - Introduce objections or negotiation hurdles relevant to the scenario.

            3. **Focus Areas**:
               - Evaluate the user's ability to handle objections and close the deal.
               - Observe their use of persuasive language and negotiation techniques.

            4. **Professional Role**:
               - Maintain the persona of a realistic client.
               - Do not provide feedback or hints during the session.

            Start the session by setting the scene and initiating the conversation.
        """
                  .trimIndent()
          // Add cases for other context types like SalesPitchContext
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
                """
                Based on my performance in the ${practiceContextAsValue.interviewType} for the ${practiceContextAsValue.targetPosition} role at ${practiceContextAsValue.companyName}, please provide detailed feedback including:

                - **Overall Assessment**: Would you recommend hiring me for the position? Please state explicitly 'Yes, I would recommend hiring you' or 'No, I would not recommend hiring you', and explain your reasoning.
                - **Strengths**: Highlight the areas where I performed well.
                - **Weaknesses**: Point out the areas where I need improvement.
                - **Suggestions for Improvement**: Provide specific advice on how I can enhance my performance in future interviews, especially regarding ${practiceContextAsValue.focusArea}.
            """
                    .trimIndent()
            is PublicSpeakingContext ->
                """
                Considering my speech prepared for the ${practiceContextAsValue.occasion} with the purpose to ${practiceContextAsValue.purpose}, please provide detailed feedback including:

                - **Overall Assessment**: How effective was my delivery? Did I successfully achieve my speech's purpose? Please state explicitly 'Yes, you were effective' or 'No, you were not effective', and explain your reasoning.
                - **Strengths**: Highlight the aspects of my speech that were particularly strong.
                - **Weaknesses**: Identify areas where I could improve.
                - **Suggestions for Improvement**: Provide specific advice on how I can enhance my speech, focusing on ${practiceContextAsValue.focusArea} and ${practiceContextAsValue.feedbackType}.
            """
                    .trimIndent()
            is SalesPitchContext ->
                """
                Based on my sales pitch for "${practiceContextAsValue.product}" aimed at ${practiceContextAsValue.targetAudience}, please provide detailed feedback including:

                - **Overall Assessment**: Did I achieve my sales goal to ${practiceContextAsValue.salesGoal}? Please state explicitly 'Yes, you achieved your sales goal' or 'No, you did not achieve your sales goal', and explain your reasoning.
                - **Strengths**: Highlight the areas where my pitch was effective.
                - **Weaknesses**: Point out aspects where I could improve.
                - **Suggestions for Improvement**: Provide specific advice on how I can enhance my sales pitch, especially regarding ${practiceContextAsValue.negotiationFocus} and ${practiceContextAsValue.feedbackType}.
            """
                    .trimIndent()
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

  /**
   * Initializes the conversation for a battle session.
   *
   * @param battleId The unique ID of the battle.
   * @param friendName The UID of the friend participating in the battle.
   */
  fun initializeBattleConversation(battleId: String, friendName: String) {
    if (isConversationInitialized) return
    isConversationInitialized = true

    _collectedAnalysisData.value = emptyList() // Reset the analysis data history
    val practiceContextAsValue =
        (apiLinkViewModel.practiceContext.value ?: return) as InterviewContext

    val systemMessageContent =
        """
                    You are engaged in a battle against $friendName a ${practiceContextAsValue.interviewType} for the position of ${practiceContextAsValue.targetPosition} at ${practiceContextAsValue.companyName}. 
                    Focus on the following areas: ${practiceContextAsValue.focusArea}. 
                    Ask questions one at a time and wait for the user's response before proceeding. 
                    Do not provide feedback until the end.
                """
            .trimIndent()

    val systemMessage = Message(role = "system", content = systemMessageContent)

    val userStartMessage =
        Message(role = "user", content = "I'm ready to begin the battle session.")

    _chatMessages.value = listOf(systemMessage, userStartMessage)

    getNextGPTResponse()
  }

  fun performEvaluation(
      battleId: String,
      battle: SpeechBattle,
      challengerMessages: List<Message>,
      opponentMessages: List<Message>,
      battleViewModel: BattleViewModel,
      navigationActions: NavigationActions
  ) {
    val context = battle.context as? InterviewContext ?: return

    val prompt = generateEvaluationPrompt(challengerMessages, opponentMessages, context)

    viewModelScope.launch {
      try {
        val request =
            ChatRequest(
                model = "gpt-3.5-turbo",
                messages =
                    listOf(
                        Message(
                            role = "system", content = "You are an expert interview evaluator."),
                        Message(role = "user", content = prompt)))

        val response = chatGPTService.getChatCompletion(request)

        val content = response.choices.firstOrNull()?.message?.content ?: ""

        // Parse the winner from the response
        val winnerUid = parseWinnerFromResponse(content, battle)

        // Update battle result
        battleViewModel.updateBattleResult(battleId, winnerUid, content)

        // Notify users
        navigationActions.navigateToEvaluationScreen(battleId)
      } catch (e: Exception) {
        Log.e("BattleViewModel", "Error evaluating battle", e)
      }
    }
  }

  private fun generateEvaluationPrompt(
      challengerMessages: List<Message>,
      opponentMessages: List<Message>,
      context: InterviewContext
  ): String {
    val challengerTranscript = messagesToTranscript(challengerMessages)
    val opponentTranscript = messagesToTranscript(opponentMessages)

    return """
        You are to evaluate two interview performances for the position of ${context.targetPosition} at ${context.companyName}.

        Candidate 1's responses:
        $challengerTranscript

        Candidate 2's responses:
        $opponentTranscript

        Please analyze both candidates' performances based on clarity, relevance, and professionalism. Determine which candidate performed better and provide a brief explanation for your decision.

        At the end, state "Candidate 1" or "Candidate 2" to indicate who should be hired.
    """
        .trimIndent()
  }

  private fun messagesToTranscript(messages: List<Message>): String {
    return messages.filter { it.role == "user" }.joinToString("\n") { it.content }
  }

  private fun parseWinnerFromResponse(response: String, battle: SpeechBattle): String {
    return when {
      response.contains("Candidate 1", ignoreCase = true) -> battle.challenger
      response.contains("Candidate 2", ignoreCase = true) -> battle.opponent
      else -> ""
    }
  }
}
