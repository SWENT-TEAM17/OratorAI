package com.github.se.orator.ui.offline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctions
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.overview.OfflineInterviewModule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class OfflineInterviewModuleTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var offlinePromptsFunctions: OfflinePromptsFunctions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    offlinePromptsFunctions = mock(OfflinePromptsFunctions()::class.java)
    speakingRepository = mock(SpeakingRepository::class.java)
    apiLinkViewModel = ApiLinkViewModel()
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
    composeTestRule.setContent {
      OfflineInterviewModule(navigationActions, speakingViewModel, offlinePromptsFunctions)
    }
  }

  @Test
  fun testEverythingDisplayed() {
    composeTestRule.onNodeWithTag("company").assertIsDisplayed()
    composeTestRule.onNodeWithTag("jobInput").assertIsDisplayed()
    composeTestRule.onNodeWithTag("content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("doneButton").assertIsDisplayed()

    composeTestRule.onNodeWithText("Go to questions screen").assertIsDisplayed()
  }

  @Test
  fun inputJobAndCompany() {
    composeTestRule.onNodeWithTag("company").performTextInput("Apple")
    composeTestRule.onNodeWithTag("jobInput").performTextInput("Engineer")

    composeTestRule.onNodeWithTag("company").assertTextContains("Apple")
    composeTestRule.onNodeWithTag("jobInput").assertTextContains("Engineer")
  }

  @Test
  fun testButtonFunctionality() {
    composeTestRule.onNodeWithTag("doneButton").performClick()
    verify(navigationActions).navigateTo(Screen.PRACTICE_QUESTIONS_SCREEN)
  }
}
