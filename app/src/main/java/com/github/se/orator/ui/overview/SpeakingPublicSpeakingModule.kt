package com.github.se.orator.ui.overview

import androidx.compose.runtime.*
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * Composable function that displays the Speaking Public Speaking module.
 *
 * @param navigationActions The navigation actions to be performed.
 * @param chatViewModel The view model for chat.
 * @param apiLinkViewModel The view model for API links.
 */
@Composable
fun SpeakingPublicSpeakingModule(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    apiLinkViewModel: ApiLinkViewModel
) {
  // State variables to hold user inputs
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

  // List of input fields for the Public Speaking module
  val inputFields =
      listOf(
          // Input field for the occasion of the speech (e.g., Conference, Seminar, Wedding)
          InputFieldData(
              value = occasion,
              onValueChange = { occasion = it },
              question = "What is the occasion?",
              placeholder = "e.g., Conference, Seminar, Wedding",
              testTag = "occasionInput"),
          // Dropdown input field for selecting the expected size of the audience
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
          // Input field for specifying the demographic of the audience (e.g., College students,
          // Professionals)
          InputFieldData(
              value = demographic,
              onValueChange = { demographic = it },
              question = "What is your audience demographic?",
              placeholder = "e.g., College students, Professionals",
              testTag = "demographicInput"),
          // Dropdown input field for selecting the aspect of the speech the user wants to focus on
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
                      "Visual Aids Usage")))

  /**
   * The SpeakingPracticeModule composable function displays the Public Speaking practice module.
   *
   * @param navigationActions The navigation actions to be performed.
   * @param screenTitle The title of the screen.
   * @param headerText The header text for the module.
   * @param inputs The list of input fields for the user to fill out.
   * @param onGetStarted The action to be performed when the user clicks on the Get Started button.
   */
  SpeakingPracticeModule(
      navigationActions = navigationActions,
      headerText = "Make your speech memorable",
      inputs = inputFields,
      onClick = {
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

        // Update the practice context in the API link view model
        apiLinkViewModel.updatePracticeContext(publicSpeakingContext)
        // Initialize the chat conversation in the chat view model
        chatViewModel.initializeConversation()

        // Navigate to the ChatScreen with the collected data
        navigationActions.navigateTo(Screen.CHAT_SCREEN)
      })
}
