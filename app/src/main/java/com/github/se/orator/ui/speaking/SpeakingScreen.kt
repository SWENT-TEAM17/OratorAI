package com.github.se.orator.ui.speaking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.se.orator.model.symblAi.AudioRecorder
import com.github.se.orator.model.symblAi.SymblApiClient
import java.io.File

@Composable
fun SpeakingScreen() {
    val context = LocalContext.current

    // State variables
    var isRecording by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("Tap the mic to start recording.") }
    var transcribedText by remember { mutableStateOf("Transcribed Text: ") }
    var sentimentResult by remember { mutableStateOf("Sentiment Analysis: ") }
    var fillersResult by remember { mutableStateOf("Filler Words: ") }

    // Instantiate AudioRecorder and SymblApiClient
    val audioRecorder = remember { AudioRecorder(context) }
    val symblApiClient = remember { SymblApiClient(context) }

    // Set listeners
    val symblListener = object : SymblApiClient.SymblListener {
        override fun onProcessingComplete(
            transcribedTextResult: String,
            sentimentResultData: String,
            fillersResultData: String
        ) {
            // Update UI with the results
            transcribedText = transcribedTextResult
            sentimentResult = sentimentResultData
            fillersResult = fillersResultData
            feedbackMessage = "Processing complete."
        }

        override fun onError(message: String) {
            feedbackMessage = message
            Log.e("SymblApiClient", message)
        }
    }

    val recordingListener = object : AudioRecorder.RecordingListener {
        override fun onRecordingFinished(audioFile: File) {
            // Send audio file to SymblApiClient
            feedbackMessage = "Recording finished, sending audio for processing..."
            symblApiClient.sendAudioToSymbl(audioFile)
        }
    }

    // Set the listeners
    LaunchedEffect(Unit) {
        symblApiClient.setListener(symblListener)
        audioRecorder.setRecordingListener(recordingListener)
    }

    // Permission handling
    val permissionGranted = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            permissionGranted.value = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                permissionGranted.value = true
            }
        } else {
            permissionGranted.value = true
        }
    }

    // UI Components
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated recording indicator
        val infiniteTransition = rememberInfiniteTransition()

        // Animation for pulsing effect during recording
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        // Microphone button with animation
        Button(
            onClick = {
                if (permissionGranted.value) {
                    if (isRecording) {
                        audioRecorder.stopRecording()
                        feedbackMessage = "Recording stopped, processing..."
                    } else {
                        audioRecorder.startRecording()
                        feedbackMessage = "Recording started..."
                    }
                    isRecording = !isRecording
                } else {
                    feedbackMessage = "Microphone permission not granted."
                }
            },
            modifier = Modifier
                .size(80.dp)
                .scale(if (isRecording) scale else 1f),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.Mic else Icons.Filled.MicOff,
                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display feedback messages
        Text(feedbackMessage)

        Spacer(modifier = Modifier.height(16.dp))

        // Display transcribed text
        Text(transcribedText)

        Spacer(modifier = Modifier.height(16.dp))

        // Display sentiment analysis result
        Text(sentimentResult)

        Spacer(modifier = Modifier.height(16.dp))

        // Display filler words result
        Text(fillersResult)
    }
}
