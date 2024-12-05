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
    composeTestRule.onRoot().printToLog("UI_TREE")

    // Scroll to and verify that all input fields are displayed

    // productTypeInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("productTypeInput-TextField"))
    composeTestRule.onNodeWithTag("productTypeInput-TextField").assertIsDisplayed()

    // targetAudienceInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("targetAudienceInput-TextField"))
    composeTestRule.onNodeWithTag("targetAudienceInput-TextField").assertIsDisplayed()

    // salesGoalInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("salesGoalInput-DropdownBox"))
    composeTestRule.onNodeWithTag("salesGoalInput-DropdownBox").assertIsDisplayed()

    // keySellingPointsInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("keySellingPointsInput-TextField"))
    composeTestRule.onNodeWithTag("keySellingPointsInput-TextField").assertIsDisplayed()

    // anticipatedChallengesInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("anticipatedChallengesInput-TextField"))
    composeTestRule.onNodeWithTag("anticipatedChallengesInput-TextField").assertIsDisplayed()

    // negotiationFocusInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("negotiationFocusInput-DropdownBox"))
    composeTestRule.onNodeWithTag("negotiationFocusInput-DropdownBox").assertIsDisplayed()

    // feedbackTypeInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("feedbackTypeInput-DropdownBox"))
    composeTestRule.onNodeWithTag("feedbackTypeInput-DropdownBox").assertIsDisplayed()

    // Verify header and other UI elements

    // Scroll to titleText
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag("titleText"))
    composeTestRule.onNodeWithTag("titleText", useUnmergedTree = true).assertIsDisplayed()

    // Verify header text
    composeTestRule
        .onNodeWithText("Master your sales pitch and negotiation skills")
        .assertIsDisplayed()

    // topAppBar and screenTitle are likely at the top and may not need scrolling
    composeTestRule.onNodeWithTag("topAppBar", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("screenTitle", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("content").assertIsDisplayed()

    // Verify back button and its functionality
    composeTestRule.onNodeWithTag("back_button", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button", useUnmergedTree = true).performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testInputFieldsInteraction() {
    // Scroll to productTypeInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("productTypeInput-TextField"))
    // Input text into productTypeInput
    composeTestRule
        .onNodeWithTag("productTypeInput-TextField")
        .performTextInput("Marketing Services")
    composeTestRule
        .onNodeWithTag("productTypeInput-TextField")
        .assertTextContains("Marketing Services")

    // Scroll to targetAudienceInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("targetAudienceInput-TextField"))
    // Input text into targetAudienceInput
    composeTestRule.onNodeWithTag("targetAudienceInput-TextField").performTextInput("Investors")
    composeTestRule.onNodeWithTag("targetAudienceInput-TextField").assertTextContains("Investors")

    // Scroll to salesGoalInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("salesGoalInput-DropdownBox"))
    // Select an option from salesGoalInput dropdown
    composeTestRule.onNodeWithTag("salesGoalInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Close the deal").performClick()
    composeTestRule
        .onNodeWithTag("salesGoalInput-DropdownField")
        .assertTextContains("Close the deal")

    // Scroll to keySellingPointsInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("keySellingPointsInput-TextField"))
    // Input text into keySellingPointsInput
    composeTestRule
        .onNodeWithTag("keySellingPointsInput-TextField")
        .performTextInput("Price, Quality, Innovation")
    composeTestRule
        .onNodeWithTag("keySellingPointsInput-TextField")
        .assertTextContains("Price, Quality, Innovation")

    // Scroll to anticipatedChallengesInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("anticipatedChallengesInput-TextField"))
    // Input text into anticipatedChallengesInput
    composeTestRule
        .onNodeWithTag("anticipatedChallengesInput-TextField")
        .performTextInput("Budget constraints, Competition")
    composeTestRule
        .onNodeWithTag("anticipatedChallengesInput-TextField")
        .assertTextContains("Budget constraints, Competition")

    // Scroll to negotiationFocusInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("negotiationFocusInput-DropdownBox"))
    // Select an option from negotiationFocusInput dropdown
    composeTestRule.onNodeWithTag("negotiationFocusInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Handling Objections").performClick()
    composeTestRule
        .onNodeWithTag("negotiationFocusInput-DropdownField")
        .assertTextContains("Handling Objections")

    // Scroll to feedbackTypeInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("feedbackTypeInput-DropdownBox"))
    // Select an option from feedbackTypeInput dropdown
    composeTestRule.onNodeWithTag("feedbackTypeInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Persuasive Language").performClick()
    composeTestRule
        .onNodeWithTag("feedbackTypeInput-DropdownField")
        .assertTextContains("Persuasive Language")

    // Close the soft keyboard
    Espresso.closeSoftKeyboard()

    // Scroll to the Get Started button
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag("getStartedButton"))
    // Click on the Get Started button
    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    composeTestRule.onNodeWithTag("getStartedButton").assertExists()
  }
}
