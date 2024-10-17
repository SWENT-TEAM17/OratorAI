package com.github.se.orator.ui.overview

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import com.github.se.orator.ui.navigation.NavigationActions

/**
 * The SpeakingJobInterviewModule composable is a composable screen that displays the job interview
 * module.
 *
 * @param navigationActions The navigation actions that can be performed.
 */
@Composable
fun SpeakingPublicSpeaking(navigationActions: NavigationActions) {
  var occasion by remember { mutableStateOf("") }
  var demographic by remember { mutableStateOf("") }
  var mainPoints by remember { mutableStateOf("") }
  var feedbackLanguage by remember { mutableStateOf("") }

  val inputFields =
      listOf(
          InputFieldData(
              value = occasion,
              onValueChange = { newValue: String ->
                occasion = newValue
              }, // Explicitly specify String
              label = "What is the occasion?",
              placeholder = "e.g Conference",
              testTag = "occasionInput"),
          InputFieldData(
              value = demographic,
              onValueChange = { newValue: String ->
                demographic = newValue
              }, // Explicitly specify String
              label = "What is your audience demographic?",
              placeholder = "e.g PHDs",
              testTag = "demographicInput",
              height = 200),
          InputFieldData(
              value = mainPoints,
              onValueChange = { newValue: String ->
                mainPoints = newValue
              }, // Explicitly specify String
              label = "What are the key points or themes of your speech?",
              placeholder = "e.g AI, Machine Learning",
              height = 85,
              testTag = "mainPointsInput"),
          InputFieldData(
              value = feedbackLanguage,
              onValueChange = { newValue: String ->
                feedbackLanguage = newValue
              }, // Explicitly specify String
              label = "Do you want suggestions on intonation or body language?",
              placeholder = "e.g Intonation",
              height = 85,
              testTag = "feedbackLanguageInput"))

  SpeakingPracticeModule(
      navigationActions = navigationActions,
      screenTitle = "Public Speaking",
      headerText = "Make your speech memorable",
      inputs = inputFields)
}
