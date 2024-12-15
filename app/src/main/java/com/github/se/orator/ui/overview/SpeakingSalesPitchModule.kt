package com.github.se.orator.ui.overview

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * The SpeakingSalesPitchModule composable displays the sales pitch module.
 *
 * @param navigationActions The navigation actions that can be performed.
 * @param chatViewModel The view model for chat.
 * @param apiLinkViewModel The view model for API links.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun SpeakingSalesPitchModule(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    apiLinkViewModel: ApiLinkViewModel
) {
  // State variables to hold user inputs
  var productType by remember { mutableStateOf("") }
  var targetAudience by remember { mutableStateOf("") }
  var salesGoal by remember { mutableStateOf("") }
  var keySellingPoints by remember { mutableStateOf("") }
  var anticipatedChallenges by remember { mutableStateOf("") }
  var negotiationFocus by remember { mutableStateOf("") }
  var feedbackType by remember { mutableStateOf("") }

  // List of input fields for the Sales Pitch module
  val inputFields =
      listOf(
          // Input field for the product or service being pitched
          InputFieldData(
              value = productType,
              onValueChange = { productType = it },
              question = "What product or service are you pitching?",
              placeholder = "e.g., Marketing services",
              testTag = "productTypeInput"),
          // Input field for identifying the target audience of the pitch
          InputFieldData(
              value = targetAudience,
              onValueChange = { targetAudience = it },
              question = "Who is your target audience?",
              placeholder = "e.g., Investors",
              testTag = "targetAudienceInput"),
          // Dropdown input field for selecting the primary goal of the sales pitch
          InputFieldData(
              value = salesGoal,
              onValueChange = { salesGoal = it },
              question = "What is your primary goal for this sales pitch?",
              placeholder = "Select sales goal",
              testTag = "salesGoalInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Close the deal",
                      "Generate interest",
                      "Build relationships",
                      "Secure a follow-up meeting")),
          // Input field for outlining key selling points to emphasize during the pitch
          InputFieldData(
              value = keySellingPoints,
              onValueChange = { keySellingPoints = it },
              question = "What key selling points do you want to emphasize?",
              placeholder = "e.g., Price, Quality, Innovation",
              testTag = "keySellingPointsInput"),
          // Input field for anticipating potential challenges or objections from the audience
          InputFieldData(
              value = anticipatedChallenges,
              onValueChange = { anticipatedChallenges = it },
              question = "What challenges or objections do you anticipate from your audience?",
              placeholder = "e.g., Budget constraints, Competition",
              testTag = "anticipatedChallengesInput"),
          // Dropdown input field for selecting specific negotiation skills to focus on
          InputFieldData(
              value = negotiationFocus,
              onValueChange = { negotiationFocus = it },
              question = "Which negotiation skills would you like to focus on?",
              placeholder = "Select focus area",
              testTag = "negotiationFocusInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Handling Objections",
                      "Price Negotiation",
                      "Building Rapport",
                      "Active Listening",
                      "Closing Techniques",
                      "Questioning Strategies")),
          // Dropdown input field for specifying the aspect of the pitch the user wants feedback on
          InputFieldData(
              value = feedbackType,
              onValueChange = { feedbackType = it },
              question = "Which aspect would you like feedback on?",
              placeholder = "Select feedback type",
              testTag = "feedbackTypeInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Persuasive Language",
                      "Volume",
                      "Delivery",
                      "Confidence",
                      "Clarity",
                      "Body Language")))

  /**
   * The SpeakingPracticeModule composable function displays the Sales Pitch practice module.
   *
   * @param navigationActions The navigation actions to be performed.
   * @param screenTitle The title of the screen.
   * @param headerText The header text for the module.
   * @param inputs The list of input fields for the user to fill out.
   * @param onGetStarted The action to be performed when the user clicks on the Get Started button.
   */
  SpeakingPracticeModule(
      navigationActions = navigationActions,
      headerText = "Master your sales pitch and negotiation skills",
      inputs = inputFields,
      onClick = {
        // Create a SalesPitchContext object with the user's inputs
        val salesPitchContext =
            SalesPitchContext(
                product = productType,
                targetAudience = targetAudience,
                salesGoal = salesGoal,
                keyFeatures = keySellingPoints.split(",").map { it.trim() },
                anticipatedChallenges = anticipatedChallenges.split(",").map { it.trim() },
                negotiationFocus = negotiationFocus,
                feedbackType = feedbackType)

        // Update the practice context in the API link view model
        apiLinkViewModel.updatePracticeContext(salesPitchContext)
        // Initialize the chat conversation in the chat view model
        chatViewModel.initializeConversation()

        // Navigate to the ChatScreen with the collected data
        navigationActions.navigateTo(Screen.CHAT_SCREEN)
      })
}
