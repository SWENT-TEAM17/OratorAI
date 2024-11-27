package com.github.se.orator.model.symblAi

// AudioRecorder.kt

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(
    private val context: Context,
    private val isOffline: Boolean, // Pass offline state from ViewModel or activity
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) {

  private var audioRecord: AudioRecord? = null
  private var isRecordingAudio = false
  private var audioFile: File? = null

  // Listener to notify when recording is finished
  interface RecordingListener {
    fun onRecordingFinished(audioFile: File)
  }

  private var recordingListener: RecordingListener? = null

  fun setRecordingListener(listener: RecordingListener) {
    recordingListener = listener
  }

  fun startRecording() {
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
        PackageManager.PERMISSION_GRANTED) {
      // Permission not granted
      throw SecurityException("Audio recording permission not granted.")
    }

    audioRecord =
        AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)


    // Save file to the appropriate directory based on offline mode
    val saveDir = if (isOffline) context.filesDir else context.cacheDir
    audioFile = File(saveDir, "audio_record_${System.currentTimeMillis()}.wav") // Recording in WAV format

    audioRecord?.startRecording()
    isRecordingAudio = true

    // Start a new thread to record audio data
    Thread {
          val data = ByteArray(bufferSize)
          val outputStream = ByteArrayOutputStream()

          while (isRecordingAudio) {
            val read = audioRecord?.read(data, 0, data.size) ?: 0
            if (read > 0) {
              outputStream.write(data, 0, read)
            }
          }

          // Save recorded data to WAV file
          val audioData = outputStream.toByteArray()
          saveAsWavFile(audioData, audioFile!!)

          outputStream.close()
          recordingListener?.onRecordingFinished(audioFile!!)
        }
        .start()
  }

  fun stopRecording() {
    isRecordingAudio = false
    audioRecord?.stop()
    audioRecord?.release()
    audioRecord = null
  }

  fun saveAsWavFile(audioData: ByteArray, audioFile: File) {
    val totalDataLen = audioData.size + 36
    val totalAudioLen = audioData.size.toLong()
    val channels = 1
    val byteRate = 16 * sampleRate * channels / 8

    val wavFile = FileOutputStream(audioFile)
    val header = ByteArray(44)

    // RIFF/WAVE header
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    writeInt(header, 4, totalDataLen)
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    writeInt(header, 16, 16) // Sub-chunk size
    writeShort(header, 20, 1.toShort()) // Audio format (1 = PCM)
    writeShort(header, 22, channels.toShort())
    writeInt(header, 24, sampleRate)
    writeInt(header, 28, byteRate)
    writeShort(header, 32, (channels * 16 / 8).toShort()) // Block align
    writeShort(header, 34, 16.toShort()) // Bits per sample
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    writeInt(header, 40, totalAudioLen.toInt())

    wavFile.write(header)
    wavFile.write(audioData)
    wavFile.close()
  }

  // Helper functions to write data to header
  fun writeInt(header: ByteArray, offset: Int, value: Int) {
    header[offset] = (value and 0xff).toByte()
    header[offset + 1] = ((value shr 8) and 0xff).toByte()
    header[offset + 2] = ((value shr 16) and 0xff).toByte()
    header[offset + 3] = ((value shr 24) and 0xff).toByte()
  }

  fun writeShort(header: ByteArray, offset: Int, value: Short) {
    header[offset] = (value.toInt() and 0xff).toByte()
    header[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
  }
}
