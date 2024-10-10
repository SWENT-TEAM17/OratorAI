package com.github.se.orator.ui.overview

import android.icu.util.GregorianCalendar
import android.text.Layout
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp


@Composable
fun SpeakingStart() {
    var level by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.testTag("addScreen"),
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Text(
                    text = "Start a new speaking session",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(16.dp))

                Spacer(modifier = Modifier.height(45.dp))
                // Title Input
                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("What is your level of education ?") },
                    placeholder = { Text("e.g Undergraduate") },
                    modifier = Modifier.fillMaxWidth())

                // Description Input
                OutlinedTextField(
                    value = job,
                    onValueChange = { job = it },
                    label = { Text("What kind of job are you applying for ?") },
                    placeholder = { Text("e.g Consulting") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp))

                // Time Input
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("How long do you want the interview to last?") },
                    placeholder = { Text("e.g 15 min") },
                    modifier = Modifier.fillMaxWidth())

                // Experience Input
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("How many years of experience do you have ?") },
                    placeholder = { Text("e.g 5 years") },
                    modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(130.dp))

                // Get Started Button
                Button(
                    modifier = Modifier.fillMaxWidth(),
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

