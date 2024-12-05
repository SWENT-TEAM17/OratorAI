package com.github.se.orator

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ActivityScenario
import com.github.se.orator.ui.network.ChatGPTService
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times

class MainActivityTest {

  //  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val composeTestRule = createComposeRule()

  //  @Test
  //  fun oratorScaffoldExists() {
  //    val mockChatGPTService = mock(ChatGPTService::class.java)
  //
  //    composeTestRule2.setContent { OratorApp(mockChatGPTService) }
  //    // Check that the orator scaffold exists
  //    composeTestRule2.onNodeWithTag("oratorScaffold").assertExists()
  //  }

  @Test
  fun testMainActivityOnCreate() {

    composeTestRule.setContent { MainActivity() }

    // Launch MainActivity
    val scenario = ActivityScenario.launch(MainActivity::class.java)

    scenario.onActivity { activity -> }

    // Check that the main activity Scaffold exists
    composeTestRule.onNodeWithTag("mainActivityScaffold").assertExists()

    // Check that the orator scaffold exists
    composeTestRule.onNodeWithTag("oratorScaffold").assertExists()
  }

  @Test
  fun noAppThemeViewModelDoesNotCrash() {
    composeTestRule.setContent { OratorApp(mock(ChatGPTService::class.java), false, null) }
  }
}
