package com.github.se.orator.model.symblAi

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SpeakingRepositoryTest {

  private lateinit var context: Context
  private lateinit var speakingRepository: SpeakingRepository

  @Before
  fun setUp() {
    // Mock the Android Context
    context = Mockito.mock(Context::class.java)

    // Initialize SpeakingRepository with the mocked context
    speakingRepository = SpeakingRepository(context)
  }

  @Test
  fun `initial state should be IDLE`() {
    // Assert that the initial state is IDLE
    assertEquals(AnalysisState.IDLE, speakingRepository.analysisState.value)
  }

  @Test
  fun `startRecording should update state to RECORDING`() {
    // Act
    speakingRepository.startRecording()

    // Assert
    assertEquals(AnalysisState.RECORDING, speakingRepository.analysisState.value)
  }

  @Test
  fun `resetRecorder should set state back to IDLE`() {
    // Arrange
    speakingRepository.startRecording()
    assertEquals(AnalysisState.RECORDING, speakingRepository.analysisState.value)

    // Act
    speakingRepository.resetRecorder()

    // Assert
    assertEquals(AnalysisState.IDLE, speakingRepository.analysisState.value)
  }
}
