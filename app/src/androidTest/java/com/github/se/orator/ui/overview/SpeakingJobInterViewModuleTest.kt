package com.github.se.orator.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class SpeakingJobInterViewModuleTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var apiLinkViewModel: ApiLinkViewModel

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    apiLinkViewModel = ApiLinkViewModel()

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING_JOB_INTERVIEW)

    composeTestRule.setContent {
      SpeakingJobInterviewModule(navigationActions = navigationActions, apiLinkViewModel)
    }
  }

  @Test
  fun testInputFieldsDisplayed() {
    composeTestRule.onNodeWithTag("levelInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("jobInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("skillsInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("experienceInput").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ace your next job interview").assertIsDisplayed()

    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("titleText").assertIsDisplayed()

    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testInputFields() {
    composeTestRule.onNodeWithTag("levelInput").performTextInput("manager")
    composeTestRule.onNodeWithTag("jobInput").performTextInput("engineer")
    composeTestRule.onNodeWithTag("skillsInput").performTextInput("language")
    composeTestRule.onNodeWithTag("experienceInput").performTextInput("10 years")

    composeTestRule.onNodeWithTag("levelInput").assertTextContains("manager")
    composeTestRule.onNodeWithTag("jobInput").assertTextContains("engineer")
    composeTestRule.onNodeWithTag("skillsInput").assertTextContains("language")
    composeTestRule.onNodeWithTag("experienceInput").assertTextContains("10 years")

    Espresso.closeSoftKeyboard()
    // verify(apiLinkViewModel).updatePracticeContext(any())
    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    composeTestRule.onNodeWithTag("getStartedButton").assertExists()
  }
}
