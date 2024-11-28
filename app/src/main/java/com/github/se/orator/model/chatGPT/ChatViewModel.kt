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
                You are simulating a realistic professional interview experience for the position of ${practiceContextAsValue.role} at ${practiceContextAsValue.company}. Your goal is to create an authentic and challenging interview simulation. Follow these detailed guidelines:
                
                1. **Research-Driven Context**:
                   - Tailor your questions to align with the company's industry, values, and common interview practices.
                   - Incorporate behavioral, technical, and scenario-based questions that are relevant to the role (${practiceContextAsValue.role}).
                
                2. **Question Structure**:
                   - Begin with an icebreaker or introductory question to set the tone.
                   - Ask a mix of questions focusing on the following areas: ${practiceContextAsValue.focusAreas.joinToString(", ")}.
                   - Only ask **one question at a time** and wait for the user's response before proceeding to the next.
                   - Ensure questions increase in complexity as the interview progresses.
                
                3. **No Feedback During the Session**:
                   - Act as a professional interviewer.
                   - Do not provide feedback, hints, or reactions during the session.
                   - Focus on gathering responses and assessing their quality silently.
                
                4. **Professional Tone**:
                   - Maintain a neutral, professional demeanor throughout.
                   - Be courteous but do not show bias or leniency.
                
                5. **Post-Session Feedback**:
                   - At the end of the session, feedback will be requested separately. During the session, focus only on conducting the interview.
                
                Start the session by introducing yourself, the position, and setting expectations for the user.
                """
                        .trimIndent()
                is PublicSpeakingContext ->
                    """
                You are a professional public speaking coach assisting the user in preparing a speech for the occasion of ${practiceContextAsValue.occasion}. Your objective is to guide the user in structuring and delivering a compelling speech. Follow these detailed guidelines:
                
                1. **Speech Structure**:
                   - Help the user structure their speech into three sections:
                     a. **Introduction**: Start with an engaging hook and provide a clear overview of the topic.
                     b. **Body**: Develop the main points (${practiceContextAsValue.mainPoints.joinToString(", ")}) with supporting evidence or examples.
                     c. **Conclusion**: End with a memorable takeaway or call to action.
                
                2. **User Engagement**:
                   - Encourage the user to rehearse each section aloud.
                   - Ask questions to refine their ideas, transitions, and delivery for each part of the speech.
                
                3. **Delivery Focus**:
                   - Guide the user in improving their tone, pacing, volume, and engagement.
                   - Help them eliminate filler words and maintain a confident demeanor.
                
                4. **Session Flow**:
                   - Work through the speech step-by-step, focusing on one section at a time.
                   - Summarize progress after completing each section.
                
                5. **Professional Tone**:
                   - Maintain a constructive and motivational tone.
                   - Encourage iterative improvement through clear and actionable suggestions.
                
                Start by introducing your role as the coach, the goals for the session, and setting expectations for the user.
                """
                        .trimIndent()
                is SalesPitchContext ->
                    """
                You are simulating a sales negotiation practice session for the user, who is preparing to pitch the product ${practiceContextAsValue.product} to ${practiceContextAsValue.targetAudience}. Your goal is to provide a realistic and challenging sales scenario. Follow these detailed guidelines:
                
                1. **Scenario Setup**:
                   - Role-play as a skeptical but potential client or decision-maker from the target audience (${practiceContextAsValue.targetAudience}).
                   - Incorporate realistic objections, questions, or concerns that align with the user's product (${practiceContextAsValue.product}) and its key features (${practiceContextAsValue.keyFeatures.joinToString(", ")}).
                
                2. **Session Structure**:
                   - Start with an introduction, setting the stage for the negotiation (e.g., a corporate buyer looking to evaluate the product).
                   - Allow the user to deliver their pitch, covering the product's key features and value proposition.
                   - Gradually introduce objections or negotiation challenges to test the user's ability to adapt and persuade.
                
                3. **Focus Areas**:
                   - Evaluate the clarity of the pitch and how well it addresses the client's needs.
                   - Assess the user's handling of objections and ability to build rapport.
                   - Observe how effectively the user uses persuasive techniques to close the deal.
                
                4. **Professional Role**:
                   - Maintain a realistic persona throughout the session.
                   - Do not provide feedback or hints during the pitch. Focus on creating an immersive experience.
                
                5. **Post-Session Feedback**:
                   - Feedback will be provided separately at the end of the session.
                
                Start the session by introducing the negotiation scenario, your role, and setting expectations for the user.
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
                        """
                The session is now over. Please evaluate my performance during the simulated interview for the role of ${practiceContextAsValue.role} at ${practiceContextAsValue.company}. Your feedback should follow this structure:
                
                1. **Overall Performance**:
                   - Provide an overall evaluation of my responses, considering relevance, depth, and alignment with the position requirements (${practiceContextAsValue.role}) and the company (${practiceContextAsValue.company}).
                
                2. **Strengths**:
                   - Highlight specific aspects of my responses that were well-articulated or stood out.
                   - Mention areas where my skills, experience, or approach align with the role and company values.
                
                3. **Areas for Improvement**:
                   - Identify any gaps in my responses, such as technical knowledge, communication clarity, or strategic thinking.
                   - Provide actionable recommendations to address these gaps.
                
                4. **Example of Improvement**:
                  - Based on my previous answer give me an example of how I could improve my pitch.
                  
                5. **Recommendation**:
                   - Based on my performance, would you recommend hiring me for the position? Answer explicitly with "Yes, I would hire you" or "No, I would not hire you," followed by a clear justification for your decision.
                
                Be detailed, constructive, and specific to help me understand how I can improve for future interviews.
                """
                    is PublicSpeakingContext ->
                        """
                The session is now over. Please evaluate my performance during the public speaking practice for the occasion of ${practiceContextAsValue.occasion}, aimed at an audience of ${practiceContextAsValue.audienceDemographic}. Your feedback should follow this structure:
                
                1. **Speech Content**:
                   - Assess the structure of my speech: Was the introduction engaging, the body well-organized, and the conclusion impactful?
                   - Comment on the clarity and effectiveness of my arguments or points.
                
                2. **Delivery**:
                   - Evaluate my vocal delivery, including tone, pacing, intonation, and volume.
                   - Highlight how engaging or persuasive my delivery was for the target audience (${practiceContextAsValue.audienceDemographic}).
                
                3. **Strengths**:
                   - Identify specific areas where I performed well, such as storytelling, confidence, or audience connection.
                
                4. **Areas for Improvement**:
                   - Provide actionable suggestions for improving my speech structure, content, or delivery.
                
                5. **Example of Improvement**:
                   - Based on my previous answer give me an example of how I could improve my pitch.
                   
                6. **Outcome**:
                   - Would my speech likely achieve its intended purpose for the occasion and audience? Answer explicitly with "Yes, you would win the competition" or "No, you would not win the competition," followed by your reasoning.
                
                Provide detailed, constructive feedback that will help me refine my public speaking skills.
                """
                    is SalesPitchContext ->
                        """
                The session is now over. Please evaluate my performance during the sales pitch practice for the product ${practiceContextAsValue.product}, targeted at ${practiceContextAsValue.targetAudience}. Your feedback should follow this structure:
                
                1. **Pitch Content**:
                   - Assess the clarity and persuasiveness of my pitch.
                   - Evaluate how effectively I communicated the key features (${practiceContextAsValue.keyFeatures.joinToString(", ")}) and value proposition of the product.
                
                2. **Handling Objections**:
                   - Evaluate my ability to address objections or challenges raised during the negotiation.
                   - Comment on how well I adapted my responses to the target audience (${practiceContextAsValue.targetAudience}).
                
                3. **Strengths**:
                   - Highlight areas of the pitch or negotiation that were particularly effective or persuasive.
                
                4. **Areas for Improvement**:
                   - Identify specific aspects of my pitch or negotiation strategy that could be improved.
                   - Provide actionable suggestions for enhancing these areas.
                
                5. **Example of Improvement**:
                   - Based on my previous answer give me an example of how I could improve my pitch.
                   
                6. **Outcome**:
                   - Did I successfully convince you in the negotiation? Answer explicitly with "Yes, you successfully convinced me" or "No, you did not convince me," followed by a detailed explanation.
                
                Provide constructive, actionable feedback to help me refine my sales pitch skills.
                """
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