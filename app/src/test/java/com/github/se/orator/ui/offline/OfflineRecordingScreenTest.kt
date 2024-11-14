package com.github.se.orator.ui.offline

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineRecordingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel
  private val isRecordingFlow = MutableStateFlow(false)

  @Before
  fun setup() {
    navigationActions = mock()
    speakingViewModel = mock()

    // Mock isRecording StateFlow in SpeakingViewModel
    whenever(speakingViewModel.isRecording).thenReturn(isRecordingFlow as StateFlow<Boolean>)
  }

  @Test
  fun testElementsDisplayed() {
    val question = "Why do you want this position?"

    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = question,
          speakingViewModel = speakingViewModel)
    }

    // Verify screen elements are displayed
    composeTestRule.onNodeWithTag("OfflineRecordingScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MicIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("QuestionText").assertTextEquals(question)
    composeTestRule.onNodeWithTag("DoneButton").assertIsDisplayed()
  }

  @Test
  fun testBackButtonNavigation() {
    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = "Sample question",
          speakingViewModel = speakingViewModel)
    }

    // Perform click on back button and verify navigation back action
    composeTestRule.onNodeWithTag("BackButton").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testDoneButtonStopsRecordingAndNavigates() {
    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = "Sample question",
          speakingViewModel = speakingViewModel)
    }

    // Click the "Done!" button and verify it stops recording and navigates
    composeTestRule.onNodeWithTag("DoneButton").performClick()
    verify(speakingViewModel).endAndSave()
    verify(navigationActions).navigateTo(Screen.OFFLINE_RECORDING_REVIEW_SCREEN)
  }

  @Test
  fun testRecordingEndsWhenDoneButtonClicked() {
    // Arrange
    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions = navigationActions,
          question = "Sample question",
          speakingViewModel = speakingViewModel,
      )
    }

    // Act
    // Simulate the "Done!" button click to end recording and trigger saving
    composeTestRule.onNodeWithTag("DoneButton").performClick()

    // Assert
    // Verify that `endAndSave()` is called, indicating that recording has ended
    verify(speakingViewModel).endAndSave()
  }
}
