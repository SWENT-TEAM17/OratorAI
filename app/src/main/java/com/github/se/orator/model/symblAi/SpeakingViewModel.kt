package com.github.se.orator.model.symblAi

import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.speaking.AnalysisData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class SpeakingViewModel(
    private val repository: SpeakingRepository,
    private val apiLinkViewModel: ApiLinkViewModel
) : ViewModel() {
  /** The analysis data collected. It is not final as the user can still re-record another audio. */
  private val _offlineAnalysisData = MutableStateFlow<AnalysisData?>(null)
  val offlineAnalysisData: StateFlow<AnalysisData?> = _offlineAnalysisData.asStateFlow()


  /** The analysis data collected. It is not final as the user can still re-record another audio. */
  private val _analysisData = MutableStateFlow<AnalysisData?>(null)
  val analysisData: StateFlow<AnalysisData?> = _analysisData.asStateFlow()

  /** The result of the analysis of the user's speech. */
  val analysisState: StateFlow<SpeakingRepository.AnalysisState> = repository.analysisState

  /** The error that occurred during processing of the user's speech. */
  private val _analysisError = MutableStateFlow(SpeakingError.NO_ERROR)
  val analysisError = _analysisError.asStateFlow()

  private val _isRecording = MutableStateFlow(false)

  /** True if the user is currently recording their speech, false otherwise. */
  val isRecording: StateFlow<Boolean> = _isRecording

  /** To be called when the speaking screen is closed or the "Done" button is pressed. */
  fun endAndSave() {
    if (_isRecording.value) {
      repository.stopRecording() // Ensure the recording stops
      _isRecording.value = false
    }
    if (_analysisData.value != null) {
      apiLinkViewModel.updateAnalysisData(_analysisData.value!!)
    }
    repository.resetRecorder()
    _analysisData.value = null
  }

  fun getTranscript(audioFile: File) {
    repository.getTranscript(
      audioFile,
      onSuccess = {ad -> _offlineAnalysisData.value = ad},
      onFailure = { error -> _analysisError.value = error }
    )
    Log.d("cute", "smile")
  }

  // Function to handle microphone button click
  fun onMicButtonClicked(permissionGranted: Boolean) {
    if (permissionGranted) {
      if (isRecording.value) {
        repository.stopRecording()
        _isRecording.value = false
      } else {
        repository.setupAnalysisResultsUsage(
            onSuccess = { ad -> _analysisData.value = ad },
            onFailure = { error -> _analysisError.value = error }
        )
        repository.startRecording()
        _isRecording.value = true
      }
    } else {
      Log.e("SpeakingViewModel", "Microphone permission not granted.")
    }
  }
}
