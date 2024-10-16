package com.github.se.orator.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp


@Composable
fun SpeakingJobInterviewModule() {
    var target_position by remember { mutableStateOf("") }
    var company_name by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var feedback_type by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.testTag("speakingStartScreen"),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Ace your next job interview",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("titleText")
                )

                Spacer(modifier = Modifier.height(45.dp))

                // Level Input
                OutlinedTextField(
                    value = target_position,
                    onValueChange = { target_position = it },
                    label = { Text("What is your target job position ?") },
                    placeholder = { Text("e.g Senior executive") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("levelInput")
                )

                // Job Input
                OutlinedTextField(
                    value = company_name,
                    onValueChange = { company_name = it },
                    label = { Text("Which company are you applying to ?") },
                    placeholder = { Text("e.g McKinsey") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("jobInput")
                )

                // Time Input
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("What skills or qualifications do you want to highlight ?") },
                    placeholder = { Text("e.g Problem solving") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeInput")
                )

                // Experience Input
                OutlinedTextField(
                    value = feedback_type,
                    onValueChange = { feedback_type = it },
                    label = { Text("Do you want feedback on persuasive language, volume, or delivery ?") },
                    placeholder = { Text("e.g Persuasive language") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("experienceInput")
                )

                Spacer(modifier = Modifier.height(130.dp))

                // Get Started Button
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("getStartedButton"),
                    onClick = {
                        // Leave the onClick action empty for now
                    }
                ) {
                    Text("Get Started")
                }
            }
        }
    )
}

