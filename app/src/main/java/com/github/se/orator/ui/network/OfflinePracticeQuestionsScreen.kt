package com.github.se.orator.ui.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Composable function to display a list of predefined questions for offline practice. When the user
 * clicks on a question, they are navigated to a recording screen with the selected question passed
 * as a parameter.
 *
 * @param navController The NavController to handle navigation actions.
 */
@Composable
fun OfflinePracticeQuestionsScreen(navController: NavController) {
  // List of predefined practice questions
  val questions =
      listOf(
          "What are your strengths?",
          "Describe a challenging situation you've faced.",
          "Why do you want this position?",
          "Tell me about a time you demonstrated leadership.",
          "How do you handle conflict in a team?")

  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  WindowInsets.systemBars.asPaddingValues()) // Adds padding to adapt to system bars
              .padding(horizontal = 16.dp), // Additional horizontal padding for spacing
      /// TODO: think about automating padding

      verticalArrangement = Arrangement.Top,
      horizontalAlignment = Alignment.Start) {
        // Header text for the list of questions
        Text(text = "Choose a question to practice:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Display each question in the list with a clickable action
        questions.forEach { question ->
          Text(
              text = question,
              fontSize = 16.sp,
              modifier =
                  Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                    // Here we need to navigate to the recording screen and pass the selected
                    // question as a parameter
                    // should we ask for all the parameters ? or hardcode parameters for when we're
                    // back online ...
                    // navController.navigate("${Route.RECORD_ANSWER}/$question")
                  })
          // Divider line to separate each question visually
          Divider()
        }
      }
}
