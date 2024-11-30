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

class SpeakingSalesPitchModuleTest {

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

    // Mock currentRoute() to return the Sales Pitch screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING_SALES_PITCH)

    // Set the composable content for testing
    composeTestRule.setContent {
      SpeakingSalesPitchModule(
          navigationActions = navigationActions,
          chatViewModel = chatViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }
  }

  @Test
  fun testInputFieldsDisplayed() {
    // Verify that all input fields are displayed
    composeTestRule.onNodeWithTag("productTypeInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("targetAudienceInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("salesGoalInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("keySellingPointsInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("anticipatedChallengesInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("negotiationFocusInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackTypeInput").assertIsDisplayed()

    // Verify header and other UI elements
    composeTestRule
        .onNodeWithText("Master your sales pitch and negotiation skills")
        .assertIsDisplayed()
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
    // Input text into productTypeInput
    composeTestRule.onNodeWithTag("productTypeInput").performTextInput("Marketing Services")
    composeTestRule.onNodeWithTag("productTypeInput").assertTextContains("Marketing Services")

    // Input text into targetAudienceInput
    composeTestRule.onNodeWithTag("targetAudienceInput").performTextInput("Investors")
    composeTestRule.onNodeWithTag("targetAudienceInput").assertTextContains("Investors")

    // Select an option from salesGoalInput dropdown
    composeTestRule.onNodeWithTag("salesGoalInput").performClick()
    composeTestRule.onNodeWithText("Close the deal").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("salesGoalInput").assertTextContains("Close the deal")

    // Input text into keySellingPointsInput
    composeTestRule
        .onNodeWithTag("keySellingPointsInput")
        .performTextInput("Price, Quality, Innovation")
    composeTestRule
        .onNodeWithTag("keySellingPointsInput")
        .assertTextContains("Price, Quality, Innovation")

    // Input text into anticipatedChallengesInput
    composeTestRule
        .onNodeWithTag("anticipatedChallengesInput")
        .performTextInput("Budget constraints, Competition")
    composeTestRule
        .onNodeWithTag("anticipatedChallengesInput")
        .assertTextContains("Budget constraints, Competition")

    // Select an option from negotiationFocusInput dropdown
    composeTestRule.onNodeWithTag("negotiationFocusInput").performClick()
    composeTestRule
        .onNodeWithText("Handling Objections")
        .performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("negotiationFocusInput").assertTextContains("Handling Objections")

    // Select an option from feedbackTypeInput dropdown
    composeTestRule.onNodeWithTag("feedbackTypeInput").performClick()
    composeTestRule
        .onNodeWithText("Persuasive Language")
        .performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("feedbackTypeInput").assertTextContains("Persuasive Language")

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
