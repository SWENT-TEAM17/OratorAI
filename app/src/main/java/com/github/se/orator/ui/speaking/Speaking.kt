package com.github.se.orator.ui.speaking

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
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
    var permissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted -> permissionGranted = isGranted })

    DisposableEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        onDispose { viewModel.endAndSave() }
    }

    // State for amplitudes
    val amplitudes = remember { mutableStateListOf<Float>() }

    // Audio recording and amplitude collection
    LaunchedEffect(analysisState.value, permissionGranted) {
        if (permissionGranted && analysisState.value == SpeakingRepository.AnalysisState.RECORDING) {
            val sampleRateInHz = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            val bufferSize = AudioRecord.getMinBufferSize(
                sampleRateInHz,
                channelConfig,
                audioFormat
            )
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                bufferSize
            )
            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize)
            try {
                while (analysisState.value == SpeakingRepository.AnalysisState.RECORDING) {
                    val readSize = audioRecord.read(buffer, 0, bufferSize)
                    if (readSize > 0) {
                        val max = buffer.take(readSize).maxOrNull()?.toFloat() ?: 0f
                        amplitudes.add(max)
                        if (amplitudes.size > 100) {
                            amplitudes.removeFirst()
                        }
                    }
                    delay(16L) // Approximately 60 fps
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        } else {
            amplitudes.clear()
        }
    }

    // UI Components
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.paddingMedium)
            .testTag("ui_column"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )

        // Microphone button with animation
        Button(
            onClick = { viewModel.onMicButtonClicked(permissionGranted) },
            modifier =
            Modifier.size(AppDimensions.buttonSize)
                .scale(
                    if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING) scale
                    else 1f
                )
                .testTag("mic_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = AppShapes.circleShape,
            border = BorderStroke(
                width = AppDimensions.borderStrokeWidth,
                color = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector =
                if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING)
                    Icons.Filled.Mic
                else Icons.Filled.MicOff,
                contentDescription =
                if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING)
                    "Stop recording"
                else "Start recording",
                modifier = Modifier.size(AppDimensions.iconSizeMic),
                tint = MaterialTheme.colorScheme.primary
            )
        }

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
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium).testTag("sentiment_analysis"))
        }

        Row {
            Button(
                onClick = { navigationActions.goBack() },
                modifier = Modifier.testTag("back_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = AppDimensions.borderStrokeWidth,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Back")
            }
        }
    }
}

/**
 * A composable that visualizes audio amplitudes as a waveform.
 *
 * @param amplitudes A list of amplitude values to visualize.
 */
@Composable
fun AudioVisualizer(amplitudes: List<Float>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.visualizerHeight)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / amplitudes.size
        amplitudes.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude / Short.MAX_VALUE) * height
            drawLine(
                color = AppColors.primaryColor,
                start = Offset(x = index * barWidth, y = height / 2 - barHeight / 2),
                end = Offset(x = index * barWidth, y = height / 2 + barHeight / 2),
                strokeWidth = barWidth
            )
        }
    }
}
