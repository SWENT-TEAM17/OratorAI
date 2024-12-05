package com.github.se.orator.ui.offline

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

class OfflineRecordingScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var testPermissionGranted: MutableState<Boolean>
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var userProfileRepository: UserProfileRepository


  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    speakingRepository = mock(SpeakingRepository::class.java)
    apiLinkViewModel = ApiLinkViewModel()
    testPermissionGranted = mutableStateOf(false)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    speakingViewModel = SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      OfflineRecordingScreen(
          navigationActions =  navigationActions,
          question =  "What are your greatest strengths?",
          viewModel =  speakingViewModel,
          permissionGranted =  testPermissionGranted)
    }
  }

  @Test
  fun asserEverythingIsDisplayed() {
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RecordingColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MicIconContainer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("QuestionText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DoneButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("OfflineRecordingScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButtonRow").assertIsDisplayed()

    composeTestRule.onNodeWithText("Done!").assertIsDisplayed()
  }

  @Test
  fun testPermissionHandling() {
    // Simulate permission denied
    testPermissionGranted.value = false
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Simulate permission granted
    testPermissionGranted.value = true
    composeTestRule.onNodeWithTag("mic_button").performClick()
    // Check if recording starts
    // assert(speakingViewModel.analysisState.value == SpeakingRepository.AnalysisState.RECORDING)
  }

  @Test
  fun testMicButton() {
    composeTestRule.onNodeWithTag("mic_button").performClick()
  }
}
