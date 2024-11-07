package com.github.se.orator.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.github.se.orator.R
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

/**
 * The FeedbackScreen composable displays the feedback screen.
 *
 * @param chatViewModel The view model for the chat.
 * @param navController The navigation controller.
 * @param navigationActions The navigation actions that can be performed.
 */
@Composable
fun FeedbackScreen(
    chatViewModel: ChatViewModel,
    navController: NavHostController,
    navigationActions: NavigationActions
) {
  // State variables
  var feedbackMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Trigger feedback generation when the screen is displayed
  LaunchedEffect(Unit) {
    try {
      feedbackMessage = chatViewModel.generateFeedback()
    } catch (e: Exception) {
      errorMessage = e.localizedMessage
    } finally {
      isLoading = false
    }
  }

  // UI
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("feedbackScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().testTag("feedbackTopAppBar"),
            backgroundColor = AppColors.surfaceColor,
            contentColor = AppColors.textColor,
            elevation = AppDimensions.elevationSmall,
            title = { Text("Feedback", modifier = Modifier.testTag("FeedbackText")) },
            navigationIcon = {
              IconButton(
                  onClick = { navController.popBackStack() },
                  modifier = Modifier.testTag("back_button")) {
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Back",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeSmall).testTag("back_button"))
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(AppDimensions.paddingMedium)
                    .testTag("feedbackContent"),
            verticalArrangement = Arrangement.Top) {
              // Header Title and Subtitle
              Text(
                  text = "Your Feedback",
                  style = AppTypography.titleLargeStyle,
                  modifier =
                      Modifier.padding(bottom = AppDimensions.paddingSmall)
                          .testTag("feedbackTitle"))
              Text(
                  text = "Here's what you did well and where you can improve.",
                  style = AppTypography.subtitleStyle,
                  modifier =
                      Modifier.padding(bottom = AppDimensions.paddingLarge)
                          .testTag("feedbackSubtitle"))

              if (isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally).testTag("loadingIndicator"))
              } else if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = AppColors.errorColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("errorText"))
              } else if (feedbackMessage != null) {
                // Display the feedback message
                Text(
                    text = feedbackMessage!!,
                    style = AppTypography.bodyLargeStyle,
                    modifier =
                        Modifier.padding(bottom = AppDimensions.paddingMedium)
                            .testTag("feedbackContent"))
              } else {
                Text(
                    text = "No feedback available.",
                    style = AppTypography.bodyLargeStyle,
                    modifier =
                        Modifier.padding(bottom = AppDimensions.paddingMedium)
                            .testTag("feedbackContent"))
              }

              // Buttons for Retry and Review
              Row(
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = AppDimensions.paddingMedium)
                          .testTag("feedbackButtons")) {
                    Button(
                        onClick = {
                          // Use navigationActions to navigate to Home
                          navigationActions.navigateTo(TopLevelDestinations.HOME)
                        },
                        modifier = Modifier.testTag("retryButton")) {
                          Text("Try Again", modifier = Modifier.testTag("retryButtonText"))
                        }
                    // Add additional buttons if needed
                  }
            }
      })
}
