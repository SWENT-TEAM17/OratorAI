package com.github.se.orator.model.symblAi

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpeakingViewModelTest {

  private lateinit var context: Context
  private lateinit var repository: SpeakingRepository
  private lateinit var viewModel: SpeakingViewModel

  @Before
  fun setUp() {
    // Use ApplicationProvider to get a real context for the test
    context = ApplicationProvider.getApplicationContext()

    // Initialize the actual SpeakingRepository with context
    repository = SpeakingRepository(context)

    // Initialize the ViewModel with the repository
    viewModel = SpeakingViewModel(repository)
  }

  @Test
  fun testStartRecordingUpdatesIsRecordingState() = runTest {
    // Simulate microphone permission is granted
    viewModel.onMicButtonClicked(permissionGranted = true)

    // Check if recording has started
    assertTrue(viewModel.isRecording.value)
  }

  @Test
  fun testStopRecordingUpdatesIsRecordingState() = runTest {
    // Start recording first
    viewModel.onMicButtonClicked(permissionGranted = true)

    // Now stop recording
    viewModel.onMicButtonClicked(permissionGranted = true)

    // Check if recording has stopped
    assertFalse(viewModel.isRecording.value)
  }

  @Test
  fun testNoRecordingActionWhenPermissionNotGranted() = runTest {
    // Simulate microphone permission is not granted
    viewModel.onMicButtonClicked(permissionGranted = false)

    // Check that recording state is still false (no recording should start)
    assertFalse(viewModel.isRecording.value)
  }
}
