package com.github.se.orator.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

@Composable
fun OfflinePracticeQuestionsScreen(navigationActions: NavigationActions) {
    val colors = MaterialTheme.colorScheme

    // List of questions to display as practice options
    val questions = listOf(
        "What are your strengths?",
        "Describe a challenging situation you've faced.",
        "Why do you want this position?",
        "Tell me about a time you demonstrated leadership.",
        "How do you handle conflict in a team?"
    )

    // Main container column for layout, centered horizontally and taking full screen height
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 16.dp)
            .testTag("OfflinePracticeQuestionsScreen"), // Test tag for identifying this screen
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row container for back button and title, positioned at the top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button icon, navigates back to Offline screen
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back", // Accessibility description
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navigationActions.navigateTo(Screen.OFFLINE) } // Go back to Offline screen
                    .background(colors.background, CircleShape)
                    .padding(4.dp)
                    .testTag("BackButton"), // Test tag for the back button
                tint = colors.primary // Primary color from the theme for the icon
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title text for the screen, styled to be bold and centered vertically
            Text(
                text = "Choose your practice question",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground, // Color from theme
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .testTag("TitleText") // Test tag for title text
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display each question in a Card with clickable functionality
        questions.forEachIndexed { index, question ->
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        // Navigate to OfflineRecordingScreen with the selected question
                        navigationActions.goToOfflineRecording(question)
                    }
                    .testTag("QuestionCard_$index"), // Test tag for each question card
                colors = CardDefaults.cardColors(containerColor = colors.surface) // Surface color for card background
            ) {
                // Box container for the question text inside each card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Display the question text inside the card
                    Text(
                        text = question,
                        fontSize = 16.sp,
                        color = colors.secondary, // Secondary color for text
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("QuestionText_$index") // Test tag for each question text
                    )
                }
            }
        }
    }
}
