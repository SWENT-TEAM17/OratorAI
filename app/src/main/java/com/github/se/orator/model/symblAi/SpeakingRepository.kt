package com.github.se.orator.model.symblAi

import android.content.Context
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeakingRepository(private val context: Context) {

  private val audioRecorder = AudioRecorder(context)
  private val symblApiClient = SymblApiClient(context)

  // MutableStateFlow to hold the processing state
  private val _analysisState = MutableStateFlow(AnalysisState.IDLE)
  val analysisState: StateFlow<AnalysisState> = _analysisState

  // Functions to start and stop recording
  fun startRecording() {
    _analysisState.value = AnalysisState.RECORDING
    audioRecorder.startRecording()
  }

  fun stopRecording() {
    audioRecorder.stopRecording()
  }

  /**
   * Sets up how the analysis data will be used after the recording is finished.
   *
   * @param onSuccess Dictating what to do with data if the analysis goes well
   * @param onFailure Dictating what to do with data if the analysis fails
   */
  fun setupAnalysisResultsUsage(
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    audioRecorder.setRecordingListener(
        object : AudioRecorder.RecordingListener {
          override fun onRecordingFinished(audioFile: File) {
            _analysisState.value = AnalysisState.PROCESSING
            symblApiClient.getTranscription(
                audioFile,
                { ad ->
                  onSuccess(ad)
                  _analysisState.value = AnalysisState.FINISHED
                },
                { se ->
                  onFailure(se)
                  _analysisState.value = AnalysisState.FINISHED
                })
          }
        })
  }

  fun resetRecorder() {
    _analysisState.value = AnalysisState.IDLE
  }
}

enum class AnalysisState {
  IDLE,
  RECORDING,
  PROCESSING,
  FINISHED
}
