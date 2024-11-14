package com.github.se.orator.ui.overview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.network.Message // Import Message class for structured message data
import com.github.se.orator.ui.theme.AppColors // Import theme colors
import com.github.se.orator.ui.theme.AppDimensions // Import theme dimensions for consistent spacing
import com.github.se.orator.ui.theme.AppTypography // Import theme typography for text styles

/**
 * The FeedbackScreen composable displays the feedback screen where users can see generated
 * feedback, retry, or navigate back.
 *
 * @param chatViewModel The view model for the chat, providing feedback-related data.
 * @param navigationActions The navigation actions that can be performed from this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(chatViewModel: ChatViewModel, navigationActions: NavigationActions) {
  // State variables for feedback message, loading status, and error message.
  var feedbackMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Launch effect to generate feedback when the screen is first displayed.
  // Tries to get feedback from `chatViewModel` and updates states based on success/failure.
  LaunchedEffect(Unit) {
    try {
      feedbackMessage = chatViewModel.generateFeedback()
    } catch (e: Exception) {
      // In case of error, capture the error message.
      errorMessage = e.localizedMessage
    } finally {
      // Set loading to false regardless of success or failure.
      isLoading = false
    }
  }

  // Scaffold is the main layout component that provides a basic structure with top bar and content
  // area.
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("feedbackScreen"),
      topBar = {
        // TopAppBar to display the screen title and back navigation.
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().testTag("feedbackTopAppBar"),
            title = {
              Text(
                  text = "Feedback",
                  modifier =
                      Modifier.testTag("FeedbackText"), // Title text displayed in the top app bar.
                  fontWeight = FontWeight.Bold, // Bold font for emphasis
                  color = AppColors.textColor // Using theme color for title text
                  )
            },
            // Back button to navigate to the previous screen.
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, // Action to navigate back
                  modifier = Modifier.testTag("back_button") // Test tag for UI testing
                  ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Back arrow icon
                        contentDescription = "Back", // Content description for accessibility
                        modifier =
                            Modifier.size(AppDimensions.iconSizeSmall), // Icon size from theme
                        tint = AppColors.textColor // Icon color from theme
                        )
                  }
            },
            // Background and title colors for the app bar, using themed colors.
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.surfaceColor,
                    titleContentColor = AppColors.textColor))
      },
      content = { paddingValues ->
        // Main content column, respecting the scaffold's padding values.
        Column(
            modifier =
                Modifier.fillMaxSize() // Fill the screen space
                    .padding(paddingValues) // Apply padding from the scaffold
                    .testTag("feedbackContent"),
        ) {
          Divider() // Divider below the top bar for visual separation
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(
                          horizontal = AppDimensions.paddingMedium) // Horizontal padding from theme
                      .padding(top = AppDimensions.paddingSmall) // Top padding from theme
                      .testTag("feedbackTitle"), // Test tag for UI testing
              horizontalAlignment = Alignment.CenterHorizontally // Center alignment
              ) {
                // Subtitle at the top to introduce feedback.
                Box(
                    modifier =
                        Modifier.fillMaxWidth().testTag("feedbackSubtitle") // Apply testTag here
                    ) {
                      ChatMessageItem(
                          message =
                              Message(
                                  content = "Here's what you did well and where you can improve:",
                                  role = "assistant"))
                    }

                // Display appropriate content based on loading, error, or feedback state.
                when {
                  isLoading -> {
                    // Show loading spinner while feedback is being generated.
                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(Alignment.CenterHorizontally)
                                .padding(AppDimensions.paddingMedium) // Padding around the spinner
                                .testTag("loadingIndicator"), // Test tag for UI testing
                        color = AppColors.loadingIndicatorColor // Spinner color from theme
                        )
                  }
                  errorMessage != null -> {
                    // Show error message if there was an issue generating feedback.
                    Text(
                        text = "Error: $errorMessage", // Display the error message
                        color = AppColors.errorColor, // Text color from theme
                        modifier = Modifier.testTag("errorText") // Test tag for UI testing
                        )
                  }
                  feedbackMessage != null -> {
                    // Display the feedback message using `ChatMessageItem` for a consistent
                    // look.
                    Box(
                        modifier =
                            Modifier.fillMaxWidth() // Fill available width
                                .weight(1f) // Allow Box to grow and take available space
                                .verticalScroll(rememberScrollState())
                                .testTag("feedbackMessage")
                        // Enable scrolling for long content
                        ) {
                          ChatMessageItem(
                              message =
                                  Message(
                                      content = feedbackMessage!!, // Display feedback content
                                      role = "assistant" // Set role to "assistant" for styling
                                      ))
                        }
                  }
                  else -> {
                    // Display if there is no feedback available (edge case).
                    Text(
                        text = "No feedback available.",
                        style = AppTypography.bodyLargeStyle, // Style from theme typography
                        color = AppColors.textColor, // Text color from theme
                        modifier = Modifier.testTag("feedbackNoMessage") // Test tag for UI testing
                        )
                  }
                }

                // Button for retrying or navigating back, positioned below the feedback
                // content.
                Button(
                    onClick = {
                      navigationActions.navigateTo(
                          TopLevelDestinations.HOME) // Action to retry or go home
                    },
                    modifier =
                        Modifier.fillMaxWidth() // Button fills available width
                            .padding(top = AppDimensions.paddingMedium) // Top padding from theme
                            .border(
                                width = AppDimensions.borderStrokeWidth, // Border width from theme
                                color = AppColors.buttonBorderColor, // Border color from theme
                                shape =
                                    MaterialTheme.shapes.medium // Button shape from MaterialTheme
                                )
                            .testTag("retryButton"),
                    enabled = !isLoading, // Disable button while loading
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                AppColors.buttonOverviewColor, // Background color from theme
                            contentColor = AppColors.textColor // Text color from theme
                            )) {
                      Text(
                          text = "Try Again",
                          modifier = Modifier.testTag("retryButtonText")) // Button label
                }
              }
        }
      })
}
