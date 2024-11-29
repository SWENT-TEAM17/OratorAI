package com.github.se.orator.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes

@Composable
fun OfflinePracticeQuestionsScreen(navigationActions: NavigationActions) {
  val colors = MaterialTheme.colorScheme

  // List of questions to display as practice options
  val questions =
      listOf(
          "What are your strengths?",
          "Describe a challenging situation you've faced.",
          "Why do you want this position?",
          "Tell me about a time you demonstrated leadership.",
          "How do you handle conflict in a team?")

  // Main container column for layout, centered horizontally and taking full screen height
  Column(
      modifier =
          Modifier.fillMaxSize()
              .background(colors.background)
              .padding(WindowInsets.systemBars.asPaddingValues())
              .padding(horizontal = AppDimensions.paddingMedium)
              .testTag("OfflinePracticeQuestionsScreen"), // Test tag for identifying this screen
      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Row container for back button and title, positioned at the top
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = AppDimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically) {
              // Back button icon, navigates back to Offline screen
              Icon(
                  imageVector = Icons.Filled.ArrowBack,
                  contentDescription = "Back", // Accessibility description
                  modifier =
                      Modifier.size(AppDimensions.iconSizeSmall)
                          .clickable {
                            navigationActions.navigateTo(Screen.OFFLINE_INTERVIEW_MODULE)
                          } // Go back to Offline screen
                          .padding(AppDimensions.paddingExtraSmall)
                          .testTag("BackButton"), // Test tag for the back button
                  tint = colors.primary // Primary color from the theme for the icon
                  )

              Spacer(modifier = Modifier.width(AppDimensions.spacerWidthMedium))

              // Title text for the screen, styled to be bold and centered vertically
              Text(
                  text = "Choose your practice question",
                  fontSize = AppFontSizes.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = colors.onBackground, // Color from theme
                  modifier =
                      Modifier.align(Alignment.CenterVertically)
                          .testTag("TitleText") // Test tag for title text
                  )
            }

        Spacer(modifier = Modifier.height(AppDimensions.spacerHeightMedium))

        // Display each question in a Card with clickable functionality
        questions.forEachIndexed { index, question ->
          Card(
              shape = RoundedCornerShape(AppDimensions.cardCornerRadius),
              elevation =
                  CardDefaults.cardElevation(defaultElevation = AppDimensions.elevationSmall),
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = AppDimensions.paddingSmall)
                      .clickable {
                        // Navigate to OfflineRecordingScreen with the selected question
                        navigationActions.goToOfflineRecording(question)
                      }
                      .testTag("QuestionCard_$index"), // Test tag for each question card
              colors =
                  CardDefaults.cardColors(
                      containerColor = colors.surface) // Surface color for card background
              ) {
                // Box container for the question text inside each card
                Box(modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingMedium)) {
                  // Display the question text inside the card
                  Text(
                      text = question,
                      fontSize = AppFontSizes.bodyLarge,
                      color = colors.secondary, // Secondary color for text
                      fontWeight = FontWeight.Medium,
                      modifier =
                          Modifier.testTag("QuestionText_$index") // Test tag for each question text
                      )
                }
              }
        }
      }
}
