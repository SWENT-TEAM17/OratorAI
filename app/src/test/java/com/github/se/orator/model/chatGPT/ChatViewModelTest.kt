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
      """You are simulating a ${interviewContext.interviewType} for the position of ${interviewContext.role} at ${interviewContext.company}. 
        |Focus on the following areas: ${interviewContext.focusAreas.joinToString(", ")}. 
        |Ask questions one at a time and wait for the user's response before proceeding. 
        |Do not provide feedback until the end."""
          .trimMargin()

  private val publicContext =
      PublicSpeakingContext("occasion", "audienceDemographic", listOf("mainPoint"))
  private val publicExpected =
      """You are helping the user prepare a speech for a ${publicContext.occasion}. 
    |The audience is ${publicContext.audienceDemographic}. 
    |The main points of the speech are: ${publicContext.mainPoints.joinToString(", ")}.
    |Please guide the user through practicing their speech, asking for their input on each point.
    """
          .trimMargin()

  private val salesContext = SalesPitchContext("product", "targetAudience", listOf("feature"))
  private val salesExpected =
      """You are helping the user prepare a sales pitch for the product ${salesContext.product}. 
    |The target audience is ${salesContext.targetAudience}. 
    |The key features of the product are: ${salesContext.keyFeatures.joinToString(", ")}.
    |Please guide the user through practicing their sales pitch, asking for their input on each feature.
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
      assert(chatViewModel.chatMessages.value[0] == Message(role = "system", content = expected))
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
