package com.github.se.orator.ui.speaking

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
import java.io.File
import kotlinx.coroutines.delay

/**
 * A composable that visualizes audio amplitudes as a waveform.
 *
 * @param amplitudes A list of amplitude values to visualize.
 */
@Composable
fun AudioVisualizer(amplitudes: List<Float>) {
  Canvas(
      modifier =
          Modifier.fillMaxWidth()
              .height(AppDimensions.visualizerHeight)
              .testTag("audio_visualizer")) {
        val width = size.width
        val height = size.height
        val barWidth = width / amplitudes.size
        amplitudes.forEachIndexed { index, amplitude ->
          val barHeight = (amplitude / Short.MAX_VALUE) * height
          drawLine(
              color = AppColors.primaryColor,
              start = Offset(x = index * barWidth, y = height / 2 - barHeight / 2),
              end = Offset(x = index * barWidth, y = height / 2 + barHeight / 2),
              strokeWidth = barWidth)
        }
      }
}

/**
 * A composable to reuse the microphone icon and the pulsing animations in multiple screens
 *
 * @param viewModel The SpeakingViewModel of the screen
 * @param analysisState The state of analysis of the speech
 * @param permissionGranted Boolean that tells us whether permission to record a screen has been
 *   granted or not
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MicrophoneButton(
    viewModel: SpeakingViewModel,
    analysisState: State<SpeakingRepository.AnalysisState>,
    permissionGranted: MutableState<Boolean>,
    context: Context,
    funRec: () -> Unit = {},
    audioFile: File = File(context.cacheDir, "audio_record.wav")
) {
  val infiniteTransition = rememberInfiniteTransition(label = "")
  val requestPerms = remember { mutableStateOf(false) }
  val scale by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.5f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
          label = "")

  // Microphone button with animation
  Button(
      onClick = {
        if (!permissionGranted.value) {
          Toast.makeText(context, "You need to allow permissions!", Toast.LENGTH_SHORT).show()
        }
        funRec()
        viewModel.onMicButtonClicked(permissionGranted.value, audioFile)
      },
      modifier =
          Modifier.size(AppDimensions.buttonSize)
              .scale(
                  if (analysisState.value == SpeakingRepository.AnalysisState.RECORDING) scale
                  else 1f)
              .testTag("mic_button"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
      shape = AppShapes.circleShape,
      border =
          BorderStroke(
              width = AppDimensions.borderStrokeWidth, color = MaterialTheme.colorScheme.primary)) {
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
            tint = MaterialTheme.colorScheme.primary)
      }
}

@SuppressLint("MissingPermission")
@Composable
fun handleAudioRecording(
    analysisState: State<SpeakingRepository.AnalysisState>,
    permissionGranted: MutableState<Boolean>,
    amplitudes: SnapshotStateList<Float>
) {
  // Audio recording and amplitude collection
  LaunchedEffect(analysisState.value, permissionGranted) {
    if (permissionGranted.value &&
        analysisState.value == SpeakingRepository.AnalysisState.RECORDING) {
      val sampleRateInHz = 44100
      val channelConfig = AudioFormat.CHANNEL_IN_MONO
      val audioFormat = AudioFormat.ENCODING_PCM_16BIT

      val bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
      val audioRecord =
          AudioRecord(
              MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize)
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
}
