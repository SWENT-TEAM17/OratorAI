package com.github.se.orator

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.se.orator.ui.authentification.LoadingScreen
import com.github.se.orator.ui.network.ChatGPTService
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class MainActivityTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun scaffoldIsDisplayed() {
    val mockChatGPTService = mock(ChatGPTService::class.java)

    composeTestRule.setContent { OratorApp(mockChatGPTService) }

    // Check that the sign-in screen is displayed
    composeTestRule.onNodeWithTag("mainActivityScaffold").assertExists()
  }
}
