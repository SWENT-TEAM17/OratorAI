package com.github.se.orator.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.se.orator.R
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations

/**
 * The FeedbackScreen composable is a composable screen that displays the feedback screen.
 *
 * @param chatViewModel The view model for the chat.
 * @param navController The navigation controller.
 * @param navigationActions The navigation actions that can be performed.
 */
@Composable
fun FeedbackScreen(
    chatViewModel: ChatViewModel,
    navController: NavHostController,
    navigationActions: NavigationActions
) {
  // State variables
  var feedbackMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Trigger feedback generation when the screen is displayed
  LaunchedEffect(Unit) {
    try {
      feedbackMessage = chatViewModel.generateFeedback()
    } catch (e: Exception) {
      errorMessage = e.localizedMessage
    } finally {
      isLoading = false
    }
  }

  // UI
  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("feedbackScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = Color.White,
            contentColor = Color.Black,
            elevation = 4.dp,
            title = { Text("Feedback") },
            navigationIcon = {
              IconButton(onClick = { navController.popBackStack() }) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp).testTag("back_button"))
              }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.Top) {
              // Header Title and Subtitle
              Text(
                  text = "Your Feedback",
                  style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                  modifier = Modifier.padding(bottom = 8.dp).testTag("feedbackTitle"))
              Text(
                  text = "Here's what you did well and where you can improve.",
                  style = TextStyle(fontSize = 16.sp, color = Color.Gray),
                  modifier = Modifier.padding(bottom = 24.dp).testTag("feedbackSubtitle"))

              if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
              } else if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
              } else if (feedbackMessage != null) {
                // Display the feedback message
                Text(
                    text = feedbackMessage!!,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 16.dp).testTag("feedbackContent"))
              } else {
                Text(
                    text = "No feedback available.",
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 16.dp).testTag("feedbackContent"))
              }

              // Buttons for Retry and Review
              Row(
                  horizontalArrangement = Arrangement.SpaceEvenly,
                  modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Button(
                        onClick = {
                          // Use navigationActions to navigate to Home
                          navigationActions.navigateTo(TopLevelDestinations.HOME)
                        },
                        modifier = Modifier.testTag("retryButton")) {
                          Text("Try Again")
                        }
                    //                    Button(
                    //                        onClick = {
                    //                            val practiceContextJson =
                    // Uri.encode(Gson().toJson(chatViewModel.practiceContext))
                    //                            val feedbackType = chatViewModel.feedbackType
                    //
                    //                            if (practiceContextJson != null &&
                    // feedbackType.isNotEmpty()) {
                    //
                    // navController.navigate("${Screen.CHAT_SCREEN}/$practiceContextJson/$feedbackType") {
                    //                                    // Remove the current FeedbackScreen from
                    // the back stack
                    //                                    popUpTo(Screen.CHAT_SCREEN) { inclusive =
                    // true }
                    //                                }
                    //                            } else {
                    //                                Log.e("FeedbackScreen", "practiceContext or
                    // feedbackType is null")
                    //                            }
                    //                        },
                    //                        modifier = Modifier.testTag("reviewButton")
                    //                    ) {
                    //                        Text("Review Conversation")
                    //                    }
                  }
            }
      })
}
