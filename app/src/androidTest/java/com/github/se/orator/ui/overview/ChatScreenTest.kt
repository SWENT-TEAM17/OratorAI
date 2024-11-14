package com.github.se.orator.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatResponse
import com.github.se.orator.ui.network.Usage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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

  @get:Rule val composeTestRule = createComposeRule()

  private val testDispatcher = StandardTestDispatcher()

  @Mock private lateinit var chatGPTService: ChatGPTService

  @Mock private lateinit var navigationActions: NavigationActions

  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockitoAnnotations.openMocks(this)

    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cancel()
  }

  @Test
  fun screenIsDisplayed() = runTest {
    `when`(chatGPTService.getChatCompletion(any()))
        .thenReturn(ChatResponse("id", "object", 0, "model", emptyList(), Usage(0, 0, 0)))

    chatViewModel.initializeConversation()
    advanceUntilIdle()

    composeTestRule.setContent {
      ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
    }

    composeTestRule.onNodeWithTag("top_app_bar").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_screen_column").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_messages_list").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").assertExists().assertIsDisplayed()
  }

  @Test
  fun clickingBackButtonNavigatesBack() = runTest {
    composeTestRule.setContent {
      ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
    }

    composeTestRule.onNodeWithTag("back_button").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun recordResponseButtonNavigatesToSpeakingScreen() = runTest {
    composeTestRule.setContent {
      ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
    }

    composeTestRule.onNodeWithTag("record_response_button").performClick()

    verify(navigationActions).navigateTo(Screen.SPEAKING)
  }

  @Test
  fun requestFeedbackButtonNavigatesToFeedbackScreen() = runTest {
    composeTestRule.setContent {
      ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
    }

    composeTestRule.onNodeWithTag("request_feedback_button").performClick()

    verify(navigationActions).navigateTo(Screen.FEEDBACK)
  }

  @Test
  fun loadingIndicatorIsNotDisplayedAndButtonsAreEnabledWhenNotLoading() = runTest {
    `when`(chatGPTService.getChatCompletion(any()))
        .thenReturn(ChatResponse("id", "object", 0, "model", emptyList(), Usage(0, 0, 0)))

    composeTestRule.setContent {
      ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
    }

    advanceUntilIdle()

    composeTestRule.onNodeWithTag("loading_indicator").assertDoesNotExist()
    composeTestRule.onNodeWithTag("record_response_button").assertIsEnabled()
    composeTestRule.onNodeWithTag("request_feedback_button").assertIsEnabled()
  }
}
