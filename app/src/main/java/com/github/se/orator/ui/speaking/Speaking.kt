package com.github.se.orator.ui.speaking

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions

/**
 * The SpeakingScreen composable displays the speaking screen.
 *
 * @param viewModel The view model for the speaking screen.
 * @param navigationActions The NavigationActions instance to navigate between screens.
 */
@SuppressLint("SuspiciousIndentation", "StateFlowValueCalledInComposition", "MissingPermission")
@Composable
fun SpeakingScreen(navigationActions: NavigationActions, viewModel: SpeakingViewModel) {

  // State variables
  val analysisState = viewModel.analysisState.collectAsState()
  val analysisData by viewModel.analysisData.collectAsState()

  val textColor = MaterialTheme.colorScheme.onBackground

  // Permission handling
  val permissionGranted = remember { mutableStateOf(false) }

  val permissionLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission(),
          onResult = { isGranted -> permissionGranted.value = isGranted })

  DisposableEffect(Unit) {
    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    onDispose { viewModel.endAndSave() }
  }

  // State for amplitudes
  val amplitudes = remember { mutableStateListOf<Float>() }
  handleAudioRecording(analysisState, permissionGranted, amplitudes)

  // UI Components
  Column(
      modifier = Modifier.fillMaxSize().padding(AppDimensions.paddingMedium).testTag("ui_column"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {

        // Microphone button with animation
        MicrophoneButton(viewModel, analysisState, permissionGranted, LocalContext.current)

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

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
        Text(feedbackMessage, modifier = Modifier.testTag("mic_text"), color = textColor)

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

        // Add the AudioVisualizer when recording
        if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING) {
          AudioVisualizer(amplitudes = amplitudes)
          Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
        }

        // Display transcribed text
        if (analysisData != null) {
          Text("Transcribed Text: ${analysisData!!.transcription}", color = textColor)
          Spacer(modifier = Modifier.height(AppDimensions.paddingMedium).testTag("transcript"))

          // Display sentiment analysis result
          Text("Sentiment Analysis: ${analysisData!!.sentimentScore}", color = textColor)
          Spacer(
              modifier = Modifier.height(AppDimensions.paddingMedium).testTag("sentiment_analysis"))
        }

        Row {
          Button(
              onClick = {
                viewModel.endAndSave() // end the recording
                navigationActions.goBack()
              },
              modifier = Modifier.testTag("back_button"),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                      contentColor = MaterialTheme.colorScheme.primary),
              border =
                  BorderStroke(
                      width = AppDimensions.borderStrokeWidth,
                      color = MaterialTheme.colorScheme.outline)) {
                Text("Back", color = MaterialTheme.colorScheme.primary)
              }
        }
      }
}
