package com.github.se.orator.model.symblAi

import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeakingViewModel(
    private val repository: SpeakingRepository,
    private val apiLinkViewModel: ApiLinkViewModel = ApiLinkViewModel()
) : ViewModel() {

  // Expose StateFlows from the repository
  val isProcessing: StateFlow<Boolean> = repository.isProcessing
  val errorMessage: StateFlow<String?> = repository.errorMessage
  val transcribedText: StateFlow<String?> = repository.transcribedText
  val sentimentResult: StateFlow<String?> = repository.sentimentResult
  val fillersResult: StateFlow<String?> = repository.fillersResult

  /**
   * Getter of the ApiLinkViewModel for structures needing the analysis data.
   *
   * @return the ApiLinkViewModel
   */
  fun getLinkViewModel(): ApiLinkViewModel {
    return apiLinkViewModel
  }

  // MutableStateFlow for recording state
  private val _isRecording = MutableStateFlow(false)
  val isRecording: StateFlow<Boolean> = _isRecording

  // Function to handle microphone button click
  fun onMicButtonClicked(permissionGranted: Boolean) {
    if (permissionGranted) {
      if (isRecording.value) {
        repository.stopRecording()
        _isRecording.value = false
      } else {
        repository.setupAnalysisResultsUsage(
            onSuccess = { analysisData -> apiLinkViewModel.updateAnalysisData(analysisData) },
            onFailure = {})
        repository.startRecording()
        _isRecording.value = true
      }
    } else {
      Log.e("SpeakingViewModel", "Microphone permission not granted.")
    }
  }
}
