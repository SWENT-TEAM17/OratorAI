package com.github.se.orator.ui.offline

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecordingReviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel

  @Before
  fun setup() {
    navigationActions = mock()
    speakingViewModel = mock()
  }

  @Test
  fun testElementsDisplayed() {
    // Set up content for RecordingReviewScreen
    composeTestRule.setContent {
      RecordingReviewScreen(
          navigationActions = navigationActions, speakingViewModel = speakingViewModel)
    }

    // Verify that all UI elements are displayed
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "Soon you will be able to : Play Recording, Comment on it and Save it locally")
        .assertIsDisplayed()
  }

  @Test
  fun testBackButtonNavigation() {
    // Set up content for RecordingReviewScreen
    composeTestRule.setContent {
      RecordingReviewScreen(
          navigationActions = navigationActions, speakingViewModel = speakingViewModel)
    }

    // Click on the back button and verify that the navigation action is triggered
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    verify(navigationActions).goBack()
  }
}
