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
import java.io.File
import kotlinx.coroutines.Dispatchers
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
  suspend fun getTranscript(
      audioFile: File,
      offlinePromptsFunctions: OfflinePromptsFunctionsInterface = OfflinePromptsFunctions(),
      id: String,
      context: Context
  ) {
    _isTranscribing.value = true

    // Suspend until the transcript is available
    withContext(Dispatchers.IO) {
      repository.getTranscript(
          audioFile,
          onSuccess = {
              ad -> _offlineAnalysisData.value = ad
              val transcription: String = _offlineAnalysisData.value?.transcription.toString()
              offlinePromptsFunctions.changePromptStatus(id, context, "transcription", transcription)
              offlinePromptsFunctions.changePromptStatus(id, context, "GPTresponse", "1")
                      },
          onFailure = { error ->
            _analysisError.value = error
            offlinePromptsFunctions.clearDisplayText()
              offlinePromptsFunctions.changePromptStatus(id, context, "transcribed", "0")

          })
    }

    Log.d("in speaking view model", "get transcript for offline mode has been called successfully")
    repository.startRecording()
    repository.stopRecording()

    // Suspend until _offlineAnalysisData.value is not null
    _offlineAnalysisData.first { it != null }
    _isTranscribing.value = false
  }

  /**
   * Function that allows to get transcript and subsequently get a gpt response It is in this view
   * model because it is necessary to modify private variables in this view model for this to work
   *
   * @param audioFile : The audio file to transcript - corresponds to the offline mode recording
   * @param prompts : String -> String mapping that maps the interviews to the companies, target job position, and interview ID
   * @param viewModel : chat view model that is needed to get the GPT response
   * @param offlinePromptsFunctions : offline prompt functions and variables needed to write to files
   */
  fun getTranscriptAndGetGPTResponse(
      audioFile: File,
      prompts: Map<String, String>?,
      viewModel: ChatViewModel,
      context: Context,
      offlinePromptsFunctions: OfflinePromptsFunctionsInterface
  ) {
    // Launch a coroutine to have this run in the background and parallelize
    viewModelScope.launch {
        val ID = prompts?.get("ID") ?: "00000000"
      // Wait for the transcript
        val notTranscribing = offlinePromptsFunctions.changePromptStatus(ID, context, "transcribed", "1")
        if (notTranscribing) {
            getTranscript(audioFile, offlinePromptsFunctions, ID, context)
            Log.d(
                "finished transcript",
                "finished transcript of file: ${_offlineAnalysisData.value?.transcription}")

            // wait for GPT response to finish loading
            viewModel.isLoading.first { !it }

            
            // if the transcription did not fail then request a prompt for feedback
            if (prompts?.get("GPTresponse") ?: "0" == "1") {
                val transcription = prompts?.get("transcription") ?: ""
                if (transcription != "") {
                    viewModel.offlineRequest(
                        transcription.trim(),
                        prompts?.get("targetCompany") ?: "Apple",
                        prompts?.get("jobPosition") ?: "engineer",
                        ID,
                        context)
                }
//                _offlineAnalysisData.value?.transcription?.removePrefix("You said:")?.let {
//
//                }
            }
            else {
                Log.d("error", "transcription might have failed")
                offlinePromptsFunctions.changePromptStatus(ID, context, "transcribed", "0")
            }

        }
        else {
            Log.d("in speaking view model", "already transcribing!!")
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
