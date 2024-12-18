package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.symblAi.AndroidAudioPlayer
import com.github.se.orator.model.symblAi.AudioPlayer
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopNavigationMenu
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
        Log.d("aa", "launching")
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

  Scaffold(
      topBar = { TopNavigationMenu(title = "Interview ",
          navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                  androidx.compose.material.Icon(
                      Icons.Outlined.ArrowBackIosNew,
                      contentDescription = "Back button",
                      modifier = Modifier.size(AppDimensions.iconSizeMedium),
                      tint = Color.Black)
              }
          })

      }) {
      innerPadding ->
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(innerPadding).padding(AppDimensions.paddingMedium)
                .testTag("RecordingReviewScreen"),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
          if (fileData == "Loading interviewer response..." || fileData.isNullOrEmpty()) {
            LoadingUI(ID)
          } else {
              Button(
                  modifier = Modifier.testTag("hear_recording_button"),
                  colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  onClick = { player.playFile(audioFile) }) {
                  Text(text = "Hear audio", color = MaterialTheme.colorScheme.surface)
              }
              Button(
                  modifier = Modifier.testTag("stop_recording_button"),
                  colors =
                  ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  onClick = { player.stop() }) {
                  Text(text = "Stop audio", color = MaterialTheme.colorScheme.surface)
              }
              Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
              HorizontalDivider()
              Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
              Text(
                  text = "You answered: ${offlinePromptsFunctions.getPromptMapElement(ID, "transcription", context)}",
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag("ResponseText"))
              Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
              HorizontalDivider()
              Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
            Text(
                text = displayText ?: "",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("ResponseText"))

          }
        }
  }
}

@Composable
fun LoadingUI(ID: String) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .testTag("loadingColumn"), // Ensures the Column takes up the entire screen
      verticalArrangement = Arrangement.Center, // Centers content vertically
      horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
      ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onBackground,
            strokeWidth = AppDimensions.strokeWidth,
            modifier =
                Modifier.size(AppDimensions.loadingIndicatorSize).testTag("loadingIndicator"))
        Text(
            "Loading interviewer response...",
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("interviewerResponse"))
      }
}
