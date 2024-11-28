package com.github.se.orator.ui.speaking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
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
  private lateinit var data: AnalysisData
  private lateinit var speech: String

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    apiLinkViewModel = ApiLinkViewModel()
    userProfileViewModel = mock()
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
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel)
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
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel)
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
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel)
    }

    composeTestRule.onNodeWithTag("mic_text").assertTextContains("Processing...")
    // composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Start recording").assertIsDisplayed()
  }

  @Test
  fun testTranscript() {

    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.FINISHED))
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel)
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
      SpeakingScreen(navigationActions = navigationActions, viewModel = speakingViewModel)
    }

    // Verify that the AudioVisualizer is displayed
    composeTestRule.onNodeWithTag("audio_visualizer").assertExists().assertIsDisplayed()
  }
}
