package com.github.se.orator.ui.offline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class OfflinePracticeQuestionsScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { OfflinePracticeQuestionsScreen(navigationActions) }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag("OfflinePracticeQuestionsScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TitleText").assertIsDisplayed()
    composeTestRule.onNodeWithText("Choose your practice question").assertIsDisplayed()

    composeTestRule.onNodeWithTag("QuestionCard_0")
    composeTestRule.onNodeWithTag("QuestionCard_1")
    composeTestRule.onNodeWithTag("QuestionCard_2")
    composeTestRule.onNodeWithTag("QuestionCard_3")
    composeTestRule.onNodeWithTag("QuestionCard_4")

    composeTestRule.onNodeWithTag("QuestionText_0")
    composeTestRule.onNodeWithTag("QuestionText_1")
    composeTestRule.onNodeWithTag("QuestionText_2")
    composeTestRule.onNodeWithTag("QuestionText_3")
    composeTestRule.onNodeWithTag("QuestionText_4")
  }

  @Test
  fun testButtonsFunctionality() {
    composeTestRule.onNodeWithTag("QuestionCard_0").performClick()
    verify(navigationActions).goToOfflineRecording("What are your strengths?")

    composeTestRule.onNodeWithTag("QuestionCard_1").performClick()
    verify(navigationActions).goToOfflineRecording("Describe a challenging situation you've faced.")

    composeTestRule.onNodeWithTag("QuestionCard_2").performClick()
    verify(navigationActions).goToOfflineRecording("Why do you want this position?")

    composeTestRule.onNodeWithTag("QuestionCard_3").performClick()
    verify(navigationActions)
        .goToOfflineRecording("Tell me about a time you demonstrated leadership.")

    composeTestRule.onNodeWithTag("QuestionCard_4").performClick()
    verify(navigationActions).goToOfflineRecording("How do you handle conflict in a team?")
  }
}
