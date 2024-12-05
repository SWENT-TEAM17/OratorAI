package com.github.se.orator.ui.offline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class RecordingReviewScreen {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var userProfileRepository: UserProfileRepository

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    speakingRepository = mock(SpeakingRepository::class.java)
    apiLinkViewModel = ApiLinkViewModel()
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    speakingViewModel = SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent { RecordingReviewScreen(navigationActions, speakingViewModel) }
  }

  @Test
  fun testEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag("RecordingReviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Back").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hear_recording_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stop_recording_button").assertIsDisplayed()
  }
}
