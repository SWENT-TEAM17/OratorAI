package com.github.se.orator.model.symblAi

import android.content.Context
import android.util.Log
import com.github.se.orator.model.speaking.AnalysisData
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpeakingRepositoryRecord(private val context: Context) : SpeakingRepository {

  private val audioRecorder = AudioRecorder(context)
  private val symblApiClient = SymblApiClient(context)

  // MutableStateFlow to hold the processing state
  private val _analysisState = MutableStateFlow(SpeakingRepository.AnalysisState.IDLE)
  override val analysisState: StateFlow<SpeakingRepository.AnalysisState> = _analysisState

  // Functions to start and stop recording
  override fun startRecording() {
    _analysisState.value = SpeakingRepository.AnalysisState.RECORDING
    audioRecorder.startRecording()
  }

  override fun startRecordingToFile(audioFile: File) {
    audioRecorder.startRecording(audioFile)
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

  override fun getTranscript(
      audioFile: File,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (SpeakingError) -> Unit
  ) {

    CoroutineScope(Dispatchers.IO).launch {
      delay(5_000) // Delay for 10 seconds (10,000 milliseconds)

      // Now proceed with transcription after the delay
      symblApiClient.getTranscription(
          audioFile,
          onSuccess = { analysisData ->
            Log.d("SymblApiClient", "Transcription successful: ${analysisData.transcription}")
            onSuccess(analysisData)
          },
          onFailure = { error ->
            Log.e("SymblApiClient", "Transcription failed: $error")
            onFailure(error)
          })
    }
  }
}
