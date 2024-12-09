package com.github.se.orator.model.chatGPT

import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatResponse
import com.github.se.orator.ui.network.Choice
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

  // Updated InterviewContext with new fields
  private val interviewContext =
      InterviewContext(
          targetPosition = "Software Engineer",
          companyName = "Tech Corp",
          interviewType = "Technical Interview",
          experienceLevel = "Intermediate",
          jobDescription = "Develop and maintain software applications.",
          focusArea = "Problem-Solving Skills")

  // Updated expected system message for InterviewContext
  private val interviewExpected =
      """
        You are simulating a realistic Technical Interview for the position of Software Engineer at Tech Corp. The candidate has Intermediate experience in this field. Your goal is to create an authentic and challenging interview simulation. Follow these detailed guidelines:

        1. **Research-Driven Context**:
           - Tailor your questions to align with Tech Corp's industry, values, and common interview practices.
           - Incorporate questions that focus on Problem-Solving Skills, as well as key skills required for the position.

        2. **Question Structure**:
           - Begin with an icebreaker or introductory question to set the tone.
           - Only ask **one question at a time** and wait for the user's response before proceeding to the next.
           - Ensure questions increase in complexity and cover both technical and behavioral aspects relevant to the Software Engineer role.

        3. **Professional Tone**:
           - Maintain a neutral, professional demeanor throughout.
           - Be courteous but do not show bias or leniency.

        4. **No Feedback During the Session**:
           - Do not provide feedback, hints, or reactions during the session.
           - Focus on conducting the interview as realistically as possible.

        Start the session by introducing yourself, the position, and setting expectations for the user.
    """
          .trimIndent()

  // Updated PublicSpeakingContext with new fields
  private val publicContext =
      PublicSpeakingContext(
          occasion = "Conference Keynote",
          purpose = "Inspire and educate",
          audienceSize = "Large",
          audienceDemographic = "Industry Professionals",
          presentationStyle = "Formal",
          mainPoints = listOf("Innovation", "Leadership"),
          experienceLevel = "Experienced",
          anticipatedChallenges = listOf("Technical Issues", "Tough Questions"),
          focusArea = "Engagement Techniques",
          feedbackType = "Body Language")

  // Updated expected system message for PublicSpeakingContext
  private val publicExpected =
      """
        You are a professional public speaking coach assisting the user in preparing a speech for a Conference Keynote with the purpose to Inspire and educate. The audience is a Large group of Industry Professionals. The user has a Experienced level of public speaking experience. Your objective is to guide the user in structuring and delivering a compelling speech. Follow these detailed guidelines:

        1. **Speech Structure and Content**:
           - Help the user structure their speech according to a Formal style.
           - Focus on the main points: Innovation, Leadership.
           - Address any anticipated challenges: Technical Issues, Tough Questions.

        2. **Delivery Improvement**:
           - Provide guidance on improving Engagement Techniques.
           - Offer tips on Body Language.

        3. **Interactive Coaching**:
           - Encourage the user to rehearse parts of their speech.
           - Provide constructive suggestions without overwhelming them.

        4. **Session Flow**:
           - Work through the speech step-by-step.
           - Summarize progress after each section.

        Start by introducing yourself and discussing the goals for the session.
    """
          .trimIndent()

  // Updated SalesPitchContext with new fields
  private val salesContext =
      SalesPitchContext(
          product = "Marketing Services",
          targetAudience = "Potential Clients",
          salesGoal = "Close the deal",
          keyFeatures = listOf("Customized Strategies", "ROI Focused"),
          anticipatedChallenges = listOf("Budget Constraints", "Competition"),
          negotiationFocus = "Handling Objections",
          feedbackType = "Persuasive Language")

  // Updated expected system message for SalesPitchContext
  private val salesExpected =
      """
        You are simulating a sales negotiation practice session for the user, who is preparing to pitch the product/service "Marketing Services" to Potential Clients. The primary goal is to Close the deal. Your objective is to provide a realistic and challenging sales scenario. Follow these detailed guidelines:

        1. **Scenario Setup**:
           - Role-play as a potential client from the target audience (Potential Clients).
           - Incorporate anticipated challenges: Budget Constraints, Competition.
           - Focus on the negotiation aspect: Handling Objections.

        2. **Session Structure**:
           - Allow the user to deliver their pitch, emphasizing key features: Customized Strategies, ROI Focused.
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

  @Test
  fun `offlineRequest sends query and sets response`() = runTest {
    val choice: Choice = Choice(0, Message("assistant", "Response content"), "done")
    // Arrange
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
    val message = "Test message"
    val company = "Test Company"
    val position = "Test Position"
    val mockResponse =
        ChatResponse(
            id = "1",
            `object` = "chat.completion",
            created = 0,
            model = "gpt-3.5-turbo",
            choices = listOf(choice),
            usage = Usage(1, 1, 2))

    `when`(chatGPTService.getChatCompletion(any())).thenReturn(mockResponse)

    // Act
    chatViewModel.offlineRequest(message, company, position)
    advanceUntilIdle()

    // Assert
    assert(chatViewModel.response.value == "Response content")
  }

  @Test
  fun `offlineRequest handles exception`() = runTest {
    // Arrange
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
    val message = "Test message"
    val company = "Test Company"
    val position = "Test Position"

    `when`(chatGPTService.getChatCompletion(any())).thenThrow(RuntimeException("Error"))

    // Act
    chatViewModel.offlineRequest(message, company, position)
    advanceUntilIdle()

    // Assert
    assert(chatViewModel.errorMessage.value == "Error")
  }
}
