package com.github.se.orator.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * The SpeakingJobInterviewModule composable is a composable screen that displays the job interview module.
 *
 * @param navigationActions The navigation actions that can be performed.
 */
@Composable
fun SpeakingJobInterviewModule(navigationActions: NavigationActions) {
    var targetPosition by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf("") }

    val inputFields = listOf(
        InputFieldData(
            value = targetPosition,
            onValueChange = { newValue: String -> targetPosition = newValue }, // Explicitly specify String for newValue
            label = "What is your target job position?",
            placeholder = "e.g Senior executive",
            testTag = "levelInput"
        ),
        InputFieldData(
            value = companyName,
            onValueChange = { newValue: String -> companyName = newValue }, // Explicitly specify String
            label = "Which company are you applying to?",
            placeholder = "e.g McKinsey",
            testTag = "jobInput",
            height = 200
        ),
        InputFieldData(
            value = skills,
            onValueChange = { newValue: String -> skills = newValue }, // Explicitly specify String
            label = "What skills or qualifications do you want to highlight?",
            placeholder = "e.g Problem solving",
            height = 85,
            testTag = "timeInput"
        ),
        InputFieldData(
            value = feedbackType,
            onValueChange = { newValue: String -> feedbackType = newValue }, // Explicitly specify String
            label = "Do you want feedback on persuasive language, volume, or delivery?",
            placeholder = "e.g Persuasive language",
            height = 85,
            testTag = "experienceInput"
        )
    )

    SpeakingPracticeModule(
        navigationActions = navigationActions,
        screenTitle = "Job Interview",
        headerText = "Ace your next job interview",
        inputs = inputFields
    )
}


