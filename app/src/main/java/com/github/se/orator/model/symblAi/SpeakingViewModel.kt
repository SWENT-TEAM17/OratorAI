package com.github.se.orator.model.symblAi

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctions
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.AnalysisData
import kotlinx.coroutines.Dispatchers
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpeakingViewModel(
    private val repository: SpeakingRepository,
    private val apiLinkViewModel: ApiLinkViewModel,
    private val userProfileViewModel: UserProfileViewModel
) : ViewModel() {
  /** The analysis data collected. It is not final as the user can still re-record another audio. */
  private val _offlineAnalysisData = MutableStateFlow<AnalysisData?>(null)
  val offlineAnalysisData: StateFlow<AnalysisData?> = _offlineAnalysisData.asStateFlow()
  val interviewPromptNb = MutableStateFlow("")

  /** The analysis data collected. It is not final as the user can still re-record another audio. */
  private val _analysisData = MutableStateFlow<AnalysisData?>(null)
  val analysisData: StateFlow<AnalysisData?> = _analysisData.asStateFlow()

  /** The result of the analysis of the user's speech. */
  val analysisState: StateFlow<SpeakingRepository.AnalysisState> = repository.analysisState

  /** The error that occurred during processing of the user's speech. */
  private val _analysisError = MutableStateFlow(SpeakingError.NO_ERROR)
  val analysisError = _analysisError.asStateFlow()

  private val _isRecording = MutableStateFlow(false)

  private val _isTranscribing = MutableStateFlow(false)
  val isTranscribing = _isTranscribing.asStateFlow()

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
  // Suspend function to handle transcript fetching
  suspend fun getTranscript(audioFile: File, offlinePromptsFunctions: OfflinePromptsFunctionsInterface) {
    _isTranscribing.value = true

    // Suspend until the transcript is available
    withContext(Dispatchers.IO) {
      repository.getTranscript(
        audioFile,
        onSuccess = { ad -> _offlineAnalysisData.value = ad },
        onFailure = { error -> _analysisError.value = error
        offlinePromptsFunctions.clearDisplayText()}
      )
    }

    Log.d("in speaking view model", "get transcript for offline mode has been called successfully")
    repository.startRecording()
    repository.stopRecording()

    // Suspend until _offlineAnalysisData.value is not null
    _offlineAnalysisData.first { it != null }
    _isTranscribing.value = false
  }

  fun getTranscriptAndGetGPTResponse(audioFile: File, prompts: Map<String, String>?, viewModel: ChatViewModel, context: Context, offlinePromptsFunctions: OfflinePromptsFunctionsInterface) {
    // Launch a coroutine
    viewModelScope.launch {
      // Wait for the transcript
      getTranscript(audioFile, offlinePromptsFunctions)
      Log.d("finished transcript", "finished transcript of file: ${_offlineAnalysisData.value?.transcription}")
      val ID = prompts?.get("ID") ?: "00000000"
      _offlineAnalysisData.value?.transcription?.removePrefix("You said:")?.let {
        viewModel.resetResponse()
        viewModel.offlineRequest(
          it.trim(),
          prompts?.get("targetCompany") ?: "Apple",
          prompts?.get("jobPosition") ?: "engineer",
          ID,
          context
        )
      }
    }
  }

  // Function to handle microphone button click
  fun onMicButtonClicked(permissionGranted: Boolean, audioFile: File) {
    if (permissionGranted) {
      if (isRecording.value) {
        repository.stopRecording()
        _isRecording.value = false
      } else {
        repository.setupAnalysisResultsUsage(
            onSuccess = { ad ->
              _analysisData.value = ad
              userProfileViewModel.addNewestData(ad)
              userProfileViewModel.updateMetricMean()
            },
            onFailure = { error -> _analysisError.value = error })
        repository.startRecording(audioFile)
        _isRecording.value = true
      }
    } else {
      Log.e("SpeakingViewModel", "Microphone permission not granted.")
    }
  }
}
