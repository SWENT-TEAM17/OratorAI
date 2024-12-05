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

    // Scroll and assert each component
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("targetPositionInput-TextField"))
    composeTestRule.onNodeWithTag("targetPositionInput-TextField").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("companyNameInput-TextField"))
    composeTestRule.onNodeWithTag("companyNameInput-TextField").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("interviewTypeInput-DropdownBox"))
    composeTestRule.onNodeWithTag("interviewTypeInput-DropdownBox").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("experienceLevelInput-DropdownBox"))
    composeTestRule.onNodeWithTag("experienceLevelInput-DropdownBox").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("jobDescriptionInput-TextFieldBox"))
    composeTestRule.onNodeWithTag("jobDescriptionInput-TextFieldBox").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("focusAreaInput-DropdownBox"))
    composeTestRule.onNodeWithTag("focusAreaInput-DropdownBox").assertIsDisplayed()

    // Verify back button and its functionality
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testInputFieldsInteraction() {
    // Scroll to the targetPositionInput field
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("targetPositionInput-TextField"))
    // Input text into targetPositionInput
    composeTestRule
        .onNodeWithTag("targetPositionInput-TextField")
        .performTextInput("Software Engineer")
    composeTestRule
        .onNodeWithTag("targetPositionInput-TextField")
        .assertTextContains("Software Engineer")

    // Scroll to the companyNameInput field
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("companyNameInput-TextField"))
    // Input text into companyNameInput
    composeTestRule.onNodeWithTag("companyNameInput-TextField").performTextInput("Tech Corp")
    composeTestRule.onNodeWithTag("companyNameInput-TextField").assertTextContains("Tech Corp")

    // Scroll to the interviewTypeInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("interviewTypeInput-DropdownBox"))
    // Select an option from interviewTypeInput dropdown
    composeTestRule.onNodeWithTag("interviewTypeInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Phone Interview").performClick()
    composeTestRule
        .onNodeWithTag("interviewTypeInput-DropdownField")
        .assertTextContains("Phone Interview")

    // Scroll to the experienceLevelInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("experienceLevelInput-DropdownBox"))
    // Select an option from experienceLevelInput dropdown
    composeTestRule.onNodeWithTag("experienceLevelInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Mid-Level").performClick()
    composeTestRule
        .onNodeWithTag("experienceLevelInput-DropdownField")
        .assertTextContains("Mid-Level")

    // Scroll to the jobDescriptionInput field
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("jobDescriptionInput-TextFieldBox"))
    // Input text into jobDescriptionInput (scrollable TextField)
    composeTestRule
        .onNodeWithTag("jobDescriptionInput-TextFieldBox")
        .onChild()
        .performTextInput("Develop and maintain software applications.")
    composeTestRule
        .onNodeWithTag("jobDescriptionInput-TextFieldBox")
        .onChild()
        .assertTextContains("Develop and maintain software applications.")

    // Scroll to the focusAreaInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("focusAreaInput-DropdownBox"))
    // Select an option from focusAreaInput dropdown
    composeTestRule.onNodeWithTag("focusAreaInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Technical Questions").performClick()
    composeTestRule
        .onNodeWithTag("focusAreaInput-DropdownField")
        .assertTextContains("Technical Questions")

    // Close the soft keyboard
    Espresso.closeSoftKeyboard()

    // Scroll to the Get Started button
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag("getStartedButton"))
    // Click on the Get Started button
    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    composeTestRule.onNodeWithTag("getStartedButton").assertExists()
  }
}
