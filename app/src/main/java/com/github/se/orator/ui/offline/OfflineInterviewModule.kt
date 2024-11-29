package com.github.se.orator.ui.overview

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppDimensionsObject
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.createAppDimensions
import savePromptsToFile
import kotlin.random.Random

fun generateRandomString(length: Int = 8): String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { charset[Random.nextInt(charset.length)] }
        .joinToString("")
}

/**
 * The SpeakingJobInterviewModule composable is a composable screen that displays the job interview
 * module.
 *
 * @param navigationActions The navigation actions that can be performed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SuspiciousIndentation")
@Composable
fun OfflineInterviewModule(
    context: Context,
    navigationActions: NavigationActions,
    speakingViewModel: SpeakingViewModel
) {
    var targetCompany by remember { mutableStateOf("") }
    var jobPosition by remember { mutableStateOf("") }
    val ID = generateRandomString()

    val dimensions: AppDimensionsObject = createAppDimensions()

    val inputFields =
        listOf(
            InputFieldData(
                value = targetCompany,
                onValueChange = { newValue: String ->
                    targetCompany = newValue
                },
                label = "What company are you applying to?",
                placeholder = "e.g Apple, Google",
                testTag = "company"),
            InputFieldData(
                value = jobPosition,
                onValueChange = { newValue: String ->
                    jobPosition = newValue
                },
                label = "What job position are you applying for?",
                placeholder = "e.g Hardware engineer",
                height = 85,
                testTag = "skillsInput"))


    Column(
        modifier =
        Modifier.fillMaxSize()
            .padding(horizontal = dimensions.paddingMedium)
            .padding(top = dimensions.paddingSmall)
            .verticalScroll(rememberScrollState())
            .testTag("content"),
        verticalArrangement = Arrangement.Center) {
        inputFields.forEach { input ->
            OutlinedTextField(
                value = input.value,
                onValueChange = input.onValueChange,
                label = { Text(input.label, color = AppColors.textColor) },
                placeholder = { Text(input.placeholder, color = AppColors.textColor) },
                modifier =
                Modifier.fillMaxWidth()
                    .height(
                        input.height.dp) // Assuming 'height' is defined in InputFieldData
                    .testTag(input.testTag),
                colors =
                TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = AppColors.primaryColor,
                    unfocusedBorderColor = AppColors.textColor,
                    cursorColor = AppColors.primaryColor,
                    focusedLabelColor = AppColors.primaryColor,
                    unfocusedLabelColor = AppColors.textColor))
        }
        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

        Button(
            onClick = {
                savePromptsToFile(
                    context = context,
                    prompts = mapOf(
                        "targetCompany" to targetCompany,
                        "jobPosition" to jobPosition,
                        "ID" to ID
                    ),
                )
                speakingViewModel.interviewPromptNb.value = ID
                navigationActions.navigateTo(Screen.PRACTICE_QUESTIONS_SCREEN)
            },
            modifier =
            Modifier.fillMaxWidth(0.8f)
                .padding(AppDimensions.paddingSmall)
                .testTag("DoneButton").align(Alignment.CenterHorizontally),

            colors = ButtonDefaults.buttonColors(containerColor = colors.primary)) {
            Text(
                text = "Go to questions screen",
                fontSize = AppFontSizes.buttonText,
                color = colors.onPrimary)
        }

    }

}