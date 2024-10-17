package com.github.se.orator.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackScreen() {
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.Top) {
              // Header Title and Subtitle
              Text(
                  text = "Your Feedback",
                  style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                  modifier = Modifier.padding(bottom = 8.dp))
              Text(
                  text = "Here's what you did well and where you can improve.",
                  style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                  modifier = Modifier.padding(bottom = 24.dp))

              // Strengths Section
              Text(
                  text = "What You Did Well",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                  modifier = Modifier.padding(vertical = 8.dp))
              Text(
                  text = "Strength", // ChatGPT's generated strengths
                  style = TextStyle(fontSize = 16.sp),
                  modifier = Modifier.padding(bottom = 16.dp))

              // Areas for Improvement
              Text(
                  text = "Areas for Improvement",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                  modifier = Modifier.padding(vertical = 8.dp))
              Text(
                  text = "Improvements", // ChatGPT's generated improvements
                  style = TextStyle(fontSize = 16.sp),
                  modifier = Modifier.padding(bottom = 24.dp))

              // Tips Section
              Text(
                  text = "Tips to Improve",
                  style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                  modifier = Modifier.padding(vertical = 8.dp))
              Text(
                  text = "Tips", // ChatGPT's actionable tips
                  style = TextStyle(fontSize = 16.sp),
                  modifier = Modifier.padding(bottom = 32.dp))

              // Buttons for Retry and Review
              Row(
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Button(onClick = { /* Retry logic */}) { Text("Try Again") }
                    Button(onClick = { /* Review logic */}) { Text("Review Conversation") }
                  }
            }
      })
}
