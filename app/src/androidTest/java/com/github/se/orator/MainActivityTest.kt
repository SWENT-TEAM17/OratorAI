package com.github.se.orator

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.times

class MainActivityTest {

  //  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

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

    // Launch MainActivity
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    // Move the activity to the RESUMED state explicitly
    scenario.moveToState(Lifecycle.State.RESUMED)

    scenario.onActivity { activity ->

    }
    // Check that the main activity Scaffold exists
    composeTestRule.onNodeWithTag("mainActivityScaffold").assertExists()

    // Check that the orator scaffold exists
    composeTestRule.onNodeWithTag("oratorScaffold").assertExists()
  }
}
