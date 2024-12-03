package com.github.se.orator.ui.battle

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.overview.InputFieldData
import com.github.se.orator.ui.overview.SpeakingPracticeModule

/**
 * The BattleScreen composable is a screen where the user can initiate a battle with a friend.
 *
 * @param friendUid The UID of the friend to battle with.
 * @param userProfileViewModel ViewModel for managing user profile data.
 * @param navigationActions Actions to handle navigation within the app.
 */
@SuppressLint("SuspiciousIndentation")
@Composable
fun BattleScreen(
    friendUid: String,
    userProfileViewModel: UserProfileViewModel,
    navigationActions: NavigationActions,
    battleViewModel: BattleViewModel
) {

  val friendName = userProfileViewModel.getName(friendUid)
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
              label = "For which position are you challenging $friendName?",
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

  // Use the existing SpeakingPracticeModule for consistent UI
  SpeakingPracticeModule(
      navigationActions = navigationActions,
      screenTitle = "Interview battle with $friendName",
      headerText = "Challenge $friendName to an interview battle!",
      inputs = inputFields,
      onClick = {
        // Simulate sending battle request
        // TODO: Implement actual battle request logic

        // Collect the inputs and create an InterviewContext object
        val interviewContext =
            InterviewContext(
                interviewType = "job interview",
                role = targetPosition,
                company = companyName,
                focusAreas = skills.split(",").map { it.trim() })

        // Create the battle speech instance
        val battleId = battleViewModel.createBattleRequest(friendUid, interviewContext)

        // Navigate to the BattleRequestSentScreen
        if (battleId != null) {
          navigationActions.navigateToBattleRequestSentScreen(friendUid, battleId)
        }
      },
      buttonName = "Send Battle Request")
}
