package com.github.se.orator.ui.speaking

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import kotlinx.coroutines.delay
import kotlin.math.min

@SuppressLint("SuspiciousIndentation", "StateFlowValueCalledInComposition", "MissingPermission")
@Composable
fun SpeakingScreen(
    navigationActions: NavigationActions,
    viewModel: SpeakingViewModel,
    apiLinkViewModel: ApiLinkViewModel,
) {
    // State variables
    val analysisState = viewModel.analysisState.collectAsState()
    val analysisData by viewModel.analysisData.collectAsState()

    val textColor = MaterialTheme.colorScheme.onBackground

    // Permission handling
    val permissionGranted = remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted -> permissionGranted.value = isGranted }
        )

    DisposableEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        onDispose { viewModel.endAndSave() }
    }

    // State for amplitudes
    val amplitudes = remember { mutableStateListOf<Float>() }
    handleAudioRecording(analysisState, permissionGranted, amplitudes)

    // State to hold the current tip
    var currentTip by remember { mutableStateOf<String?>(null) }

    // When entering PROCESSING state, pick a random tip
    LaunchedEffect(analysisState.value) {
        if (analysisState.value == SpeakingRepository.AnalysisState.PROCESSING) {
            val tips = getTipsForModule(apiLinkViewModel)
            currentTip = tips.random()
        } else {
            // Once we leave PROCESSING, clear the tip
            if (analysisState.value != SpeakingRepository.AnalysisState.PROCESSING) {
                currentTip = null
            }
        }
    }

    // Add a progress value for the bar
    var progress by remember { mutableStateOf(0f) }

    // This handles the slow increment during PROCESSING
    LaunchedEffect(analysisState.value) {
        if (analysisState.value == SpeakingRepository.AnalysisState.PROCESSING) {
            // Reset and start incrementing the progress
            progress = 0f
            // While in PROCESSING, gradually increase the progress
            while (analysisState.value == SpeakingRepository.AnalysisState.PROCESSING && progress < 1f) {
                progress += 0.01f
                delay(200L) // Increment every 200ms
            }
        }
    }

    // This handles the quick fill once processing ends
    LaunchedEffect(analysisState.value) {
        if (analysisState.value != SpeakingRepository.AnalysisState.PROCESSING && currentTip != null) {
            // Processing finished, now fill the bar quickly from current progress to 1.0
            // but not too fast, so user can see it:
            while (progress < 1f) {
                progress += 0.02f
                delay(100L)
            }
            // Once fully filled, navigate back
            viewModel.endAndSave()
            navigationActions.goBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("main_box")) {
        // UI Components
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimensions.paddingMedium)
                .testTag("ui_column"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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
                Spacer(
                    modifier = Modifier.height(AppDimensions.paddingMedium).testTag("transcript")
                )

                // Display sentiment analysis result
                Text("Sentiment Analysis: ${analysisData!!.sentimentScore}", color = textColor)
                Spacer(
                    modifier = Modifier.height(AppDimensions.paddingMedium)
                        .testTag("sentiment_analysis")
                )
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
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border =
                    BorderStroke(
                        width = AppDimensions.borderStrokeWidth,
                        color = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text("Back", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Overlay if we are processing (or just finished and doing the final fill)
        if (currentTip != null && (analysisState.value == SpeakingRepository.AnalysisState.PROCESSING || progress < 1f)) {
            // Semi-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            ) {
                // Card with the tip
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(AppDimensions.paddingMedium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.testTag("tips_card"),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        elevation = AppDimensions.elevationSmall
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(AppDimensions.paddingMedium)
                                .widthIn(min = 200.dp, max = 300.dp)
                                .testTag("tips_container"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Processing your speech...",
                                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.padding(bottom = AppDimensions.paddingSmall)
                            )
                            Text(
                                text = currentTip!!,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.padding(AppDimensions.paddingSmall)
                            )
                            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

                            // LinearProgressIndicator showing current progress
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth().testTag("progress_bar"),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                        }
                    }
                }
            }
        }
    }
}

/**
 * Function to get tips based on the current practice context.
 *
 * @param apiLinkViewModel The view model for the API link.
 * @return A list of tips based on the current practice context.
 */
fun getTipsForModule(apiLinkViewModel: ApiLinkViewModel): List<String> {
    // Example tips for each module
    val interviewTips = listOf(
        "Avoid filler words like 'um' or 'uh' to sound more confident.",
        "Maintain a steady pace and organize your thoughts before speaking.",
        "Research the company's culture and align your answers accordingly.",
        "Highlight both technical and soft skills relevant to the role.",
        "Demonstrate problem-solving abilities with concrete examples."
    )

    val publicSpeakingTips = listOf(
        "Engage with your audience by varying your tone and volume.",
        "Use a clear structure: introduction, main points, and conclusion.",
        "Incorporate storytelling to make your speech memorable.",
        "Practice your pacing to avoid rushing through key points.",
        "Anticipate audience questions and prepare responses."
    )

    val salesPitchTips = listOf(
        "Emphasize unique selling points that differentiate you from competitors.",
        "Address potential objections head-on and confidently.",
        "Focus on the client's needs rather than just product features.",
        "Use concrete metrics and case studies to demonstrate value.",
        "Show adaptability if the client raises unexpected concerns."
    )

    val practiceContext = apiLinkViewModel.practiceContext.value

    return when (practiceContext) {
        is InterviewContext -> interviewTips
        is PublicSpeakingContext -> publicSpeakingTips
        is SalesPitchContext -> salesPitchTips
        else -> {
            // If no context or a different one is set, return a generic tip list
            listOf("Stay calm, be clear, and remember your main goals!")
        }
    }
}
