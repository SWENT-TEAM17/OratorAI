package com.github.se.orator.ui.offline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions

/**
 * Basic implementation of the RecordingReviewScreen, setting up the UI structure for reviewing a
 * recorded audio. In this preliminary version, functionalities such as playback, commenting, and
 * saving are placeholders for future development.
 *
 * @param navigationActions Used to handle navigation events.
 * @param speakingViewModel ViewModel handling recording and playback-related data.
 */
@Composable
fun RecordingReviewScreen(
    navigationActions: NavigationActions,
    speakingViewModel: SpeakingViewModel = viewModel()
) {
  // Main column container for the screen content, centered both vertically and horizontally
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(16.dp)
              .testTag("RecordingReviewScreen"), // Test tag for identifying the screen in tests
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Header row containing the back button
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .testTag("BackButtonRow"), // Test tag for the row with back button
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Filled.ArrowBack,
                  contentDescription = "Back", // Accessibility description for the back icon
                  modifier =
                      Modifier.size(32.dp)
                          .clickable { navigationActions.goBack() } // Navigates back when clicked
                          .testTag("BackButton"), // Test tag for back button
                  tint =
                      MaterialTheme.colorScheme
                          .primary // Primary color from the theme for icon tint
                  )
            }

        // Instructional text to inform the user about upcoming features
        Text(
            text = "Soon you will be able to : Play Recording, Comment on it and Save it locally",
            modifier =
                Modifier.padding(8.dp)
                    .testTag("InstructionText") // Test tag for identifying the instructional text
            )
      }
}
