package com.github.se.orator.ui.speaking

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions

/**
 * The SpeakingScreen composable is a composable screen that displays the speaking screen.
 *
 * @param viewModel The view model for the speaking screen.
 * @param navigationActions The NavigationActions instance to navigate between screens.
 */
@SuppressLint("SuspiciousIndentation", "StateFlowValueCalledInComposition")
@Composable
fun SpeakingScreen(navigationActions: NavigationActions, viewModel: SpeakingViewModel) {

  // State variables
  val analysisState = viewModel.analysisState.collectAsState()
  // val sentimentResult by viewModel.sentimentResult.collectAsState()
  // val fillersResult by viewModel.fillersResult.collectAsState()

  val analysisData by viewModel.analysisData.collectAsState()

  // Permission handling
  var permissionGranted by remember { mutableStateOf(false) }
  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted -> permissionGranted = isGranted })

  DisposableEffect(Unit) {
    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

    onDispose { viewModel.endAndSave() }
  }

  // UI Components
  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("ui_column"),
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
            modifier =
                Modifier.size(80.dp)
                    .scale(
                        if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING) scale
                        else 1f).testTag("mic_button"),
            contentPadding = PaddingValues(0.dp)) {
              Icon(
                  imageVector =
                      if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING)
                          Icons.Filled.Mic
                      else Icons.Filled.MicOff,
                  contentDescription =
                      if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING)
                          "Stop recording"
                      else "Start recording",
                  modifier = Modifier.size(48.dp))
            }

        Spacer(modifier = Modifier.height(16.dp))

        // Display feedback messages
        val feedbackMessage =
            when (analysisState.value) {
              SpeakingRepository.AnalysisState.RECORDING -> "Recording..."
              SpeakingRepository.AnalysisState.PROCESSING -> "Processing..."
              SpeakingRepository.AnalysisState.IDLE -> "Tap the mic to start recording."
              else ->
                  when (viewModel.analysisError.value) {
                    SpeakingError.NO_ERROR -> "Analysis finished."
                    else -> "Error : ${viewModel.analysisError.value}"
                  }
            }
        Text(feedbackMessage, modifier = Modifier.testTag("mic_text"))

        Spacer(modifier = Modifier.height(16.dp))

        // Display transcribed text
        if (analysisData != null) {
          Text("Transcribed Text: ${analysisData!!.transcription}")
          Spacer(modifier = Modifier.height(16.dp).testTag("transcript"))

          // Display sentiment analysis result
          Text("Sentiment Analysis: ${analysisData!!.sentimentScore}")
          Spacer(modifier = Modifier.height(16.dp).testTag("sentiment_analysis"))

          /*// Display filler words result
          if (fillersResult != null) {
            Text("Filler Words: $fillersResult")
          }*/
        }

        Row {
          Button(
              onClick = { navigationActions.goBack() },
              modifier = Modifier.testTag("back_button")) {
                Text("Back")
              }
        }
      }
}
