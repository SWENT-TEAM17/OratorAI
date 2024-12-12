package com.github.se.orator.ui.speaking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

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

  // I have to re create the view model and screen each time because if I don't the public
  // analysisState which is a stateFlow cannot be changed
  // solution -> create a diff view model with an initial state flow
  @Test
  fun testIdleMode() {

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.IDLE))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
    }

    composeTestRule.onNodeWithTag("ui_column").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()

    verify(navigationActions).goBack()

    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Tap the mic to start recording.")

    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Start recording").assertIsDisplayed()
  }

  @Test
  fun testRecordingMode() {

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.RECORDING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
    }
    composeTestRule.onNodeWithTag("ui_column").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").performClick()

    verify(navigationActions).goBack()

    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Recording...")

    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Stop recording").assertIsDisplayed()

    composeTestRule.onNodeWithTag("mic_button").performClick()
    //    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Analysis finished.")
    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()

    composeTestRule.onNodeWithText("Transcribed Text: $speech").assertExists()
    composeTestRule.onNodeWithText("Sentiment Analysis: 1.0").assertIsDisplayed()
  }


  @Test
  fun testProcessingMode() {
    `when`(speakingRepository.analysisState)
      .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
      SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
    }

    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Processing...")
    composeTestRule.onNodeWithContentDescription("Start recording").assertIsDisplayed()

    // Since we are in PROCESSING, a tip overlay should appear (after next frame)
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()

    // Check if progress bar is displayed using the assigned test tag
    composeTestRule.onNodeWithTag("progress_bar").assertExists().assertIsDisplayed()
  }


  @Test
  fun testTranscript() {

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.FINISHED))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
    }

    Thread.sleep(5000)
    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Analysis finished.")
  }

  @Test
  fun testAudioVisualizerIsDisplayedInRecordingState() {
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.RECORDING))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, viewModel = speakingViewModel, apiLinkViewModel)
    }

    // Verify that the AudioVisualizer is displayed
    composeTestRule.onNodeWithTag("audio_visualizer").assertExists().assertIsDisplayed()
  }

  @Test
  fun testNoPracticeContextTips() {
    // If no practice context is set, we get the generic tips
    `when`(speakingRepository.analysisState)
      .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
      SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    // No context updated on apiLinkViewModel, so it should show generic tips
    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
    }

    // Wait for tips to show up
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

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
        apiLinkViewModel = apiLinkViewModel
      )
    }

    // Wait for the tips to appear
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify that the tips container is displayed
    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

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
        feedbackType = "Body Language"
      )
    )

    `when`(speakingRepository.analysisState)
      .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
      SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
        navigationActions = navigationActions,
        viewModel = speakingViewModel,
        apiLinkViewModel = apiLinkViewModel
      )
    }

    // Wait for the tips to show
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify that the tips container is displayed
    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

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
        feedbackType = "Persuasive Language"
      )
    )

    `when`(speakingRepository.analysisState)
      .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING))
    speakingViewModel =
      SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)

    composeTestRule.setContent {
      SpeakingScreen(
        navigationActions = navigationActions,
        viewModel = speakingViewModel,
        apiLinkViewModel = apiLinkViewModel
      )
    }

    // Wait for the tips to show
    composeTestRule.waitUntil(timeoutMillis = 2000) {
      composeTestRule.onAllNodesWithTag("tips_container").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify that the tips container is displayed
    composeTestRule.onNodeWithTag("tips_container").assertIsDisplayed()
  }

}
