package com.github.se.orator.model.symblAi

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class SpeakingViewModelTest {
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var context: Context
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    speakingRepository = mock()
    apiLinkViewModel = mock()
    userProfileViewModel = mock()
    context = mock()
    Dispatchers.setMain(testDispatcher)

    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
  }

  @Test
  fun `onMicButtonClicked starts recording and sets isRecording to true when permission is granted`() =
      runTest {
        // Act
        speakingViewModel.onMicButtonClicked(true, File(context.cacheDir, "audio_record.wav"))
        // Assert
        verify(speakingRepository).setupAnalysisResultsUsage(any(), any())
        verify(speakingRepository).startRecording(any())
        Assert.assertTrue(speakingViewModel.isRecording.value)
      }

  @Test
  fun `onMicButtonClicked stops recording and sets isRecording to false when permission is granted and already recording`() =
      runTest {
        // Arrange
        speakingViewModel =
            SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
        speakingViewModel.onMicButtonClicked(
            true, File(context.cacheDir, "audio_record.wav")) // Start recording
        reset(speakingRepository) // Reset to verify stopRecording
        // Act
        speakingViewModel.onMicButtonClicked(
            true, File(context.cacheDir, "audio_record.wav")) // Stop recording
        // Assert
        verify(speakingRepository).stopRecording()
        Assert.assertFalse(speakingViewModel.isRecording.value)
      }

  @Test
  fun `setupAnalysisResultsUsage onSuccess updates the analysisData value`() = runTest {
    // Arrange
    val analysisData = mock<AnalysisData>()
    whenever(speakingRepository.setupAnalysisResultsUsage(any(), any())).thenAnswer {
      val onSuccess = it.arguments[0] as (Any) -> Unit
      onSuccess(analysisData) // Simulate success callback
    }
    // Act
    speakingViewModel.onMicButtonClicked(true, File(context.cacheDir, "audio_record.wav"))
    // Assert
    assert(speakingViewModel.analysisData.value == analysisData)
  }

  @Test
  fun `onMicButtonClicked toggles isRecording state correctly`() = runTest {
    // Arrange
    // Initial state: not recording
    Assert.assertFalse(speakingViewModel.isRecording.value)
    // Act: Start recording
    speakingViewModel.onMicButtonClicked(true, File(context.cacheDir, "audio_record.wav"))
    Assert.assertTrue(speakingViewModel.isRecording.value)
    // Act: Stop recording
    speakingViewModel.onMicButtonClicked(true, File(context.cacheDir, "audio_record.wav"))
    Assert.assertFalse(speakingViewModel.isRecording.value)
  }

  @Test
  fun `getTranscript starts and stops recording`() = runTest {
    // Arrange
    val mockFile = mock<File>()

    // Act
    speakingViewModel.getTranscript(mockFile)

    // Assert
    verify(speakingRepository).startRecording()
    verify(speakingRepository).stopRecording()
  }

  @Test
  fun `getTranscriptAndGetGPTResponse requests GPT response after transcript`() = runTest(testDispatcher) {
    // mocking
    val audioFile = mock<File>()
    val prompts = mapOf("ID" to "00000000", "targetCompany" to "google", "jobPosition" to "researcher")
    val chatViewModel = mock<ChatViewModel> {
      on { isLoading }.thenReturn(MutableStateFlow(false))
    }
    val offlinePromptsFunctions = mock<OfflinePromptsFunctionsInterface>()

    whenever(chatViewModel.isLoading).thenReturn(MutableStateFlow(false))

    whenever(speakingRepository.getTranscript(eq(audioFile), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (AnalysisData) -> Unit
      onSuccess(mock<AnalysisData>().apply { whenever(transcription).thenReturn("Test transcription") })
    }

    // calling get transcript and then gpt response
    speakingViewModel.getTranscriptAndGetGPTResponse(audioFile, prompts, chatViewModel, context, offlinePromptsFunctions)

    // waiting for isLoading to be checked since it's in a separate thread
    advanceUntilIdle()
    advanceUntilIdle()
    advanceUntilIdle()
    advanceUntilIdle()
    // assert the function is being called
    verify(chatViewModel).offlineRequest(
      any(),
      eq("google"),
      eq("researcher"),
      eq("00000000"),
      any()
    )
  }


}
