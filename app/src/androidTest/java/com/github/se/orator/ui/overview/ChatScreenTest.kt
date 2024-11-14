package com.github.se.orator.ui.overview

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatResponse
import com.github.se.orator.ui.network.Choice
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.network.Usage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class ChatScreenTest {

  @get:Rule
  val composeTestRule =
      createComposeRule().apply {
        // Disable animations in tests
        mainClock.autoAdvance = false
        mainClock.advanceTimeBy(0) // Set animations to effectively zero time
      }

  private val testDispatcher = StandardTestDispatcher() // Using a test dispatcher

  @Mock lateinit var navigationActions: NavigationActions
  @Mock lateinit var chatGPTService: ChatGPTService

  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  private val testChatResponse =
      ChatResponse(
          "response",
          "obj",
          0L,
          "model",
          listOf(
              Choice(0, Message("role", "content"), null),
          ),
          Usage(0, 0, 0))

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockitoAnnotations.openMocks(this)

    apiLinkViewModel = ApiLinkViewModel()
    apiLinkViewModel.updatePracticeContext(
        InterviewContext("type", "role", "company", listOf("focus")))
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain() // Reset the main dispatcher
    testDispatcher.cancel()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun elementsAreDisplayed(): Unit = runTest {
    `when`(chatGPTService.getChatCompletion(any())).thenReturn(testChatResponse)

    composeTestRule.setContent {
      ChatScreen(chatViewModel = chatViewModel, navigationActions = navigationActions)
    }
    advanceUntilIdle()

    composeTestRule.onNodeWithTag("ChatScreen").assertExists().assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ChatScreenTitle")
        .assertExists()
        .assertIsDisplayed()
        .assertTextEquals("Chat Screen")
    composeTestRule.onNodeWithTag("ChatScreenBackButton").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("ChatScreenMessagesColumn").assertExists().assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ChatScreenRecordResponseButton")
        .assertExists()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ChatScreenRequestFeedbackButton")
        .assertExists()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ChatScreenRequestFeedbackButton")
        .assertExists()
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("ChatScreenMessage").assertCountEquals(3)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun backButtonNavigatesBack() = runTest {
    composeTestRule.setContent {
      ChatScreen(chatViewModel = chatViewModel, navigationActions = navigationActions)
    }

    advanceUntilIdle()

    composeTestRule.onNodeWithTag("ChatScreenBackButton").performClick()

    verify(navigationActions).goBack()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun recordResponseButtonNavigatesToSpeakingScreen() = runTest {
    composeTestRule.setContent {
      ChatScreen(chatViewModel = chatViewModel, navigationActions = navigationActions)
    }

    advanceUntilIdle()

    composeTestRule.onNodeWithTag("ChatScreenRecordResponseButton").performClick()

    verify(navigationActions).navigateTo(Screen.SPEAKING)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun requestFeedbackButtonNavigatesToFeedbackScreen() = runTest {
    composeTestRule.setContent {
      ChatScreen(chatViewModel = chatViewModel, navigationActions = navigationActions)
    }

    advanceUntilIdle()

    composeTestRule.onNodeWithTag("ChatScreenRequestFeedbackButton").performClick()

    verify(navigationActions).navigateTo(Screen.FEEDBACK)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun receivingNewDataUpdatesScreen() = runTest {
    `when`(chatGPTService.getChatCompletion(any())).thenReturn(testChatResponse)

    composeTestRule.setContent {
      ChatScreen(chatViewModel = chatViewModel, navigationActions = navigationActions)
    }

    advanceUntilIdle()

    composeTestRule.onAllNodesWithTag("ChatScreenMessage").assertCountEquals(3)

    runBlocking { apiLinkViewModel.updateAnalysisData(AnalysisData("transcript", 0, 0.0, 0.0)) }
    advanceUntilIdle()
    composeTestRule
        .onAllNodesWithTag("ChatScreenMessage", useUnmergedTree = true)
        .assertCountEquals(5)
  }
}
