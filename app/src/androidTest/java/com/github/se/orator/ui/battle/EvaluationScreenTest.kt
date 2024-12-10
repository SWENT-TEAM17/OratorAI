package com.github.se.orator.ui.battle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class EvaluationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockNavigationActions: NavigationActions

  @Test
  fun evaluationScreenDisplaysCorrectly() {
    MockitoAnnotations.openMocks(this)

    composeTestRule.setContent {
      EvaluationScreen(battleId = "testBattleId", navigationActions = mockNavigationActions)
    }

    // Assert that the top app bar title is displayed
    composeTestRule.onNodeWithText("Evaluation in Progress").assertIsDisplayed()

    // Assert that the evaluation message is displayed
    composeTestRule.onNodeWithText("GPT is currently evaluating both answers").assertIsDisplayed()
  }
}
