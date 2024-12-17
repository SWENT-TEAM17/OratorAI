package com.github.se.orator.model.symblAi

import android.content.Context
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import kotlinx.coroutines.Dispatchers
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
    speakingViewModel.getTranscript(mockFile, context = context, id = "12345678")

    // Assert
    verify(speakingRepository).startRecording()
    verify(speakingRepository).stopRecording()
  }

  @Test
  fun `getTranscriptAndGetGPTResponse requests GPT response after transcript`() = runTest {
    // Arrange
    val audioFile = mock<File>() // Audio file mock
    val prompts =
        mapOf(
            "ID" to "00000000",
            "targetCompany" to "Google",
            "jobPosition" to "Researcher",
            "transcribed" to "1",
            "transcription" to "Hello!",
            "GPTresponse" to "1")

    val chatViewModel = mock<ChatViewModel> { on { isLoading }.thenReturn(MutableStateFlow(false)) }

    val offlinePromptsFunctions = mock<OfflinePromptsFunctionsInterface>()

    // Mock changePromptStatus to return true for updating "transcribed"
    whenever(offlinePromptsFunctions.changePromptStatus(any(), any(), eq("transcribed"), eq("1")))
        .thenReturn(true)

    // Mock the retrieval of the GPTresponse and transcription
    whenever(offlinePromptsFunctions.getPromptMapElement("00000000", "GPTresponse", context))
        .thenReturn("1")
    whenever(offlinePromptsFunctions.getPromptMapElement("00000000", "transcription", context))
        .thenReturn("Test transcription")

    // Act
    speakingViewModel.getTranscriptAndGetGPTResponse(
        audioFile, prompts, chatViewModel, context, offlinePromptsFunctions)

    // Ensure all coroutines finish executing
    advanceUntilIdle()

    // Assert
    // Verify that the transcribed status was updated
    verify(offlinePromptsFunctions)
        .changePromptStatus(eq("00000000"), eq(context), eq("transcribed"), eq("1"))

    // Verify that the offlineRequest method in ChatViewModel was called with correct values
    verify(chatViewModel)
        .offlineRequest(
            eq("Test transcription"), // The transcription text
            eq("Google"), // Target company
            eq("Researcher"), // Job position
            eq("00000000"), // Interview ID
            eq(context) // Context
            )
  }
}
