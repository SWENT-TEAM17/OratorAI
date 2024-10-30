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
  private val isProcessing_ = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> = isProcessing_

  // MutableStateFlow to hold errors
  private val errorMessage_ = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = errorMessage_

  // Listeners for audio recording and Symbl API processing

  // MutableStateFlows for results
  private val _transcribedText = MutableStateFlow<String?>(null)
  val transcribedText: StateFlow<String?> = _transcribedText

  private val _sentimentResult = MutableStateFlow<String?>(null)
  val sentimentResult: StateFlow<String?> = _sentimentResult

  private val _fillersResult = MutableStateFlow<String?>(null)
  val fillersResult: StateFlow<String?> = _fillersResult

  // Functions to start and stop recording
  fun startRecording() {
    isProcessing_.value = false
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
  fun setupAnalysisResultsUsage(onSuccess: (AnalysisData) -> Unit, onFailure: (Exception) -> Unit) {
    audioRecorder.setRecordingListener(
        object : AudioRecorder.RecordingListener {
          override fun onRecordingFinished(audioFile: File) {
            isProcessing_.value = true
            symblApiClient.getTranscription(audioFile, onSuccess, onFailure)
          }
        })
  }
}
