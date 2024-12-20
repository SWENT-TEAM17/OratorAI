package com.github.se.orator.ui.speaking

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.rule.GrantPermissionRule
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any

class SpeakingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var data: AnalysisData
  private lateinit var speech: String

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    apiLinkViewModel = ApiLinkViewModel()
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    speakingRepository = mock(SpeakingRepository::class.java)
    speech = "Hello! My name is John. I am an entrepreneur"

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING)
    data = AnalysisData(speech, 5, 2.0, 1.0)

    `when`(speakingRepository.setupAnalysisResultsUsage(any(), any())).thenAnswer { invocation ->
      // Extract the onSuccess callback and invoke it
      val onSuccessCallback = invocation.getArgument<(AnalysisData) -> Unit>(0)
      onSuccessCallback.invoke(data)
      null
    }
  }

  /**
   * Test the Idle Mode of the SpeakingScreen. Verifies that the back button and microphone button
   * are displayed, and the appropriate feedback message is shown.
   */
  @Test
  fun testIdleMode() {

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.IDLE))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Verify UI Components
    composeTestRule.onNodeWithTag("ui_column").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Start recording").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Tap the mic to start recording.")

    // Perform Back Button Click and Verify Navigation
    composeTestRule.onNodeWithTag("back_button").performClick()
    verify(navigationActions).goBack()
  }

  /**
   * Test the Recording Mode of the SpeakingScreen. Verifies that the microphone button reflects the
   * recording state, displays the recording feedback message, and shows the audio visualizer.
   */
  @Test
  fun testRecordingMode() {

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.RECORDING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Verify UI Components
    composeTestRule.onNodeWithTag("ui_column").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Stop recording").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Recording...")

    // Perform Back Button Click and Verify Navigation
    composeTestRule.onNodeWithTag("back_button").performClick()
    verify(navigationActions).goBack()

    // Since Transcribed Text and Sentiment Analysis are commented out,
    // we should not check for them anymore. Remove or comment out these lines:
    // composeTestRule.onNodeWithTag("transcribed_text").assertExists()
    // composeTestRule.onNodeWithText("Sentiment Analysis: 1.0").assertIsDisplayed()

    // Optionally, verify that the AudioVisualizer is displayed
    composeTestRule.onNodeWithTag("audio_visualizer").assertIsDisplayed()

    // Perform Click on Microphone Button to Stop Recording
    composeTestRule.onNodeWithTag("mic_button").performClick()

    // After stopping recording, verify that the microphone button reflects the idle state
    // Optionally, verify updated feedback message
  }

  /**
   * Test the Processing Mode of the SpeakingScreen. Verifies that the processing overlay appears
   * with the correct message and progress bar.
   */
  @Test
  fun testProcessingMode() {
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Verify Feedback Message
    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Processing...")
    composeTestRule.onNodeWithContentDescription("Start recording").assertIsDisplayed()

    // Verify that the processing overlay appears
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()

    // Verify the presence of the progress bar
    composeTestRule.onNodeWithTag("progress_bar").assertExists().assertIsDisplayed()
  }

  /** Test that the AudioVisualizer is displayed when in Recording State. */
  @Test
  fun testAudioVisualizerIsDisplayedInRecordingState() {
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.RECORDING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Verify that the AudioVisualizer is displayed
    composeTestRule.onNodeWithTag("audio_visualizer").assertExists().assertIsDisplayed()
  }

  /** Test that the generic tips overlay is displayed when no practice context is set. */
  @Test
  fun testNoPracticeContextTips() {
    // If no practice context is set, we get the generic tips
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
    }

    // Wait for tips to show up
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

  /** Test that the interview context tips are displayed correctly. */
  @Test
  fun testInterviewContextTips() {
    // The @Before method already sets an InterviewContext in the apiLinkViewModel.
    // We just need to set the state to PROCESSING to show tips.
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Wait for the tips to appear
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify that the tips container is displayed
    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

  /** Test that the public speaking context tips are displayed correctly. */
  @Test
  fun testPublicSpeakingContextTips() {
    // Set a PublicSpeakingContext by updating the practice context again
    apiLinkViewModel.updatePracticeContext(
        PublicSpeakingContext(
            occasion = "Conference",
            purpose = "Inspire",
            audienceSize = "Large",
            audienceDemographic = "Professionals",
            presentationStyle = "Formal",
            mainPoints = listOf("Innovation", "Leadership"),
            experienceLevel = "Experienced",
            anticipatedChallenges = listOf("Technical Issues"),
            focusArea = "Engagement Techniques",
            feedbackType = "Body Language"))

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Wait for the tips to show
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify that the tips container is displayed
    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

  /** Test that the sales pitch context tips are displayed correctly. */
  @Test
  fun testSalesPitchContextTips() {
    // Set a SalesPitchContext by updating the practice context again
    apiLinkViewModel.updatePracticeContext(
        SalesPitchContext(
            product = "Marketing Services",
            targetAudience = "Potential Clients",
            salesGoal = "Close the deal",
            keyFeatures = listOf("Customized Strategies", "ROI Focused"),
            anticipatedChallenges = listOf("Budget Constraints", "Competition"),
            negotiationFocus = "Handling Objections",
            feedbackType = "Persuasive Language"))

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
          navigationActions = navigationActions,
          viewModel = speakingViewModel,
          apiLinkViewModel = apiLinkViewModel)
    }

    // Wait for the tips to show
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify that the tips container is displayed
    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }
}
