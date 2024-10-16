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
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
fun SpeakingSalesPitchModule(navigationActions: NavigationActions) {
    var type by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var feedback_type by remember { mutableStateOf("") }
    var key_points by remember { mutableStateOf("") }

    androidx.compose.material.Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feedbackScreen"),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 4.dp,
                title = { Text("Sales Pitch") },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
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
                    text = "Become a master at sales pitch",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("titleText")
                )

                Spacer(modifier = Modifier.height(45.dp))

                // Level Input
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("What product or service are you pitching ?") },
                    placeholder = { Text("e.g Marketing services") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("levelInput")
                )

                // Job Input
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("What is the target audience ?") },
                    placeholder = { Text("e.g Investors") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("jobInput")
                )

                // Time Input
                OutlinedTextField(
                    value = feedback_type,
                    onValueChange = { feedback_type = it },
                    label = { Text("Do you want feedback on persuasive language, volume or delivery?") },
                    placeholder = { Text("e.g Delivery") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeInput")
                )

                // Experience Input
                OutlinedTextField(
                    value = key_points,
                    onValueChange = { key_points = it },
                    label = { Text("What key selling points do you want to emphasize ?") },
                    placeholder = { Text("e.g Price") },
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