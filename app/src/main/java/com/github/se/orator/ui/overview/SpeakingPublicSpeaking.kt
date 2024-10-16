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


@Composable
fun SpeakingPublicSpeaking(navigationActions: NavigationActions) {
    var occasion by remember { mutableStateOf("") }
    var demographic by remember { mutableStateOf("") }
    var main_points by remember { mutableStateOf("") }
    var feedback_language by remember { mutableStateOf("") }

    androidx.compose.material.Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feedbackScreen"),
        topBar = {
            androidx.compose.material.TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 4.dp,
                title = { Text("Public Speaking") },
                navigationIcon = {
                    androidx.compose.material.IconButton(onClick = { navigationActions.goBack() }) {
                        Image(
                            painter = painterResource(id = R.drawable.back_arrow),
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("back_button")
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Make your speech memorable",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("titleText")
                )

                Spacer(modifier = Modifier.height(45.dp))

                // Level Input
                OutlinedTextField(
                    value = occasion,
                    onValueChange = { occasion = it },
                    label = { Text("What is the occasion ?") },
                    placeholder = { Text("e.g Conference") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("levelInput")
                )

                // Job Input
                OutlinedTextField(
                    value = demographic,
                    onValueChange = { demographic = it },
                    label = { Text("What is your audience demographic ?") },
                    placeholder = { Text("e.g PHDs") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("jobInput")
                )

                // Time Input
                OutlinedTextField(
                    value = main_points,
                    onValueChange = { main_points = it },
                    label = { Text("What are the key points or themes of your speech ?") },
                    placeholder = { Text("e.g AI, Machine Learning") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeInput")
                )

                // Experience Input
                OutlinedTextField(
                    value = feedback_language,
                    onValueChange = { feedback_language = it },
                    label = { Text("Do you want suggestion on intonation or body language ?") },
                    placeholder = { Text("e.g Intonation") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("experienceInput")
                )

                // Get Started Button
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("getStartedButton"),
                    onClick = {
                        // Leave the onClick action empty for now
                        navigationActions.navigateTo(Screen.SPEAKING_SCREEN)
                    }
                ) {
                    Text("Get Started")
                }
            }
        }
    )
}


