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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.R
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes

@SuppressLint("MissingPermission")
@Composable
fun OfflineRecordingScreen(
    navigationActions: NavigationActions,
    question: String,
    speakingViewModel: SpeakingViewModel = viewModel()
) {
  var permissionGranted by remember { mutableStateOf(false) }
  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
          speakingViewModel.onMicButtonClicked(true)
        }
      }

  val isRecording by speakingViewModel.isRecording.collectAsState()
  val infiniteTransition = rememberInfiniteTransition()
  val scale by
      infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.3f,
          animationSpec =
              infiniteRepeatable(
                  animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse))

  DisposableEffect(Unit) {
    onDispose {
      if (isRecording) {
        speakingViewModel.endAndSave()
      }
    }
  }
  val colors = MaterialTheme.colorScheme

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
                    Image(
                        painter = painterResource(id = R.drawable.bckgrnd_blobs),
                        contentDescription = "Background",
                        modifier = Modifier.size(AppDimensions.logoSize).testTag("BackgroundBlob"))
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Microphone",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMic)
                                .scale(if (isRecording) scale else 1f)
                                .clickable {
                                  if (permissionGranted) {
                                    speakingViewModel.onMicButtonClicked(true)
                                  } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                  }
                                }
                                .testTag("MicIcon"),
                        tint = colors.secondary)
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
                    speakingViewModel.endAndSave()
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
