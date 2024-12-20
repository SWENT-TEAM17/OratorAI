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
import com.github.se.orator.ui.theme.AppDimensions

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
  var interviewType by remember { mutableStateOf("") }
  var experienceLevel by remember { mutableStateOf("") }
  var jobDescription by remember { mutableStateOf("") }
  var focusArea by remember { mutableStateOf("") }

  val inputFields =
      listOf(
          InputFieldData(
              value = targetPosition,
              onValueChange = { targetPosition = it },
              question = "What is your target job position?",
              placeholder = "e.g., Senior Executive",
              testTag = "targetPositionInput"),
          InputFieldData(
              value = companyName,
              onValueChange = { companyName = it },
              question = "Which company are you applying to?",
              placeholder = "e.g., McKinsey",
              testTag = "companyNameInput"),
          InputFieldData(
              value = interviewType,
              onValueChange = { interviewType = it },
              question = "What type of interview are you preparing for?",
              placeholder = "Select interview type",
              testTag = "interviewTypeInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Phone Interview",
                      "Video Interview",
                      "In-Person Interview",
                      "Panel Interview",
                      "Group Interview",
                      "Assessment Center")),
          InputFieldData(
              value = experienceLevel,
              onValueChange = { experienceLevel = it },
              question = "What is your experience level in this field?",
              placeholder = "Select experience level",
              testTag = "experienceLevelInput",
              isDropdown = true,
              dropdownItems =
                  listOf("Entry-Level", "Mid-Level", "Senior-Level", "Executive-Level")),
          InputFieldData(
              value = jobDescription,
              onValueChange = { jobDescription = it },
              question = "Please provide the job description:",
              placeholder = "Paste the job description here",
              testTag = "jobDescriptionInput",
              isScrollable = true,
              height = AppDimensions.jobDescriptionInputFieldHeight),
          InputFieldData(
              value = focusArea,
              onValueChange = { focusArea = it },
              question = "What do you want to focus on the most?",
              placeholder = "Select focus area",
              testTag = "focusAreaInput",
              isDropdown = true,
              dropdownItems =
                  listOf(
                      "Behavioral Questions",
                      "Technical Questions",
                      "Situational Questions",
                      "Competency-Based Questions",
                      "Case Studies",
                      "Company-Specific Questions")),
      )

  // Use the existing SpeakingPracticeModule for consistent UI
  SpeakingPracticeModule(
      navigationActions = navigationActions,
      headerText = "Challenge $friendName to an interview battle!",
      inputs = inputFields,
      onClick = {

        // Collect the inputs and create an InterviewContext object
        val interviewContext =
            InterviewContext(
                targetPosition = targetPosition,
                companyName = companyName,
                interviewType = interviewType,
                experienceLevel = experienceLevel,
                jobDescription = jobDescription,
                focusArea = focusArea,
            )

        // Create the battle speech instance
        val battleId = battleViewModel.createBattleRequest(friendUid, interviewContext)

        // Navigate to the BattleRequestSentScreen
        if (battleId != null) {
          navigationActions.navigateToBattleRequestSentScreen(friendUid, battleId)
        }
      },
      buttonName = "Send Battle Request")
}
