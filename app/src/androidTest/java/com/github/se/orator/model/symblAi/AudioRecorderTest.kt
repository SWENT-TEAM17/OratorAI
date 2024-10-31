package com.github.se.orator.model.symblAi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class AudioRecorderTest {

  private lateinit var context: Context
  private lateinit var audioRecorder: AudioRecorder
  private lateinit var recordingListener: AudioRecorder.RecordingListener

  @Before
  fun setUp() {
    context = mock(Context::class.java)
    recordingListener = mock(AudioRecorder.RecordingListener::class.java)
    audioRecorder = AudioRecorder(context)
    audioRecorder.setRecordingListener(recordingListener)
  }

  @Test(expected = SecurityException::class)
  fun testStartRecordingWithoutPermissionThrowsSecurityException() {
    // Simulate the scenario where the audio recording permission is not granted
    `when`(ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO))
        .thenReturn(PackageManager.PERMISSION_DENIED)

    audioRecorder.startRecording()
  }
}
