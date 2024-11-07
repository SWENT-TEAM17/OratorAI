package com.github.se.orator.model.symblAi

import com.github.se.orator.model.speaking.AnalysisData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface SpeakingRepository {
    val analysisState: StateFlow<AnalysisState>

  fun startRecording()

  fun stopRecording()

  fun setupAnalysisResultsUsage(
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  )

  fun resetRecorder()

  enum class AnalysisState {
    IDLE,
    RECORDING,
    PROCESSING,
    FINISHED
  }
}
