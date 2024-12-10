package com.github.se.orator.model.symblAi

import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class SpeakingViewModelTest {
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var userProfileViewModel: UserProfileViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    speakingRepository = mock()
    apiLinkViewModel = mock()
    userProfileViewModel = mock()
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
  }

  @Test
  fun `onMicButtonClicked starts recording and sets isRecording to true when permission is granted`() =
      runTest {
        // Act
        speakingViewModel.onMicButtonClicked(true)
        // Assert
        verify(speakingRepository).setupAnalysisResultsUsage(any(), any())
        verify(speakingRepository).startRecording()
        Assert.assertTrue(speakingViewModel.isRecording.value)
      }

  @Test
  fun `onMicButtonClicked stops recording and sets isRecording to false when permission is granted and already recording`() =
      runTest {
        // Arrange
        speakingViewModel =
            SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
        speakingViewModel.onMicButtonClicked(true) // Start recording
        reset(speakingRepository) // Reset to verify stopRecording
        // Act
        speakingViewModel.onMicButtonClicked(true) // Stop recording
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
    speakingViewModel.onMicButtonClicked(true)
    // Assert
    assert(speakingViewModel.analysisData.value == analysisData)
  }

  @Test
  fun `onMicButtonClicked toggles isRecording state correctly`() = runTest {
    // Arrange
    // Initial state: not recording
    Assert.assertFalse(speakingViewModel.isRecording.value)
    // Act: Start recording
    speakingViewModel.onMicButtonClicked(true)
    Assert.assertTrue(speakingViewModel.isRecording.value)
    // Act: Stop recording
    speakingViewModel.onMicButtonClicked(true)
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
}
