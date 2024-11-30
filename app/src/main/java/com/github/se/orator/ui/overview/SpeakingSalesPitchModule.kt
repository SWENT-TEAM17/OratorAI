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
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun SpeakingSalesPitchModule(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    apiLinkViewModel: ApiLinkViewModel
) {
  var productType by remember { mutableStateOf("") }
  var targetAudience by remember { mutableStateOf("") }
  var salesGoal by remember { mutableStateOf("") }
  var keySellingPoints by remember { mutableStateOf("") }
  var anticipatedChallenges by remember { mutableStateOf("") }
  var negotiationFocus by remember { mutableStateOf("") }
  var feedbackType by remember { mutableStateOf("") }

  val inputFields =
      listOf(
          InputFieldData(
              value = productType,
              onValueChange = { productType = it },
              question = "What product or service are you pitching?",
              placeholder = "e.g., Marketing services",
              testTag = "productTypeInput"),
          InputFieldData(
              value = targetAudience,
              onValueChange = { targetAudience = it },
              question = "Who is your target audience?",
              placeholder = "e.g., Investors",
              testTag = "targetAudienceInput"),
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
          InputFieldData(
              value = keySellingPoints,
              onValueChange = { keySellingPoints = it },
              question = "What key selling points do you want to emphasize?",
              placeholder = "e.g., Price, Quality, Innovation",
              testTag = "keySellingPointsInput"),
          InputFieldData(
              value = anticipatedChallenges,
              onValueChange = { anticipatedChallenges = it },
              question = "What challenges or objections do you anticipate from your audience?",
              placeholder = "e.g., Budget constraints, Competition",
              testTag = "anticipatedChallengesInput"),
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

  SpeakingPracticeModule(
      navigationActions = navigationActions,
      screenTitle = "Sales Pitch",
      headerText = "Master your sales pitch and negotiation skills",
      inputs = inputFields,
      onGetStarted = {
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

        apiLinkViewModel.updatePracticeContext(salesPitchContext)
        chatViewModel.initializeConversation()

        // Navigate to ChatScreen
        navigationActions.navigateTo(Screen.CHAT_SCREEN)
      })
}
