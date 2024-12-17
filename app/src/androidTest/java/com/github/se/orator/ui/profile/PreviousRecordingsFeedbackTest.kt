package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctions
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.AudioPlayer
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.network.ChatGPTService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class PreviousRecordingsFeedbackTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var navigationActions: NavigationActions
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatGPTService: ChatGPTService
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var mockPlayer: AudioPlayer
  private lateinit var offlinePromptsFunctions: OfflinePromptsFunctionsInterface

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    speakingRepository = mock(SpeakingRepository::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    apiLinkViewModel = ApiLinkViewModel()
    chatGPTService = mock(ChatGPTService::class.java)
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
      offlinePromptsFunctions = mock(OfflinePromptsFunctionsInterface::class.java)

    mockPlayer = mock(AudioPlayer::class.java)

    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
      `when`(offlinePromptsFunctions.fileData).thenReturn(MutableStateFlow("Loading").asStateFlow())
  }

  @Test
  fun testEverythingDisplayed() {
    composeTestRule.setContent {
      PreviousRecordingsFeedbackScreen(
          navigationActions = navigationActions,
          viewModel = chatViewModel,
          speakingViewModel = speakingViewModel,
          offlinePromptsFunctions = offlinePromptsFunctions)
    }
    composeTestRule.onNodeWithTag("RecordingReviewScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("play_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Back").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
  }

  @Test
  fun testBackButtonNavigation() {
    composeTestRule.setContent {
      PreviousRecordingsFeedbackScreen(
          navigationActions = navigationActions,
          viewModel = chatViewModel,
          speakingViewModel = speakingViewModel,
          offlinePromptsFunctions = offlinePromptsFunctions)
    }

    // Click on the back button
    composeTestRule.onNodeWithTag("BackButton").performClick()

    // Verify navigation action was triggered
    verify(navigationActions).goBack()
  }

  @Test
  fun testEditButtonPlaysAudio() {

    composeTestRule.setContent {
      PreviousRecordingsFeedbackScreen(
          navigationActions = navigationActions,
          viewModel = chatViewModel,
          speakingViewModel = speakingViewModel,
          player = mockPlayer,
          offlinePromptsFunctions = offlinePromptsFunctions)
    }

    // Click on the play button
    composeTestRule.onNodeWithTag("play_button").performClick()

    // Verify that the audio player was triggered
    verify(mockPlayer).playFile(any())
  }

  @Test
  fun testLaunchedEffectLogic() {
    composeTestRule.setContent {
      PreviousRecordingsFeedbackScreen(
          navigationActions = navigationActions,
          viewModel = chatViewModel,
          speakingViewModel = speakingViewModel,
          offlinePromptsFunctions = offlinePromptsFunctions)
    }

    // Verify that the expected methods were called in the LaunchedEffect
    //verify(speakingRepository).getTranscript(any(), any(), any())

      verify(offlinePromptsFunctions).clearDisplayText()
      verify(offlinePromptsFunctions).readPromptTextFile(any(), any())

  }
}
