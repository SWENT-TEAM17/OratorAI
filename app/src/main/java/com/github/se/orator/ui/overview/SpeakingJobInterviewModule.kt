package com.github.se.orator.ui.overview

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions


/**
 * Composable function that displays the Speaking Job Interview module.
 *
 * @param navigationActions The navigation actions to be performed.
 * @param chatViewModel The view model for chat.
 * @param apiLinkViewModel The view model for API links.
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

    /**
     * Composable function that displays the Speaking Practice module for job interviews.
     *
     * @param navigationActions The navigation actions to be performed.
     * @param chatViewModel The view model for chat.
     * @param apiLinkViewModel The view model for API links.
     */
    SpeakingPracticeModule(
        navigationActions = navigationActions,
        screenTitle = "Job Interview",
        headerText = "Ace your next job interview",
        inputs = inputFields,
        onGetStarted = {
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

            apiLinkViewModel.updatePracticeContext(interviewContext)
            chatViewModel.initializeConversation()

            // Navigate to ChatScreen with the collected data
            navigationActions.navigateTo(Screen.CHAT_SCREEN)
        })
}
