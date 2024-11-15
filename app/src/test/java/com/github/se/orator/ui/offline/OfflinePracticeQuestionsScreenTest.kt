package com.github.se.orator.ui.offline

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30], qualifiers = "en")
class OfflinePracticeQuestionsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setup() {
    // Create a mock for NavigationActions and configure the behavior for navigateToOfflineRecording
    navigationActions = mock()

    // Set up navigateToOfflineRecording to avoid NullPointerException by returning Unit
    whenever(navigationActions.navigateToOfflineRecording).thenReturn { _: String -> }
  }

  @Test
  fun testScreenElementsDisplayed() {
    composeTestRule.setContent {
      OfflinePracticeQuestionsScreen(navigationActions = navigationActions)
    }

    // Verify that the main screen and title text are displayed
    composeTestRule.onNodeWithTag("OfflinePracticeQuestionsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TitleText").assertTextEquals("Choose your practice question")

    // Verify that the back button is displayed
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()

    // Verify that each question card and text are displayed
    for (index in 0 until 5) {
      composeTestRule
          .onNodeWithTag("QuestionCard_$index", useUnmergedTree = true)
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag("QuestionText_$index", useUnmergedTree = true).assertExists()
    }
  }

  @Test
  fun testBackButtonNavigation() {
    composeTestRule.setContent {
      OfflinePracticeQuestionsScreen(navigationActions = navigationActions)
    }

    // Click the back button and verify navigation action
    composeTestRule.onNodeWithTag("BackButton").performClick()
    verify(navigationActions).navigateTo(Screen.OFFLINE)
  }

  @Test
  fun testQuestionCardNavigation() {
    val questions =
        listOf(
            "What are your strengths?",
            "Describe a challenging situation you've faced.",
            "Why do you want this position?",
            "Tell me about a time you demonstrated leadership.",
            "How do you handle conflict in a team?")

    composeTestRule.setContent {
      OfflinePracticeQuestionsScreen(navigationActions = navigationActions)
    }

    questions.forEachIndexed { index, question ->
      composeTestRule.onNodeWithTag("QuestionCard_$index").performClick()
      verify(navigationActions).goToOfflineRecording(question)
    }
  }
}
