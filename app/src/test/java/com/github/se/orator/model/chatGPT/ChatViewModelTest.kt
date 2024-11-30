package com.github.se.orator.model.chatGPT

import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatResponse
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.network.Usage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class ChatViewModelTest {

  @Mock private lateinit var chatGPTService: ChatGPTService
  @Mock private lateinit var apiLinkViewModel: ApiLinkViewModel

  private lateinit var chatViewModel: ChatViewModel

  private val testDispatcher = StandardTestDispatcher() // Using a test dispatcher

  private val chatResp = ChatResponse("id", "object", 0, "model", emptyList(), Usage(0, 0, 0))

  private val interviewContext =
      InterviewContext("interviewType", "role", "company", listOf("focusArea"))
  private val interviewExpected =
      """
        |You are simulating a realistic professional interview experience for the position of ${interviewContext.role} at ${interviewContext.company}. Your goal is to create an authentic and challenging interview simulation. Follow these detailed guidelines:
        |
        |1. **Research-Driven Context**:
        |   - Tailor your questions to align with the company's industry, values, and common interview practices.
        |   - Incorporate behavioral, technical, and scenario-based questions that are relevant to the role (${interviewContext.role}).
        |
        |2. **Question Structure**:
        |   - Begin with an icebreaker or introductory question to set the tone.
        |   - Ask a mix of questions focusing on the following areas: ${interviewContext.focusAreas.joinToString(", ")}.
        |   - Only ask **one question at a time** and wait for the user's response before proceeding to the next.
        |   - Ensure questions increase in complexity as the interview progresses.
        |
        |3. **No Feedback During the Session**:
        |   - Act as a professional interviewer.
        |   - Do not provide feedback, hints, or reactions during the session.
        |   - Focus on gathering responses and assessing their quality silently.
        |
        |4. **Professional Tone**:
        |   - Maintain a neutral, professional demeanor throughout.
        |   - Be courteous but do not show bias or leniency.
        |
        |5. **Post-Session Feedback**:
        |   - At the end of the session, feedback will be requested separately. During the session, focus only on conducting the interview.
        |
        |Start the session by introducing yourself, the position, and setting expectations for the user.
    """
          .trimMargin()

  private val publicContext =
      PublicSpeakingContext("occasion", "audienceDemographic", listOf("mainPoint"))
  private val publicExpected =
      """
                You are a professional public speaking coach assisting the user in preparing a speech for the occasion of ${publicContext.occasion}. Your objective is to guide the user in structuring and delivering a compelling speech. Follow these detailed guidelines:
                
                1. **Speech Structure**:
                   - Help the user structure their speech into three sections:
                     a. **Introduction**: Start with an engaging hook and provide a clear overview of the topic.
                     b. **Body**: Develop the main points (${publicContext.mainPoints.joinToString(", ")}) with supporting evidence or examples.
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
          .trimMargin()

  private val salesContext = SalesPitchContext("product", "targetAudience", listOf("feature"))
  private val salesExpected =
      """
                You are simulating a sales negotiation practice session for the user, who is preparing to pitch the product ${salesContext.product} to ${salesContext.targetAudience}. Your goal is to provide a realistic and challenging sales scenario. Follow these detailed guidelines:
                
                1. **Scenario Setup**:
                   - Role-play as a skeptical but potential client or decision-maker from the target audience (${salesContext.targetAudience}).
                   - Incorporate realistic objections, questions, or concerns that align with the user's product (${salesContext.product}) and its key features (${salesContext.keyFeatures.joinToString(", ")}).
                
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
          .trimMargin()

  private val analysisData = AnalysisData("transcription", 0, 0.0, 0.0)

  private val analysisDataState = MutableStateFlow<AnalysisData?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher) // Set the test dispatcher

    chatGPTService = mock(ChatGPTService::class.java)
    apiLinkViewModel = mock(ApiLinkViewModel::class.java)

    // Mock the practice context value
    `when`(apiLinkViewModel.practiceContext).thenReturn(MutableStateFlow(interviewContext))
    // Mock the analysis data value
    `when`(apiLinkViewModel.analysisData).thenReturn(analysisDataState)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain() // Reset the main dispatcher
    testDispatcher.cancel()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun initialzeConversationWorksProperly() = runTest {
    var expected = interviewExpected

    for (i in 1..3) {
      // Create a ChatViewModel instance with the mocked ChatGPTService and ApiLinkViewModel
      chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

      // Call the initializeConversation method
      chatViewModel.initializeConversation()

      advanceUntilIdle()

      // Verify that the chat messages are as expected
      assert(chatViewModel.chatMessages.value.size == 2)
      assert(
          chatViewModel.chatMessages.value[1] ==
              Message(role = "user", content = "I'm ready to begin the session."))

      if (i == 1) {
        `when`(apiLinkViewModel.practiceContext).thenReturn(MutableStateFlow(publicContext))
        expected = publicExpected
      } else if (i == 2) {
        `when`(apiLinkViewModel.practiceContext).thenReturn(MutableStateFlow(salesContext))
        expected = salesExpected
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun analysisDataIsCorrectlyObserved() = runTest {
    // Create a ChatViewModel instance with the mocked ChatGPTService and ApiLinkViewModel
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    // Call the initializeConversation method
    chatViewModel.initializeConversation()

    // The list of collected analysis data should be empty
    assert(chatViewModel.collectedAnalysisData.value.isEmpty())

    // Set the analysis data state to the analysis data, this should trigger the sendUserResponse
    // method in ChatViewModel
    analysisDataState.value = analysisData

    // Advance the test dispatcher (wait for the coroutine to finish)
    advanceUntilIdle()

    // Verify that the analysis data was collected
    verify(apiLinkViewModel).analysisData
    assert(chatViewModel.collectedAnalysisData.value.isNotEmpty())
    assert(chatViewModel.collectedAnalysisData.value[0] == analysisData)
  }

  @Test
  fun generateFeedbackCallsAPI() = runTest {
    // Create a ChatViewModel instance with the mocked ChatGPTService and ApiLinkViewModel
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
    // Call the initializeConversation method
    chatViewModel.initializeConversation()

    // Verify that chatGPTService.getChatCompletion was called and retrieve the argument
    `when`(chatGPTService.getChatCompletion(any())).thenReturn(chatResp)

    // Call the generateFeedback method
    assert(chatViewModel.generateFeedback() == chatResp.choices.firstOrNull()?.message?.content)
    verify(chatGPTService).getChatCompletion(any())
  }
}
