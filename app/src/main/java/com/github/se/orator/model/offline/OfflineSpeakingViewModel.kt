package com.github.se.orator.ui.speaking

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import java.io.File

class OfflineSpeakingViewModel(private val context: Context) : ViewModel() {

  private var mediaRecorder: MediaRecorder? = null
  private var mediaPlayer: MediaPlayer? = null
  private var audioFile: File? = null

  // Start recording and save to a local file
  fun startRecording() {
    audioFile = File(context.externalCacheDir, "offline_recording.3gp")
    mediaRecorder =
        MediaRecorder().apply {
          setAudioSource(MediaRecorder.AudioSource.MIC)
          setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
          setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
          setOutputFile(audioFile?.absolutePath)
          prepare()
          start()
        }
  }

  // Stop recording
  fun stopRecording() {
    mediaRecorder?.apply {
      stop()
      release()
    }
    mediaRecorder = null
  }

  // Play the recorded audio
  fun playRecording() {
    mediaPlayer =
        MediaPlayer().apply {
          setDataSource(audioFile?.absolutePath)
          prepare()
          start()
        }
  }

  // Stop playback
  fun stopPlayback() {
    mediaPlayer?.release()
    mediaPlayer = null
  }

  override fun onCleared() {
    super.onCleared()
    stopRecording()
    stopPlayback()
  }
}
