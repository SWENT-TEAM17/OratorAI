package com.github.se.orator.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.orator.R
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.network.Message

/**
 * The ChatScreen composable is a composable screen that displays the chat screen.
 *
 * @param navigationActions The navigation actions that can be performed.
 * @param navController The navigation controller.
 * @param viewModel The view model for the chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navigationActions: NavigationActions,
    navController: NavHostController,
    viewModel: ChatViewModel
) {
  val chatMessages by viewModel.chatMessages.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()

  val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
  val transcribedText = savedStateHandle?.get<String>("transcribedText")

  LaunchedEffect(transcribedText) {
    if (!transcribedText.isNullOrBlank()) {
      val analysisData =
          AnalysisData(fillerWordsCount = 0, averagePauseDuration = 0.0, sentimentScore = 0.0)
      viewModel.sendUserResponse(transcribedText, analysisData)
      savedStateHandle.remove<String>("transcribedText")
    }
  }
  // State for tracking the scroll position
  val listState = rememberLazyListState()

  // Auto-scroll to the last message when the chatMessages size changes
  LaunchedEffect(chatMessages.size) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            backgroundColor = Color.White,
            contentColor = Color.Black,
            elevation = 4.dp,
            title = { Text(text = "Chat Screen", fontWeight = FontWeight.Bold) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp).testTag("back_button"))
              }
            },
        )
      },
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
          // Display chat messages using LazyColumn
          LazyColumn(
              state = listState,
              modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 8.dp)) {
                items(chatMessages.size) { index ->
                  val message = chatMessages[index]
                  ChatMessageItem(message)
                }
              }

          // Display a loading indicator when fetching a response
          if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
          }

          Button(
              onClick = { navigationActions.navigateToSpeakingScreen() },
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
              enabled = !isLoading) {
                Text(text = "Record Response")
              }

          // User input text field and send button (commented out in your original code)
          /*
          OutlinedTextField(
              value = userInput,
              onValueChange = { userInput = it },
              modifier = Modifier.fillMaxWidth(),
              placeholder = { Text(text = "Type your message...") },
              singleLine = true
          )

          Button(
              onClick = {
                  // Create an AnalysisData object (you can replace with actual data)
                  val analysisData = AnalysisData(
                      fillerWordsCount = 0,
                      averagePauseDuration = 0.0,
                      sentimentScore = 0.0
                      // Add other fields as necessary
                  )
                  // Send the user response
                  viewModel.sendUserResponse(userInput.text, analysisData)
                  userInput = TextFieldValue("") // Clear input after sending
              },
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
              enabled = userInput.text.isNotBlank() && !isLoading
          ) {
              Text(text = "Send")
          }
          */

          Button(
              onClick = {
                // Navigate to FeedbackScreen and pass necessary data
                navigationActions.navigateToFeedbackScreen()
              },
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(text = "Request Feedback")
              }
        }
      })
}

@Composable
fun ChatMessageItem(message: Message) {
  val backgroundColor = if (message.role == "user") Color(0xFFE8EAF6) else Color(0xFFE1F5FE)
  val alignment = if (message.role == "user") Alignment.End else Alignment.Start

  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start) {
        Box(
            modifier =
                Modifier.background(backgroundColor, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)) {
              Text(text = message.content, color = Color.Black)
            }
      }
}
