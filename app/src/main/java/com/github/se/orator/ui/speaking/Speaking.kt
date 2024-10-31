package com.github.se.orator.ui.speaking

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.se.orator.model.symblAi.SpeakingViewModel

/**
 * The SpeakingScreen composable is a composable screen that displays the speaking screen.
 *
 * @param viewModel The view model for the speaking screen.
 * @param navController The navigation controller.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun SpeakingScreen(viewModel: SpeakingViewModel, navController: NavHostController) {

  // State variables
  val isRecording by viewModel.isRecording.collectAsState()
  val isProcessing by viewModel.isProcessing.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()
  val transcribedText by viewModel.transcribedText.collectAsState()
  // val sentimentResult by viewModel.sentimentResult.collectAsState()
  val fillersResult by viewModel.fillersResult.collectAsState()

  val analysisData by viewModel.getLinkViewModel().transcribedText.collectAsState()

  LaunchedEffect(analysisData) {
    if (analysisData != null) {
      navController.previousBackStackEntry
          ?.savedStateHandle
          ?.set("transcribedText", analysisData!!.transcription)
      navController.popBackStack()
    }
  }

  // Permission handling
  var permissionGranted by remember { mutableStateOf(false) }
  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted -> permissionGranted = isGranted })

  LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }

  // UI Components
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Animated recording indicator
        val infiniteTransition = rememberInfiniteTransition()

        // Animation for pulsing effect during recording
        val scale by
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.5f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse),
                label = "")

        // Microphone button with animation
        Button(
            onClick = { viewModel.onMicButtonClicked(permissionGranted) },
            modifier = Modifier.size(80.dp).scale(if (isRecording) scale else 1f),
            contentPadding = PaddingValues(0.dp)) {
              Icon(
                  imageVector = if (isRecording) Icons.Filled.Mic else Icons.Filled.MicOff,
                  contentDescription = if (isRecording) "Stop recording" else "Start recording",
                  modifier = Modifier.size(48.dp))
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Display feedback messages
        val feedbackMessage =
            when {
              isRecording -> "Recording..."
              isProcessing -> "Processing..."
              errorMessage != null -> "Error: $errorMessage"
              else -> "Tap the mic to start recording."
            }
        Text(feedbackMessage)

        Spacer(modifier = Modifier.height(16.dp))

        // Display transcribed text
        if (analysisData != null) {
          Text("Transcribed Text: ${analysisData!!.transcription}")
          Spacer(modifier = Modifier.height(16.dp))

          // Display sentiment analysis result
          Text("Sentiment Analysis: ${analysisData!!.sentimentScore}")
          Spacer(modifier = Modifier.height(16.dp))

          // Display filler words result
          if (fillersResult != null) {
            Text("Filler Words: $fillersResult")
          }
        }
      }
}
