package com.github.se.orator.ui.offline

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.model.symblAi.AudioRecorder
import com.github.se.orator.model.symblAi.SpeakingError
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.speaking.MicrophoneButton
import com.github.se.orator.ui.speaking.handleAudioRecording
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow

// TODO: remove this suppress and fix the permissions
@SuppressLint("MissingPermission", "StateFlowValueCalledInComposition")
@Composable
fun OfflineRecordingScreen(
    context: Context = LocalContext.current,
    navigationActions: NavigationActions,
    question: String,
    viewModel: SpeakingViewModel = viewModel(),
    permissionGranted: MutableState<Boolean> = remember {
      mutableStateOf(false)
    } // Makes for easier testing
) {
  val analysisState = remember {
    MutableStateFlow(SpeakingRepository.AnalysisState.IDLE)
  } // viewModel.analysisState.collectAsState()
  val collState = analysisState.collectAsState()
  // val analysisData by viewModel.analysisData.collectAsState()
  val recorder by lazy { AudioRecorder(context = context) }

  //    val player by lazy {
  //        AndroidAudioPlayer(context)
  //    }

  var audioFile: File? = null // Keep this!! It should not be greyed out (bug?)
  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        permissionGranted.value = isGranted
      }

  DisposableEffect(Unit) {
    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

    onDispose { viewModel.endAndSave() }
  }

  val colors = MaterialTheme.colorScheme
  val amplitudes = remember { mutableStateListOf<Float>() }
  handleAudioRecording(collState, permissionGranted, amplitudes)

  // back button
  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(colors.background)
              .padding(WindowInsets.systemBars.asPaddingValues())
              .padding(horizontal = AppDimensions.paddingMedium)
              .testTag("OfflineRecordingScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(vertical = AppDimensions.paddingMedium)
                    .testTag("BackButtonRow"),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  modifier =
                      Modifier.size(AppDimensions.iconSizeSmall)
                          .clickable { navigationActions.goBack() }
                          .padding(AppDimensions.paddingExtraSmall)
                          .testTag("BackButton"),
                  tint = colors.primary)
            }

        // Microphone UI and its functionality
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = AppDimensions.paddingMedium)
                    .testTag("RecordingColumn"),
            verticalArrangement =
                Arrangement.spacedBy(AppDimensions.paddingMedium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.size(AppDimensions.logoSize).testTag("MicIconContainer")) {
                    MicrophoneButton(
                        viewModel,
                        collState,
                        permissionGranted,
                        context,
                        funRec = {
                          // what to do when user begins to record a file
                          if (analysisState.value == SpeakingRepository.AnalysisState.IDLE ||
                              analysisState.value == SpeakingRepository.AnalysisState.IDLE) {
                            File(context.cacheDir, "${viewModel.interviewPromptNb.value}.mp3")
                                .also {
                                  recorder.startRecording(it)
                                  audioFile = it
                                }
                            Log.d(
                                "aa",
                                "now transcribing to: ${viewModel.interviewPromptNb.value}.mp3")
                            analysisState.value = SpeakingRepository.AnalysisState.RECORDING
                          }
                          // what to do when user finishes recording a file
                          else if (analysisState.value ==
                              SpeakingRepository.AnalysisState.RECORDING) {
                            File(context.cacheDir, "${viewModel.interviewPromptNb.value}.mp3")
                                .also {
                                  Log.d(
                                      "aall",
                                      " file saved to: \"${viewModel.interviewPromptNb.value}.mp3\"")
                                  recorder.stopRecording()
                                }
                            analysisState.value = SpeakingRepository.AnalysisState.FINISHED
                          }
                        })
                  }

              Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

              // Display feedback messages
              val feedbackMessage =
                  when (analysisState.value) {
                    SpeakingRepository.AnalysisState.RECORDING -> "Recording..."
                    SpeakingRepository.AnalysisState.IDLE -> "Tap the mic to start recording."
                    else ->
                        when (viewModel.analysisError.value) {
                          SpeakingError.NO_ERROR -> "Analysis finished."
                          else -> "Finished recording"
                        }
                  }

              Text(
                  feedbackMessage,
                  modifier = Modifier.testTag("mic_text"),
                  fontSize = AppFontSizes.bodyLarge,
                  color = colors.onSurface)

              // question for user to remember
              Text(
                  text = question,
                  fontSize = AppFontSizes.bodyLarge,
                  color = colors.onSurface,
                  modifier =
                      Modifier.padding(top = AppDimensions.paddingMedium).testTag("QuestionText"))

              Spacer(modifier = Modifier.weight(1f))

              // button for user to click when he is done recording
              Button(
                  onClick = {
                    viewModel.endAndSave()
                    navigationActions.navigateTo(Screen.OFFLINE_RECORDING_REVIEW_SCREEN)
                  },
                  modifier =
                      Modifier.fillMaxWidth(0.6f)
                          .padding(AppDimensions.paddingSmall)
                          .testTag("DoneButton"),
                  colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
                    Text(
                        text = "Done!",
                        fontSize = AppFontSizes.buttonText,
                        color = colors.onPrimary)
                  }
            }
      }
}
