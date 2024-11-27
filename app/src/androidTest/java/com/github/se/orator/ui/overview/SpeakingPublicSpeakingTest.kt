package com.github.se.orator.ui.overview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
    navigationActions = mock(NavigationActions::class.java)
    val chatGPTService = mock(ChatGPTService::class.java)
    apiLinkViewModel = ApiLinkViewModel()

    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING_JOB_INTERVIEW)
    composeTestRule.setContent {
      SpeakingPublicSpeakingModule(
          navigationActions = navigationActions, chatViewModel, apiLinkViewModel)
    }
  }

  @Test
  fun testInputFieldsDisplayed() {
    composeTestRule.onNodeWithTag("occasionInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("demographicInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mainPointsInput").assertIsDisplayed()
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
    composeTestRule.onNodeWithTag("occasionInput").performTextInput("fun")
    composeTestRule.onNodeWithTag("demographicInput").performTextInput("phd")
    composeTestRule.onNodeWithTag("mainPointsInput").performTextInput("stuff")
    composeTestRule.onNodeWithTag("feedbackLanguageInput").performTextInput("maybe")

    composeTestRule.onNodeWithTag("occasionInput").assertTextContains("fun")
    composeTestRule.onNodeWithTag("demographicInput").assertTextContains("phd")
    composeTestRule.onNodeWithTag("mainPointsInput").assertTextContains("stuff")
    composeTestRule.onNodeWithTag("feedbackLanguageInput").assertTextContains("maybe")
    Espresso.closeSoftKeyboard()
    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    // verify(apiLinkViewModel).updatePracticeContext(any())
    composeTestRule.onNodeWithTag("getStartedButton").assertExists()
  }
}
