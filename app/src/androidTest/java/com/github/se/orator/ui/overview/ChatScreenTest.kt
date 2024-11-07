/*
package com.github.se.orator.ui.overview


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.network.Message
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class ChatScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testSendButtonEnabledWhenUserInputIsNotBlank() {
    val viewModel = mock(ChatViewModel::class.java)

    composeTestRule.setContent {
      ChatScreen(viewModel = viewModel)
    }

    // Input some text
    composeTestRule.onNodeWithTag("UserInputField").performTextInput("Hello, Orator!")

    // Verify Send button is enabled
    composeTestRule.onNodeWithTag("SendButton").assertIsDisplayed()
  }

  @Test
  fun testSendMessageFunctionality() {
    val viewModel = mock(ChatViewModel::class.java)

    composeTestRule.setContent {
      ChatScreen(viewModel = viewModel)
    }

    // Input some text and click the Send button
    composeTestRule.onNodeWithTag("UserInputField").performTextInput("Hello, Orator!")
    composeTestRule.onNodeWithTag("SendButton").performClick()

    // Verify that the sendUserResponse function is called in the ViewModel
    verify(viewModel).sendUserResponse(eq("Hello, Orator!"), any())
  }

  @Test
  fun testFeedbackButtonFunctionality() {
    val viewModel = mock(ChatViewModel::class.java)

    composeTestRule.setContent {
      ChatScreen(viewModel = viewModel)
    }

    // Click the feedback button
    composeTestRule.onNodeWithTag("FeedbackButton").performClick()

    // Verify that the requestFeedback function is called in the ViewModel
    verify(viewModel).requestFeedback()
  }

  @Composable
  @Test
  fun testLoadingIndicatorDisplayedWhileLoading() {
    val viewModel = mock(ChatViewModel::class.java)
    `when`(viewModel.isLoading.collectAsState()).thenReturn(true)

    composeTestRule.setContent {
      ChatScreen(viewModel = viewModel)
    }

    // Verify that the loading indicator is displayed
    composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
  }

  @Composable
  @Test
  fun testMessageDisplay() {
    val viewModel = mock(ChatViewModel::class.java)
    val messages = listOf(Message("user", "Hi there!"))
    `when`(viewModel.chatMessages.collectAsState()).thenReturn(messages)

    composeTestRule.setContent {
      ChatScreen(viewModel = viewModel)
    }

    // Verify that the message is displayed
    composeTestRule.onNodeWithTag("ChatMessageItem_user").assertIsDisplayed()
  }
}

*/
