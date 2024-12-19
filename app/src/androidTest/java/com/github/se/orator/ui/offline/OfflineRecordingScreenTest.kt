package com.github.se.orator.ui.offline

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.any
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class OfflineRecordingScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
    GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var testPermissionGranted: MutableState<Boolean>
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var mockOfflinePromptsFunctions: OfflinePromptsFunctionsInterface

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    speakingRepository = mock(SpeakingRepository::class.java)
    apiLinkViewModel = ApiLinkViewModel()
    testPermissionGranted = mutableStateOf(false)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    mockOfflinePromptsFunctions = mock(OfflinePromptsFunctionsInterface::class.java)
    speakingViewModel = SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    // Mocking the response for getPromptMapElement
    `when`(mockOfflinePromptsFunctions.getPromptMapElement(anyString(), anyString(), any()))
      .thenReturn("Test Company")

    composeTestRule.setContent {
      OfflineRecordingScreen(
        navigationActions = navigationActions,
        question = "What are your greatest strengths?",
        viewModel = speakingViewModel,
        permissionGranted = testPermissionGranted,
        offlinePromptsFunctions = mockOfflinePromptsFunctions
      )
    }
  }

  @Test
  fun assertEverythingIsDisplayed() {
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
  fun testPermissionHandling_denied() {
    // Simulate permission denied
    testPermissionGranted.value = false
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Verify that startRecording was never called
    verify(speakingRepository, never()).startRecording(any())
    verify(speakingRepository, never()).stopRecording()
  }

  @Test
  fun testPermissionHandling_granted() {
    // Simulate permission granted
    testPermissionGranted.value = true
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Verify that startRecording was called
    verify(speakingRepository).startRecording(any())
  }

  @Test
  fun testMicButton_startAndStopRecording() {
    // Grant permission
    testPermissionGranted.value = true

    // Click mic button to start recording
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Verify startRecording was called
    verify(speakingRepository).startRecording(any())

    // Click mic button to stop recording
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Verify stopRecording was called
    verify(speakingRepository).stopRecording()
  }

  @Test
  fun testDoneButton_enabledAfterRecording() {
    // Grant permission and start recording
    testPermissionGranted.value = true
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Stop recording
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Simulate that the file has been saved
    // Since fileSaved is a MutableStateFlow inside the composable, we need to mock the behavior
    // Alternatively, you can use a test double for OfflinePromptsFunctions to simulate file saving

    // For this test, assume that speakingViewModel.endAndSave() has been called elsewhere
    // Trigger the "Done" button
    composeTestRule.onNodeWithTag("DoneButton").performClick()

    // Verify navigation to review screen
    verify(navigationActions).navigateTo(Screen.OFFLINE_RECORDING_REVIEW_SCREEN)
  }

  @Test
  fun testDoneButton_disabledDuringRecording() {
    // Grant permission and start recording
    testPermissionGranted.value = true
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // "Done" button should not trigger navigation while recording
    composeTestRule.onNodeWithTag("DoneButton").performClick()

    // Verify that navigation does not occur
    verify(navigationActions, never()).navigateTo(Screen.OFFLINE_RECORDING_REVIEW_SCREEN)
  }

  @Test
  fun testNavigationOnDone() {
    // Grant permission, start and stop recording
    testPermissionGranted.value = true
    composeTestRule.onNodeWithTag("mic_button").performClick()
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Click "Done" button
    composeTestRule.onNodeWithTag("DoneButton").performClick()

    // Verify navigation to review screen
    verify(navigationActions).navigateTo(Screen.OFFLINE_RECORDING_REVIEW_SCREEN)
  }

  @Test
  fun testRecordingFeedbackMessage_displayed() {
    // Check that the initial feedback message is displayed
    composeTestRule.onNodeWithTag("mic_text").assertTextEquals("Tap once to record, tap again to stop returning.")
  }

  @Test
  fun testQuestionText_displayedCorrectly() {
    composeTestRule.onNodeWithTag("QuestionText").assertTextEquals("Make sure to focus on: What are your greatest strengths?")
  }

  @Test
  fun testTargetCompany_displayedCorrectly() {
    composeTestRule.onNodeWithTag("targetCompany").assertTextEquals("Target company: Test Company")
  }

  @Test
  fun testDoneButton_notNavigatingWhenNotSaved() {
    // Grant permission and perform a recording without stopping or saving
    testPermissionGranted.value = true
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // Attempt to click "Done" without saving
    composeTestRule.onNodeWithTag("DoneButton").performClick()

    // Verify that navigation does not happen
    verify(navigationActions, never()).navigateTo(Screen.OFFLINE_RECORDING_REVIEW_SCREEN)
  }

  // Additional tests can be added here as needed
}
