package com.github.se.orator.ui.offline

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.offline.OfflineSpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class OfflineRecordingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var offlineSpeakingViewModel: OfflineSpeakingViewModel

  @Before
  fun setup() {
    navigationActions = mock()
    offlineSpeakingViewModel = mock() // Now possible with inline mocking
  }

  @Test
  fun testInitialUIElementsDisplayed() {
    val question = "What is your favorite color?"

    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = question,
          offlineSpeakingViewModel = offlineSpeakingViewModel)
    }

    composeTestRule.onNodeWithTag("QuestionText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RecordButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PlayButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
  }

  @Test
  fun testToggleRecordingButton() {
    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = "Sample Question",
          offlineSpeakingViewModel = offlineSpeakingViewModel)
    }

    // Start recording
    composeTestRule.onNodeWithTag("RecordButton").performClick()
    verify(offlineSpeakingViewModel).startRecording()

    // Stop recording
    composeTestRule.onNodeWithTag("RecordButton").performClick()
    verify(offlineSpeakingViewModel).stopRecording()
  }

  @Test
  fun testPlayRecordingButton() {
    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = "Sample Question",
          offlineSpeakingViewModel = offlineSpeakingViewModel)
    }

    composeTestRule.onNodeWithTag("PlayButton").performClick()
    verify(offlineSpeakingViewModel).playRecording()
  }

  @Test
  fun testBackButtonNavigatesCorrectly() {
    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = "Sample Question",
          offlineSpeakingViewModel = offlineSpeakingViewModel)
    }

    composeTestRule.onNodeWithTag("BackButton").performClick()
    verify(navigationActions).goBack()
  }
}
