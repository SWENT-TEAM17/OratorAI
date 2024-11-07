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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

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
  val sentimentResult by viewModel.sentimentResult.collectAsState()
  val fillersResult by viewModel.fillersResult.collectAsState()

  LaunchedEffect(transcribedText) {
    if (transcribedText != null) {
      navController.previousBackStackEntry
          ?.savedStateHandle
          ?.set("transcribedText", transcribedText)
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
      modifier =
          Modifier.fillMaxSize()
              .padding(AppDimensions.paddingMedium), // Replaced 16.dp with paddingMedium
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
                Modifier.size(
                        AppDimensions.buttonHeightLarge) // Replaced 80.dp with buttonSizeLarge
                    .scale(if (isRecording) scale else 1f)
                    .testTag("micButton"), // Added testTag for mic button
            contentPadding = PaddingValues(0.dp) // Removed default padding
            ) {
              Icon(
                  imageVector = if (isRecording) Icons.Filled.Mic else Icons.Filled.MicOff,
                  contentDescription = if (isRecording) "Stop recording" else "Start recording",
                  modifier =
                      Modifier.size(AppDimensions.iconSizeLarge)
                          .testTag("IconStopRecording") // Replaced 48.dp with iconSizeLarge
                  )
            }

        Spacer(
            modifier =
                Modifier.height(AppDimensions.paddingMedium)) // Replaced 16.dp with paddingMedium

        // Display feedback messages
        val feedbackMessage =
            when {
              isRecording -> "Recording..."
              isProcessing -> "Processing..."
              errorMessage != null -> "Error: $errorMessage"
              else -> "Tap the mic to start recording."
            }
        Text(
            text = feedbackMessage,
            style = AppTypography.bodyLargeStyle,
            modifier = Modifier.testTag("FeedbackText") // Replaced manual styling
            )

        Spacer(
            modifier =
                Modifier.height(AppDimensions.paddingMedium)) // Replaced 16.dp with paddingMedium

        // Display transcribed text
        if (transcribedText != null) {
          Text(
              text = "Transcribed Text: $transcribedText",
              style = AppTypography.bodyLargeStyle,
              modifier = Modifier.testTag("TranscribedText") // Replaced manual styling
              )
          Spacer(
              modifier =
                  Modifier.height(AppDimensions.paddingMedium)) // Replaced 16.dp with paddingMedium
        }

        // Display sentiment analysis result
        if (sentimentResult != null) {
          Text(
              text = "Sentiment Analysis: $sentimentResult",
              style = AppTypography.bodyLargeStyle,
              modifier = Modifier.testTag("SentimentAnalysisText") // Replaced manual styling
              )
          Spacer(
              modifier =
                  Modifier.height(AppDimensions.paddingMedium)) // Replaced 16.dp with paddingMedium
        }

        // Display filler words result
        if (fillersResult != null) {
          Text(
              text = "Filler Words: $fillersResult",
              style = AppTypography.bodyLargeStyle,
              modifier = Modifier.testTag("FillerWordsText") // Replaced manual styling
              )
        }
      }
}
