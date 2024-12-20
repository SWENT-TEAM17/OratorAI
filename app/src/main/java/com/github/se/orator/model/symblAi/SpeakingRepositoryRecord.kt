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

  private val _fileSaved: MutableStateFlow<Boolean> = MutableStateFlow(false)
  override val fileSaved: StateFlow<Boolean> = _fileSaved

  // Functions to start and stop recording

  // Function to start recording to the default file "audio_record.wav" kept for backwards
  // compatibility
  // with the speaking screens
  override fun startRecording() {
    _analysisState.value = SpeakingRepository.AnalysisState.RECORDING
    audioRecorder.startRecording(File(context.cacheDir, "audio_record.wav"))
  }

  override fun setFileSaved(newVal: Boolean) {
    _fileSaved.value = true
  }

  // This overload of startRecording() allows specifying a custom audio file name.
  // It was introduced to ensure that the recorded audio can be saved under a unique,
  // caller-defined filename, preventing conflicts and issues in playback due to default names.
  override fun startRecording(audioFile: File) {
    _analysisState.value = SpeakingRepository.AnalysisState.RECORDING
    audioRecorder.startRecording(audioFile)
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
