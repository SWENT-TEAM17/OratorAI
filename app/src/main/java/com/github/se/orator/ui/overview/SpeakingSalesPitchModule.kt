package com.github.se.orator.ui.overview

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.navigation.NavigationActions

/**
 * The SpeakingSalesPitchModule composable is a composable screen that displays the sales pitch
 * module.
 *
 * @param navigationActions The navigation actions that can be performed.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun SpeakingSalesPitchModule(navigationActions: NavigationActions) {
  var productType by remember { mutableStateOf("") }
  var targetAudience by remember { mutableStateOf("") }
  var feedbackType by remember { mutableStateOf("") }
  var keySellingPoints by remember { mutableStateOf("") }
    var feedbackLanguage by remember { mutableStateOf("") }

  val inputFields =
      listOf(
          InputFieldData(
              value = productType,
              onValueChange = { newValue: String ->
                productType = newValue
              }, // Explicitly specify String
              label = "What product or service are you pitching?",
              placeholder = "e.g Marketing services",
              testTag = "productTypeInput"),
          InputFieldData(
              value = targetAudience,
              onValueChange = { newValue: String ->
                targetAudience = newValue
              }, // Explicitly specify String
              label = "What is the target audience?",
              placeholder = "e.g Investors",
              testTag = "targetAudienceInput",
              height = 200),
          InputFieldData(
              value = feedbackType,
              onValueChange = { newValue: String ->
                feedbackType = newValue
              }, // Explicitly specify String
              label = "Do you want feedback on persuasive language, volume or delivery?",
              placeholder = "e.g Delivery",
              height = 85,
              testTag = "feedbackTypeInput"),
          InputFieldData(
              value = keySellingPoints,
              onValueChange = { newValue: String ->
                keySellingPoints = newValue
              }, // Explicitly specify String
              label = "What key selling points do you want to emphasize?",
              placeholder = "e.g Price",
              height = 85,
              testTag = "keySellingPointsInput"))

    SpeakingPracticeModule(
        navigationActions = navigationActions,
        screenTitle = "Public Speaking",
        headerText = "Make your speech memorable",
        inputs = inputFields,
        onGetStarted = {
            // Create a PublicSpeakingContext object with the user's inputs
            val salesPitchContext = SalesPitchContext(
                product = productType,
                targetAudience = targetAudience,
                keyFeatures = keySellingPoints.split(", ")
            )

            // Navigate to ChatScreen, passing the context and feedbackLanguage
            navigationActions.navigateToChatScreen(salesPitchContext, feedbackLanguage)
        }
    )
}
