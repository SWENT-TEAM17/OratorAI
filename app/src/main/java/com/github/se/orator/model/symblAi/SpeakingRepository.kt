package com.github.se.orator.model.symblAi

import android.content.Context
import android.util.Log
import com.github.se.orator.model.symblAi.AudioRecorder
import com.github.se.orator.model.symblAi.SymblApiClient
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
    init {
        // Listener for Symbl API processing
        symblApiClient.setListener(object : SymblApiClient.SymblListener {
            override fun onProcessingComplete(
                transcribedTextResult: String,
                sentimentResultData: String,
                fillersResultData: String
            ) {
                // Update the StateFlows with the results
                _transcribedText.value = transcribedTextResult
                _sentimentResult.value = sentimentResultData
                _fillersResult.value = fillersResultData
                isProcessing_.value = false
            }

            override fun onError(message: String) {
                errorMessage_.value = message
                isProcessing_.value = false
                Log.e("SymblApiClient", message)
            }
        })

        // Listener for audio recording completion
        audioRecorder.setRecordingListener(object : AudioRecorder.RecordingListener {
            override fun onRecordingFinished(audioFile: File) {
                // Start processing the audio file
                isProcessing_.value = true
                symblApiClient.sendAudioToSymbl(audioFile)
            }
        })
    }

    // MutableStateFlows for results
    private val _transcribedText = MutableStateFlow<String?>(null)
    val transcribedText: StateFlow<String?> = _transcribedText

    private val _sentimentResult = MutableStateFlow<String?>(null)
    val sentimentResult: StateFlow<String?> = _sentimentResult

    private val _fillersResult = MutableStateFlow<String?>(null)
    val fillersResult: StateFlow<String?> = _fillersResult

    // Functions to start and stop recording
    fun startRecording() {
        audioRecorder.startRecording()
    }

    fun stopRecording() {
        audioRecorder.stopRecording()
    }
}