package com.github.se.orator.model.symblAi

import android.content.Context
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeakingRepositoryRecord(context: Context, isOffline: Boolean = false) : SpeakingRepository {

  private val audioRecorder = AudioRecorder(context, isOffline)
  private val symblApiClient = SymblApiClient(context)

  // MutableStateFlow to hold the processing state
  private val _analysisState = MutableStateFlow(SpeakingRepository.AnalysisState.IDLE)
  override val analysisState: StateFlow<SpeakingRepository.AnalysisState> = _analysisState

  // Functions to start and stop recording
  override fun startRecording() {
    _analysisState.value = SpeakingRepository.AnalysisState.RECORDING
    audioRecorder.startRecording()
  }

  override fun stopRecording() {
    audioRecorder.stopRecording()
  }

  /**
   * Sets up how the analysis data will be used after the recording is finished.
   *
   * @param onSuccess Dictating what to do with data if the analysis goes well
   * @param onFailure Dictating what to do with data if the analysis fails
   */
  override fun setupAnalysisResultsUsage(
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {
    audioRecorder.setRecordingListener(
        object : AudioRecorder.RecordingListener {
          override fun onRecordingFinished(audioFile: File) {
            _analysisState.value = SpeakingRepository.AnalysisState.PROCESSING
            symblApiClient.getTranscription(
                audioFile,
                { ad ->
                  onSuccess(ad)
                  _analysisState.value = SpeakingRepository.AnalysisState.FINISHED
                },
                { se ->
                  onFailure(se)
                  _analysisState.value = SpeakingRepository.AnalysisState.FINISHED
                })
          }
        })
  }

  override fun resetRecorder() {
    _analysisState.value = SpeakingRepository.AnalysisState.IDLE
  }
}
