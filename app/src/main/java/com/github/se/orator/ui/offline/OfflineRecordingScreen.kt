package com.github.se.orator.ui.offline

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.speaking.OfflineSpeakingViewModel

/**
 * OfflineRecordingScreen is a composable screen for offline mode. It allows the user to record
 * audio locally and play it back.
 *
 * @param navigationActions Provides navigation actions for navigating between screens.
 * @param question The question text selected by the user for offline practice.
 * @param offlineSpeakingViewModel ViewModel for managing audio recording and playback
 *   functionality.
 */
@SuppressLint("MissingPermission")
@Composable
fun OfflineRecordingScreen(
    navigationActions: NavigationActions,
    question: String, // Accept question as a parameter
    offlineSpeakingViewModel: OfflineSpeakingViewModel =
        viewModel(factory = OfflineSpeakingViewModelFactory(LocalContext.current))
) {
  // Local state to track whether recording is in progress
  var isRecording by remember { mutableStateOf(false) }

  // Local state to track if the recording permission has been granted
  var permissionGranted by remember { mutableStateOf(false) }

  // Set up permission launcher for requesting microphone access
  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted -> permissionGranted = isGranted })

  // Request recording permission on initial composition
  LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }

  // Layout for the offline speaking screen
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Display the selected question
        Text("Question: $question", Modifier.padding(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Button to start or stop recording based on the current recording state
        Button(
            onClick = {
              if (permissionGranted) {
                if (isRecording) {
                  // Stop recording if currently recording
                  offlineSpeakingViewModel.stopRecording()
                } else {
                  // Start recording if not currently recording
                  offlineSpeakingViewModel.startRecording()
                }
                // Toggle recording state
                isRecording = !isRecording
              }
            }) {
              Text(if (isRecording) "Stop Recording" else "Start Recording")
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to play the recorded audio, only available if not currently recording
        Button(
            onClick = {
              if (!isRecording) {
                offlineSpeakingViewModel.playRecording()
              }
            }) {
              Text("Play Recording")
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate back to the previous screen
        Button(onClick = { navigationActions.goBack() }) { Text("Back") }
      }
}
