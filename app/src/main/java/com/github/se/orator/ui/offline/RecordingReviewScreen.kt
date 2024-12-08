package com.github.se.orator.ui.offline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes

@Composable
fun RecordingReviewScreen(
    navigationActions: NavigationActions,
    speakingViewModel: SpeakingViewModel = viewModel()
) {
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(AppDimensions.paddingMedium)
              .testTag("RecordingReviewScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().testTag("Back"),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  modifier =
                      Modifier.size(AppDimensions.iconSizeSmall)
                          .clickable { navigationActions.goBack() }
                          .testTag("BackButton"),
                  tint = MaterialTheme.colorScheme.primary)
            }

        Text(
            text = "Soon you will be able to : Play Recording, Comment on it and Save it locally",
            fontSize = AppFontSizes.bodyLarge,
            modifier = Modifier.padding(AppDimensions.paddingMedium).testTag("InstructionText"))
      }
}
