package com.github.se.orator.ui.offline

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.model.symblAi.AndroidAudioPlayer
import com.github.se.orator.model.symblAi.AudioRecorder
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import java.io.File

@Composable
fun RecordingReviewScreen(
    context: Context,
    navigationActions: NavigationActions,
    speakingViewModel: SpeakingViewModel = viewModel()
) {
    val recorder by lazy {
        AudioRecorder(context = context)
    }

    val player by lazy {
        AndroidAudioPlayer(context)
    }

    val audioFile: File = File(context.cacheDir, "audio.mp3")

    Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(AppDimensions.paddingMedium)
              .testTag("RecordingReviewScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {

      Button(
          onClick = {
              player.playFile(audioFile)
          }
      ) {
          Text(text = "Hear recording")
      }

        Button(
            onClick = {
                player.stop()
            }
        ) {
            Text(text = "Stop recording")
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
