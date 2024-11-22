package com.github.se.orator.ui.speaking

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import kotlinx.coroutines.delay

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
      MicrophoneButton(
          viewModel,
          analysisState,
          permissionGranted,
          LocalContext.current)

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
        Text(feedbackMessage, modifier = Modifier.testTag("mic_text"))

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

        // Add the AudioVisualizer when recording
        if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING) {
          AudioVisualizer(amplitudes = amplitudes)
          Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
        }

        // Display transcribed text
        if (analysisData != null) {
          Text("Transcribed Text: ${analysisData!!.transcription}")
          Spacer(modifier = Modifier.height(AppDimensions.paddingMedium).testTag("transcript"))

          // Display sentiment analysis result
          Text("Sentiment Analysis: ${analysisData!!.sentimentScore}")
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
                      containerColor = Color.White,
                      contentColor = MaterialTheme.colorScheme.primary),
              border =
                  BorderStroke(
                      width = AppDimensions.borderStrokeWidth,
                      color = MaterialTheme.colorScheme.primary)) {
                Text("Back")
              }
        }
      }
}
