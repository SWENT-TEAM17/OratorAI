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

class SpeakingPublicSpeakingTest {

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

    // Mock currentRoute() to return the Public Speaking screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING_PUBLIC_SPEAKING)

    // Set the composable content for testing
    composeTestRule.setContent {
      SpeakingPublicSpeakingModule(
          navigationActions = navigationActions,
          chatViewModel = chatViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }
  }

  @Test
  fun testInputFieldsDisplayed() {
    composeTestRule.onRoot().printToLog("UI_TREE")

    // Scroll to and verify that all input fields are displayed

    // occasionInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("occasionInput-TextField"))
    composeTestRule.onNodeWithTag("occasionInput-TextField").assertIsDisplayed()

    // purposeInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("purposeInput-DropdownBox"))
    composeTestRule.onNodeWithTag("purposeInput-DropdownBox").assertIsDisplayed()

    // audienceSizeInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("audienceSizeInput-DropdownBox"))
    composeTestRule.onNodeWithTag("audienceSizeInput-DropdownBox").assertIsDisplayed()

    // demographicInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("demographicInput-TextField"))
    composeTestRule.onNodeWithTag("demographicInput-TextField").assertIsDisplayed()

    // presentationStyleInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("presentationStyleInput-DropdownBox"))
    composeTestRule.onNodeWithTag("presentationStyleInput-DropdownBox").assertIsDisplayed()

    // mainPointsInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("mainPointsInput-TextField"))
    composeTestRule.onNodeWithTag("mainPointsInput-TextField").assertIsDisplayed()

    // visualAidsInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("visualAidsInput-DropdownBox"))
    composeTestRule.onNodeWithTag("visualAidsInput-DropdownBox").assertIsDisplayed()

    // experienceLevelInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("experienceLevelInput-DropdownBox"))
    composeTestRule.onNodeWithTag("experienceLevelInput-DropdownBox").assertIsDisplayed()

    // anticipatedChallengesInput-TextField
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("anticipatedChallengesInput-TextField"))
    composeTestRule.onNodeWithTag("anticipatedChallengesInput-TextField").assertIsDisplayed()

    // focusAreaInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("focusAreaInput-DropdownBox"))
    composeTestRule.onNodeWithTag("focusAreaInput-DropdownBox").assertIsDisplayed()

    // feedbackTypeInput-DropdownBox
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("feedbackTypeInput-DropdownBox"))
    composeTestRule.onNodeWithTag("feedbackTypeInput-DropdownBox").assertIsDisplayed()

    // Verify header and other UI elements

    // Scroll to titleText
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag("titleText"))
    composeTestRule.onNodeWithTag("titleText", useUnmergedTree = true).assertIsDisplayed()

    // topAppBar and screenTitle are likely at the top and may not need scrolling
    composeTestRule.onNodeWithTag("topAppBar", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("content").assertIsDisplayed()

    // Verify back button and its functionality
    composeTestRule.onNodeWithTag("back_button", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button", useUnmergedTree = true).performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun testInputFieldsInteraction() {
    // Scroll to the occasionInput field
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("occasionInput-TextField"))
    // Input text into occasionInput
    composeTestRule.onNodeWithTag("occasionInput-TextField").performTextInput("Conference")
    composeTestRule.onNodeWithTag("occasionInput-TextField").assertTextContains("Conference")

    // Scroll to purposeInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("purposeInput-DropdownBox"))
    // Select an option from purposeInput dropdown
    composeTestRule.onNodeWithTag("purposeInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Inform").performClick()
    composeTestRule.onNodeWithTag("purposeInput-DropdownField").assertTextContains("Inform")

    // Scroll to audienceSizeInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("audienceSizeInput-DropdownBox"))
    // Select an option from audienceSizeInput dropdown
    composeTestRule.onNodeWithTag("audienceSizeInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Medium group (20-50)").performClick()
    composeTestRule
        .onNodeWithTag("audienceSizeInput-DropdownField")
        .assertTextContains("Medium group (20-50)")

    // Scroll to demographicInput
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("demographicInput-TextField"))
    // Input text into demographicInput
    composeTestRule.onNodeWithTag("demographicInput-TextField").performTextInput("Professionals")
    composeTestRule.onNodeWithTag("demographicInput-TextField").assertTextContains("Professionals")

    // Scroll to presentationStyleInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("presentationStyleInput-DropdownBox"))
    // Select an option from presentationStyleInput dropdown
    composeTestRule.onNodeWithTag("presentationStyleInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Interactive").performClick()
    composeTestRule
        .onNodeWithTag("presentationStyleInput-DropdownField")
        .assertTextContains("Interactive")

    // Scroll to mainPointsInput
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("mainPointsInput-TextField"))
    // Input text into mainPointsInput
    composeTestRule
        .onNodeWithTag("mainPointsInput-TextField")
        .performTextInput("Innovation, Leadership, Teamwork")
    composeTestRule
        .onNodeWithTag("mainPointsInput-TextField")
        .assertTextContains("Innovation, Leadership, Teamwork")

    // Scroll to visualAidsInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("visualAidsInput-DropdownBox"))
    // Select an option from visualAidsInput dropdown
    composeTestRule.onNodeWithTag("visualAidsInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Yes").performClick()
    composeTestRule.onNodeWithTag("visualAidsInput-DropdownField").assertTextContains("Yes")

    // Scroll to experienceLevelInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("experienceLevelInput-DropdownBox"))
    // Select an option from experienceLevelInput dropdown
    composeTestRule.onNodeWithTag("experienceLevelInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Intermediate").performClick()
    composeTestRule
        .onNodeWithTag("experienceLevelInput-DropdownField")
        .assertTextContains("Intermediate")

    // Scroll to anticipatedChallengesInput
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("anticipatedChallengesInput-TextField"))
    // Input text into anticipatedChallengesInput
    composeTestRule
        .onNodeWithTag("anticipatedChallengesInput-TextField")
        .performTextInput("Nervousness, Audience engagement")
    composeTestRule
        .onNodeWithTag("anticipatedChallengesInput-TextField")
        .assertTextContains("Nervousness, Audience engagement")

    // Scroll to focusAreaInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("focusAreaInput-DropdownBox"))
    // Select an option from focusAreaInput dropdown
    composeTestRule.onNodeWithTag("focusAreaInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Delivery Style").performClick()
    composeTestRule
        .onNodeWithTag("focusAreaInput-DropdownField")
        .assertTextContains("Delivery Style")

    // Scroll to feedbackTypeInput dropdown
    composeTestRule
        .onNodeWithTag("content")
        .performScrollToNode(hasTestTag("feedbackTypeInput-DropdownBox"))
    // Select an option from feedbackTypeInput dropdown
    composeTestRule.onNodeWithTag("feedbackTypeInput-DropdownBox").performClick()
    composeTestRule.onNodeWithText("Vocal Variety").performClick()
    composeTestRule
        .onNodeWithTag("feedbackTypeInput-DropdownField")
        .assertTextContains("Vocal Variety")

    // Close the soft keyboard
    Espresso.closeSoftKeyboard()

    // Scroll to the Get Started button
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag("getStartedButton"))
    // Click on the Get Started button
    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    composeTestRule.onNodeWithTag("getStartedButton").assertExists()
  }
}
