package com.github.se.orator.model.symblAi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeakingViewModel(private val repository: SpeakingRepository) : ViewModel() {

  // Expose StateFlows from the repository
  val isProcessing: StateFlow<Boolean> = repository.isProcessing
  val errorMessage: StateFlow<String?> = repository.errorMessage
  val transcribedText: StateFlow<String?> = repository.transcribedText
  val sentimentResult: StateFlow<String?> = repository.sentimentResult
  val fillersResult: StateFlow<String?> = repository.fillersResult

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
        repository.startRecording()
        _isRecording.value = true
      }
    } else {
      Log.e("SpeakingViewModel", "Microphone permission not granted.")
    }
  }

  // Factory for creating SpeakingViewModel with the repository
  class SpeakingViewModelFactory(private val repository: SpeakingRepository) :
      ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(SpeakingViewModel::class.java)) {
        return SpeakingViewModel(repository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
