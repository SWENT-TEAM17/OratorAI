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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.symblAi.AndroidAudioPlayer
import com.github.se.orator.model.symblAi.AudioPlayer
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun PreviousRecordingsFeedbackScreen(
    context: Context = LocalContext.current,
    navigationActions: NavigationActions,
    viewModel: ChatViewModel,
    speakingViewModel: SpeakingViewModel,
    player: AudioPlayer = AndroidAudioPlayer(context),
    offlinePromptsFunctions: OfflinePromptsFunctionsInterface
) {
  var prompts: Map<String, String>? =
      offlinePromptsFunctions.loadPromptsFromFile(context)?.find {
        it["ID"] == speakingViewModel.interviewPromptNb.value
      }
  var ID: String = prompts?.get("ID") ?: "audio.mp3"
  var audioFile: File = File(context.cacheDir, "$ID.mp3")

  val fileData by offlinePromptsFunctions.fileData.collectAsState()

  LaunchedEffect(Unit) {
    // clearing old display text
    offlinePromptsFunctions.clearDisplayText()
    // read the file containing interviewer's response
    offlinePromptsFunctions.readPromptTextFile(context, ID)

    // retrieve previous interviews mapping
    prompts =
        offlinePromptsFunctions.loadPromptsFromFile(context)?.find {
          it["ID"] == speakingViewModel.interviewPromptNb.value
        }

    ID = prompts?.get("ID") ?: "audio.mp3"
    audioFile = File(context.cacheDir, "$ID.mp3")

    Log.d("PreviousRecordingsFeedbackScreen", "Screen is opened, running code.")
  }

  // if there isn't already an interviewer response: transcribe text + request a gpt prompt
  if (fileData == "Loading interviewer response..." || fileData.isNullOrEmpty()) {
    Log.d("in pre ", "calling get transcript and gpt response $fileData")
      speakingViewModel.getTranscriptAndGetGPTResponse(
          audioFile, prompts, viewModel, context, offlinePromptsFunctions)
  }

  // text corresponding to interviewer's response
  val displayText =
      when {
        fileData == "Loading interviewer response..." || fileData.isNullOrEmpty() -> {
          "Processing your audio, please wait..."
        }
        else -> "Interviewer's response: $fileData"
      }

  // rest of UI elements
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(AppDimensions.paddingMedium)
              .testTag("RecordingReviewScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = displayText ?: "",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("ResponseText"))

        Button(
            onClick = { player.playFile(audioFile) },
            shape = AppShapes.circleShape,
            modifier = Modifier.testTag("play_button"),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(0.dp)) {
              Icon(
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
