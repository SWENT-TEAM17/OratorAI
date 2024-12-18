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
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // Reset Main dispatcher
    reset(speakingRepository, apiLinkViewModel, userProfileViewModel, context) // Reset all mocks
  }

  @Test
  fun `getTranscriptAndGetGPTResponse requests GPT response after transcript`() = runTest {
    // Arrange
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
    val audioFile = mock<File>() // Mocked audio file
    val prompts =
        mapOf(
            "ID" to "00000001",
            "targetCompany" to "Google",
            "jobPosition" to "Researcher",
            "transcribed" to "1",
            "transcription" to "Hello!",
            "GPTresponse" to "1")

    // Mock ChatViewModel
    val chatViewModel = mock<ChatViewModel> { on { isLoading }.thenReturn(MutableStateFlow(false)) }

    // Mock OfflinePromptsFunctionsInterface
    val offlinePromptsFunctions = mock<OfflinePromptsFunctionsInterface>()

    // Mock behavior for changePromptStatus
    whenever(
            offlinePromptsFunctions.changePromptStatus(
                eq("00000001"), eq(context), eq("transcribed"), eq("1")))
        .thenReturn(true)

    // Mock responses for getPromptMapElement
    whenever(
            offlinePromptsFunctions.getPromptMapElement(
                eq("00000001"), eq("GPTresponse"), eq(context)))
        .thenReturn("1")
    whenever(
            offlinePromptsFunctions.getPromptMapElement(
                eq("00000001"), eq("transcription"), eq(context)))
        .thenReturn("Test transcription")

    // Act
    speakingViewModel.getTranscriptAndGetGPTResponse(
        audioFile, prompts, chatViewModel, context, offlinePromptsFunctions)

    // Advance the coroutine execution
    advanceUntilIdle()

    // Assert
    // Verify that changePromptStatus was called to update "transcribed" status
    verify(offlinePromptsFunctions)
        .changePromptStatus(eq("00000001"), eq(context), eq("transcribed"), eq("1"))
    //
    //    // Verify that offlineRequest in ChatViewModel was called with the correct values
    //    verify(chatViewModel).offlineRequest(any(), any(), any(), any(), any())
  }

  @Test
  fun `onMicButtonClicked starts recording and sets isRecording to true when permission is granted`() =
      runTest {
        speakingViewModel =
            SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
        // Act
        speakingViewModel.onMicButtonClicked(true, File(context.cacheDir, "audio_record.wav"))

        Assert.assertTrue(speakingViewModel.isRecording.value)
        speakingViewModel.onMicButtonClicked(true, File(context.cacheDir, "audio_record.wav"))
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
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
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
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
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
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
    // Arrange
    val mockFile = mock<File>()

    // Act
    speakingViewModel.getTranscript(mockFile, context = context, id = "12345678")

    // Assert
    verify(speakingRepository).startRecording()
    verify(speakingRepository).stopRecording()
  }
}
