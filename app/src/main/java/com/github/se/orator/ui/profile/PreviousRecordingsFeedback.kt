package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.symblAi.AndroidAudioPlayer
import com.github.se.orator.model.symblAi.AudioPlayer
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
import java.io.File
import loadPromptsFromFile

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun PreviousRecordingsFeedbackScreen(
    context: Context = LocalContext.current,
    navigationActions: NavigationActions,
    viewModel: ChatViewModel,
    speakingViewModel: SpeakingViewModel,
    player: AudioPlayer = AndroidAudioPlayer(context)
) {

  // val recorder by lazy { AudioRecorder(context = context) }

  // val player by lazy { AndroidAudioPlayer(context) }
  var prompts: Map<String, String>? =
      loadPromptsFromFile(context)?.find { it["ID"] == speakingViewModel.interviewPromptNb.value }
  var ID: String = prompts?.get("ID") ?: "audio.mp3"
  var audioFile: File = File(context.cacheDir, "$ID.mp3")

  val offlineAnalysisData by speakingViewModel.offlineAnalysisData.collectAsState()

  LaunchedEffect(Unit) {
    prompts =
        loadPromptsFromFile(context)?.find { it["ID"] == speakingViewModel.interviewPromptNb.value }

    ID = prompts?.get("ID") ?: "audio.mp3"

    audioFile = File(context.cacheDir, "$ID.mp3")

    Log.d("PreviousRecordingsFeedbackScreen", "Screen is opened, running code.")
    // Call necessary methods or logic when the screen is opened
    speakingViewModel.getTranscript(audioFile)
    // Any other initialization logic
    Log.d("prompts are: ", prompts?.get("targetPosition") ?: "Default Value")
    viewModel.resetResponse()
  }

  val response by viewModel.response.collectAsState("")

  LaunchedEffect(response) { Log.d("gpt said: ", "gpt said: $response") }

  if (offlineAnalysisData != null) {
    viewModel.offlineRequest(
        offlineAnalysisData!!.transcription.removePrefix("You said:").trim(),
        prompts?.get("targetCompany") ?: "Apple",
        prompts?.get("jobPosition") ?: "engineer")
    Log.d("testing offline chat view model", "the gpt model offline value response is $response")
    // Text(text = "What you said: ${what_has_been_said.value}")
    Text(text = "Interviewer's response: $response", color = Color.Black)
    Log.d("d", "Hello! This is has been said: ${offlineAnalysisData!!.transcription}")
  }
  // prompts?.get("targetPosition") ?: "Default Value"
  // val jobPosition = prompts?.get("jobPosition")

  // val chatMessages by chatViewModel.chatMessages.collectAsState()

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(AppDimensions.paddingMedium)
              .testTag("RecordingReviewScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { player.playFile(audioFile) },
            shape = AppShapes.circleShape,
            modifier = Modifier.testTag("play_button"),
            colors = ButtonDefaults.buttonColors(Color.White),
            contentPadding = PaddingValues(0.dp)) {
              androidx.compose.material.Icon(
                  Icons.Outlined.PlayCircleOutline,
                  contentDescription = "Play button",
                  modifier = Modifier.size(30.dp),
                  tint = AppColors.primaryColor)
            }

        Row(
            modifier = Modifier.fillMaxWidth().testTag("Back"),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Filled.ArrowBack,
                  contentDescription = "Back",
                  modifier =
                      Modifier.size(AppDimensions.iconSizeSmall)
                          .clickable { navigationActions.goBack() }
                          .testTag("BackButton"),
                  tint = MaterialTheme.colorScheme.primary)
            }
      }
}
