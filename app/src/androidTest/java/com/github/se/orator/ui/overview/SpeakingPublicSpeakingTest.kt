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
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class SpeakingPublicSpeakingTest {

  @get:Rule
  val composeTestRule = createComposeRule()

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
        apiLinkViewModel = apiLinkViewModel
      )
    }
  }

  @Test
  fun testInputFieldsDisplayed() {
    // Verify that all input fields are displayed
    composeTestRule.onNodeWithTag("occasionInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("purposeInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("audienceSizeInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("demographicInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("presentationStyleInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mainPointsInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("visualAidsInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("experienceLevelInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("anticipatedChallengesInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("focusAreaInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackTypeInput").assertIsDisplayed()

    // Verify header and other UI elements
    composeTestRule.onNodeWithText("Make your speech memorable").assertIsDisplayed()
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
    // Input text into occasionInput
    composeTestRule.onNodeWithTag("occasionInput").performTextInput("Conference")
    composeTestRule.onNodeWithTag("occasionInput").assertTextContains("Conference")

    // Select an option from purposeInput dropdown
    composeTestRule.onNodeWithTag("purposeInput").performClick()
    composeTestRule.onNodeWithText("Inform").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("purposeInput").assertTextContains("Inform")

    // Select an option from audienceSizeInput dropdown
    composeTestRule.onNodeWithTag("audienceSizeInput").performClick()
    composeTestRule.onNodeWithText("Medium group (20-50)").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("audienceSizeInput").assertTextContains("Medium group (20-50)")

    // Input text into demographicInput
    composeTestRule.onNodeWithTag("demographicInput").performTextInput("Professionals")
    composeTestRule.onNodeWithTag("demographicInput").assertTextContains("Professionals")

    // Select an option from presentationStyleInput dropdown
    composeTestRule.onNodeWithTag("presentationStyleInput").performClick()
    composeTestRule.onNodeWithText("Interactive").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("presentationStyleInput").assertTextContains("Interactive")

    // Input text into mainPointsInput
    composeTestRule.onNodeWithTag("mainPointsInput").performTextInput("Innovation, Leadership, Teamwork")
    composeTestRule.onNodeWithTag("mainPointsInput").assertTextContains("Innovation, Leadership, Teamwork")

    // Select an option from visualAidsInput dropdown
    composeTestRule.onNodeWithTag("visualAidsInput").performClick()
    composeTestRule.onNodeWithText("Yes").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("visualAidsInput").assertTextContains("Yes")

    // Select an option from experienceLevelInput dropdown
    composeTestRule.onNodeWithTag("experienceLevelInput").performClick()
    composeTestRule.onNodeWithText("Intermediate").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("experienceLevelInput").assertTextContains("Intermediate")

    // Input text into anticipatedChallengesInput
    composeTestRule.onNodeWithTag("anticipatedChallengesInput").performTextInput("Nervousness, Audience engagement")
    composeTestRule.onNodeWithTag("anticipatedChallengesInput").assertTextContains("Nervousness, Audience engagement")

    // Select an option from focusAreaInput dropdown
    composeTestRule.onNodeWithTag("focusAreaInput").performClick()
    composeTestRule.onNodeWithText("Delivery Style").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("focusAreaInput").assertTextContains("Delivery Style")

    // Select an option from feedbackTypeInput dropdown
    composeTestRule.onNodeWithTag("feedbackTypeInput").performClick()
    composeTestRule.onNodeWithText("Vocal Variety").performClick() // Replace with a valid option
    composeTestRule.onNodeWithTag("feedbackTypeInput").assertTextContains("Vocal Variety")

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
