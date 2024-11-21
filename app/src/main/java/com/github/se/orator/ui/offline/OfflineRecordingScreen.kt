package com.github.se.orator.ui.offline

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.R
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.speaking.MicrophoneButton
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes

// TODO: remove this suppress and fix the permissions
@SuppressLint("MissingPermission")
@Composable
fun OfflineRecordingScreen(
    navigationActions: NavigationActions,
    question: String,
    viewModel: SpeakingViewModel = viewModel()
) {
    val analysisState = viewModel.analysisState.collectAsState()
    val analysisData by viewModel.analysisData.collectAsState()

  val permissionGranted = remember { mutableStateOf(false) }
  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted -> permissionGranted.value = isGranted
      }

  val isRecording by viewModel.isRecording.collectAsState()

    DisposableEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        onDispose { viewModel.endAndSave() }
    }

  val colors = MaterialTheme.colorScheme
    val amplitudes = remember { mutableStateListOf<Float>() }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(colors.background)
              .padding(WindowInsets.systemBars.asPaddingValues())
              .padding(horizontal = AppDimensions.paddingMedium)
              .testTag("OfflineRecordingScreen"),
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(vertical = AppDimensions.paddingMedium)
                    .testTag("BackButtonRow"),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Filled.ArrowBack,
                  contentDescription = "Back",
                  modifier =
                      Modifier.size(AppDimensions.iconSizeSmall)
                          .clickable { navigationActions.goBack() }
                          .padding(AppDimensions.paddingExtraSmall)
                          .testTag("BackButton"),
                  tint = colors.primary)
            }

        Spacer(
            modifier =
                Modifier.height(
                    AppDimensions.largeSpacerHeight)) // /// or   val buttonHeight = 48.dp

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
                  modifier =
                      Modifier.size(AppDimensions.logoSize)
                          .testTag("MicIconContainer")) { // // should be 203.dp
                  MicrophoneButton(viewModel, analysisState, permissionGranted, LocalContext.current)
                  }

              Text(
                  text = question,
                  fontSize = AppFontSizes.bodyLarge,
                  color = colors.onSurface,
                  modifier =
                      Modifier.padding(top = AppDimensions.paddingMedium).testTag("QuestionText"))

              Spacer(modifier = Modifier.weight(1f))

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
