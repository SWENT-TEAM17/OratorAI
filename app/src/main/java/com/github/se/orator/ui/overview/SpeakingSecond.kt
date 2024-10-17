package com.github.se.orator.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpeakingSecond() {
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement =
                Arrangement.SpaceBetween // Ensures elements are spaced from top to bottom
            ) {
              // Chat Message from ChatGPT
              Column(
                  modifier =
                      Modifier.weight(1f), // This makes the chat take as much space as possible
                  verticalArrangement = Arrangement.Top) {
                    ChatBubble(
                        message =
                            "What is your current level of education and what kind of job are you applying for?")
                  }

              // Microphone Button
              Row(
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.fillMaxWidth().padding(2.dp)) {
                    IconButton(
                        onClick = {
                          // No action yet, just UI
                        },
                        modifier =
                            Modifier.size(60.dp) // Set the size of the button
                                .background(
                                    color = Color.Gray,
                                    shape = CircleShape // Circle shape for the mic button
                                    )) {
                          Icon(
                              imageVector = Icons.Filled.PlayArrow,
                              contentDescription = "Mic Icon",
                              tint = Color.White, // White color for the mic icon
                              modifier = Modifier.size(32.dp) // Icon size
                              )
                        }
                  }

              // Feedback Button at the Bottom
              Button(
                  onClick = {
                    // No action yet
                  },
                  modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Text("Feedback")
                  }
            }
      })
}

// Chat bubble composable for ChatGPT message
@Composable
fun ChatBubble(message: String) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .padding(8.dp)
              .background(
                  color = Color(0xFFF2F2F2), // Light grey for the bubble
                  shape = RoundedCornerShape(8.dp) // Rounded corners for the bubble
                  )
              .padding(16.dp)) {
        Text(text = message, color = Color.Black, style = TextStyle(fontSize = 16.sp))
      }
}
