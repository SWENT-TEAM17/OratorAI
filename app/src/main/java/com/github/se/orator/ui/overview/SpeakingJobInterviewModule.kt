package com.github.se.orator.ui.overview

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * The SpeakingJobInterviewModule composable is a composable screen that displays the job interview
 * module.
 *
 * @param navigationActions The navigation actions that can be performed.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun SpeakingJobInterviewModule(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    apiLinkViewModel: ApiLinkViewModel
) {
  var targetPosition by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var skills by remember { mutableStateOf("") }
  var feedbackType by remember { mutableStateOf("") }

  val inputFields =
      listOf(
          InputFieldData(
              value = targetPosition,
              onValueChange = { newValue: String ->
                targetPosition = newValue
              }, // Explicitly specify String for newValue
              label = "What is your target job position?",
              placeholder = "e.g Senior executive",
              testTag = "levelInput"),
          InputFieldData(
              value = companyName,
              onValueChange = { newValue: String ->
                companyName = newValue
              }, // Explicitly specify String
              label = "Which company are you applying to?",
              placeholder = "e.g McKinsey",
              testTag = "jobInput",
              height = 200),
          InputFieldData(
              value = skills,
              onValueChange = { newValue: String ->
                skills = newValue
              }, // Explicitly specify String
              label = "What skills or qualifications do you want to highlight?",
              placeholder = "e.g Problem solving",
              height = 85,
              testTag = "skillsInput"),
          InputFieldData(
              value = feedbackType,
              onValueChange = { newValue: String ->
                feedbackType = newValue
              }, // Explicitly specify String
              label = "Do you want feedback on persuasive language, volume, or delivery?",
              placeholder = "e.g Persuasive language",
              height = 85,
              testTag = "experienceInput"))

  SpeakingPracticeModule(
      navigationActions = navigationActions,
      screenTitle = "Job Interview",
      headerText = "Ace your next job interview",
      inputs = inputFields,
      onGetStarted = {
        // Collect the inputs and create an InterviewContext object
        val interviewContext =
            InterviewContext(
                interviewType = "job interview",
                role = targetPosition,
                company = companyName,
                focusAreas = skills.split(",").map { it.trim() })

        apiLinkViewModel.updatePracticeContext(interviewContext)

        chatViewModel.initializeConversation()

        // Navigate to ChatScreen with the collected data
        navigationActions.navigateTo(Screen.CHAT_SCREEN)
      })
}
