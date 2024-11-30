package com.github.se.orator.ui.overview

import androidx.compose.runtime.*
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * The SpeakingPublicSpeakingModule composable displays the public speaking module.
 *
 * @param navigationActions The navigation actions that can be performed.
 */
@Composable
fun SpeakingPublicSpeakingModule(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    apiLinkViewModel: ApiLinkViewModel
) {
  var occasion by remember { mutableStateOf("") }
  var purpose by remember { mutableStateOf("") }
  var audienceSize by remember { mutableStateOf("") }
  var demographic by remember { mutableStateOf("") }
  var presentationStyle by remember { mutableStateOf("") }
  var mainPoints by remember { mutableStateOf("") }
  var visualAids by remember { mutableStateOf("") }
  var experienceLevel by remember { mutableStateOf("") }
  var anticipatedChallenges by remember { mutableStateOf("") }
  var focusArea by remember { mutableStateOf("") }
  var feedbackType by remember { mutableStateOf("") }

  val inputFields =
      listOf(
          InputFieldData(
              value = occasion,
              onValueChange = { occasion = it },
              question = "What is the occasion?",
              placeholder = "e.g., Conference, Seminar, Wedding",
              testTag = "occasionInput"),
          InputFieldData(
              value = purpose,
              onValueChange = { purpose = it },
              question = "What is the primary purpose of your speech?",
              placeholder = "Select purpose",
              testTag = "purposeInput",
              isDropdown = true,
              dropdownItems = listOf("Inform", "Persuade", "Inspire", "Entertain")),
          InputFieldData(
              value = audienceSize,
              onValueChange = { audienceSize = it },
              question = "What is the expected size of your audience?",
              placeholder = "Select audience size",
              testTag = "audienceSizeInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Small group (less than 20)",
                      "Medium group (20-50)",
                      "Large group (50-200)",
                      "Very large group (over 200)")),
          InputFieldData(
              value = demographic,
              onValueChange = { demographic = it },
              question = "What is your audience demographic?",
              placeholder = "e.g., College students, Professionals",
              testTag = "demographicInput"),
          InputFieldData(
              value = presentationStyle,
              onValueChange = { presentationStyle = it },
              question = "What presentation style will you be using?",
              placeholder = "Select style",
              testTag = "presentationStyleInput",
              isDropdown = true,
              dropdownItems = listOf("Formal", "Informal", "Storytelling", "Interactive")),
          InputFieldData(
              value = mainPoints,
              onValueChange = { mainPoints = it },
              question = "What are the key points or themes of your speech?",
              placeholder = "e.g., Innovation, Leadership, Teamwork",
              testTag = "mainPointsInput"),
          InputFieldData(
              value = visualAids,
              onValueChange = { visualAids = it },
              question = "Will you be using visual aids?",
              placeholder = "Select option",
              testTag = "visualAidsInput",
              isDropdown = true,
              dropdownItems = listOf("Yes", "No")),
          InputFieldData(
              value = experienceLevel,
              onValueChange = { experienceLevel = it },
              question = "What is your experience level with public speaking?",
              placeholder = "Select experience level",
              testTag = "experienceLevelInput",
              isDropdown = true,
              dropdownItems = listOf("Beginner", "Intermediate", "Advanced")),
          InputFieldData(
              value = anticipatedChallenges,
              onValueChange = { anticipatedChallenges = it },
              question = "What challenges or concerns do you have about this speech?",
              placeholder = "e.g., Nervousness, Audience engagement",
              testTag = "anticipatedChallengesInput"),
          InputFieldData(
              value = focusArea,
              onValueChange = { focusArea = it },
              question = "Which aspect would you like to focus on?",
              placeholder = "Select focus area",
              testTag = "focusAreaInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Content Structure",
                      "Delivery Style",
                      "Audience Engagement",
                      "Handling Q&A",
                      "Storytelling",
                      "Visual Aids Usage")),
          InputFieldData(
              value = feedbackType,
              onValueChange = { feedbackType = it },
              question = "What type of feedback would you like to receive?",
              placeholder = "Select feedback type",
              testTag = "feedbackTypeInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Verbal Delivery",
                      "Body Language",
                      "Vocal Variety",
                      "Clarity",
                      "Confidence",
                      "Use of Filler Words")))

  SpeakingPracticeModule(
      navigationActions = navigationActions,
      screenTitle = "Public Speaking",
      headerText = "Make your speech memorable",
      inputs = inputFields,
      onGetStarted = {
        // Create a PublicSpeakingContext object with the user's inputs
        val publicSpeakingContext =
            PublicSpeakingContext(
                occasion = occasion,
                purpose = purpose,
                audienceSize = audienceSize,
                audienceDemographic = demographic,
                presentationStyle = presentationStyle,
                mainPoints = mainPoints.split(",").map { it.trim() },
                experienceLevel = experienceLevel,
                anticipatedChallenges = anticipatedChallenges.split(",").map { it.trim() },
                focusArea = focusArea,
                feedbackType = feedbackType)

        apiLinkViewModel.updatePracticeContext(publicSpeakingContext)
        chatViewModel.initializeConversation()

        // Navigate to ChatScreen
        navigationActions.navigateTo(Screen.CHAT_SCREEN)
      })
}
