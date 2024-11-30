package com.github.se.orator.ui.overview

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class SpeakingJobInterviewModuleTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  @Before
  fun setUp() {
    // Mock NavigationActions and ChatGPTService
    navigationActions = mock(NavigationActions::class.java)
    val chatGPTService = mock(ChatGPTService::class.java)

    // Initialize ApiLinkViewModel and ChatViewModel
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    // Mock currentRoute() to return the Job Interview screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING_JOB_INTERVIEW)

    // Set the composable content for testing
    composeTestRule.setContent {
      SpeakingJobInterviewModule(
          navigationActions = navigationActions,
          chatViewModel = chatViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }
  }

  @Test
  fun testInputFieldsDisplayed() {
    composeTestRule.onRoot().printToLog("UI_TREE")
    // Verify that all input fields are displayed
    composeTestRule.onNodeWithTag("targetPositionInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("companyNameInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("interviewTypeInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("experienceLevelInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("jobDescriptionInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("focusAreaInput").assertIsDisplayed()

    // Verify header and other UI elements
    composeTestRule.onNodeWithText("Ace your next job interview").assertIsDisplayed()
    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("titleText").assertIsDisplayed()

    // Verify back button and its functionality
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testInputFieldsInteraction() {
    // Input text into targetPositionInput
    composeTestRule.onNodeWithTag("targetPositionInput").performTextInput("Software Engineer")
    composeTestRule.onNodeWithTag("targetPositionInput").assertTextContains("Software Engineer")

    // Input text into companyNameInput
    composeTestRule.onNodeWithTag("companyNameInput").performTextInput("Tech Corp")
    composeTestRule.onNodeWithTag("companyNameInput").assertTextContains("Tech Corp")

    // Select an option from interviewTypeInput dropdown
    composeTestRule.onNodeWithTag("interviewTypeInput").performClick()
    composeTestRule.onNodeWithText("Technical").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("interviewTypeInput").assertTextContains("Technical")

    // Select an option from experienceLevelInput dropdown
    composeTestRule.onNodeWithTag("experienceLevelInput").performClick()
    composeTestRule.onNodeWithText("Mid-Level").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("experienceLevelInput").assertTextContains("Mid-Level")

    // Input text into jobDescriptionInput (scrollable TextField)
    composeTestRule
        .onNodeWithTag("jobDescriptionInput")
        .performTextInput("Develop and maintain software applications.")
    composeTestRule
        .onNodeWithTag("jobDescriptionInput")
        .assertTextContains("Develop and maintain software applications.")

    // Select an option from focusAreaInput dropdown
    composeTestRule.onNodeWithTag("focusAreaInput").performClick()
    composeTestRule
        .onNodeWithText("Technical Questions")
        .performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("focusAreaInput").assertTextContains("Technical Questions")

    // Close the soft keyboard
    Espresso.closeSoftKeyboard()

    // Click on the Get Started button
    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    composeTestRule.onNodeWithTag("getStartedButton").assertExists()

    // Optionally, verify that the ViewModel's context was updated
    // This requires spying on the ViewModel or using other verification techniques
    // Example (if using Mockito's spy):
    // val spyApiLinkViewModel = spy(apiLinkViewModel)
    // verify(spyApiLinkViewModel).updatePracticeContext(any())
  }
}
