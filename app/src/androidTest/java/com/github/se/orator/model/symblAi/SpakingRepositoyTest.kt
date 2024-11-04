package com.github.se.orator.model.symblAi

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.symblAi.SpeakingError
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpeakingRepositoryTest {

    private lateinit var context: Context
    private lateinit var speakingRepository: SpeakingRepository
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @Before
    fun setUp() {
        // Obtain the application context
        context = ApplicationProvider.getApplicationContext()

        // Initialize SpeakingRepository with the context
        speakingRepository = SpeakingRepository(context)
    }

    @Test
    fun initial_state_should_be_IDLE() {
        // Assert that the initial state is IDLE
        assertEquals(AnalysisState.IDLE, speakingRepository.analysisState.value)
    }

    @Test
    fun startRecording_should_update_state_to_RECORDING() {
        // Act
        speakingRepository.startRecording()

        // Assert
        assertEquals(AnalysisState.RECORDING, speakingRepository.analysisState.value)
    }

    @Test
    fun resetRecorder_should_set_state_back_to_IDLE() {
        // Arrange
        speakingRepository.startRecording()
        assertEquals(AnalysisState.RECORDING, speakingRepository.analysisState.value)

        // Act
        speakingRepository.resetRecorder()

        // Assert
        assertEquals(AnalysisState.IDLE, speakingRepository.analysisState.value)
    }
}
