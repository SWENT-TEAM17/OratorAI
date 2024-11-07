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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.se.orator.R
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

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
            modifier = Modifier.testTag("chat_screen_app_bar"),
            backgroundColor = AppColors.surfaceColor,
            contentColor = AppColors.textColor,
            elevation = AppDimensions.appBarElevation,
            title = { Text(text = "Chat Screen", style = AppTypography.appBarTitleStyle) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Back",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeSmall).testTag("back_button"))
                  }
            },
        )
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(AppDimensions.paddingMedium)
                    .testTag("chat_screen_content")) {
              // Display chat messages using LazyColumn
              LazyColumn(
                  state = listState,
                  modifier =
                      Modifier.weight(1f)
                          .fillMaxWidth()
                          .padding(bottom = AppDimensions.paddingSmall)
                          .testTag("chat_messages")) {
                    items(chatMessages.size) { index ->
                      val message = chatMessages[index]
                      ChatMessageItem(message)
                    }
                  }

              // Display a loading indicator when fetching a response
              if (isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.align(Alignment.CenterHorizontally)
                            .padding(AppDimensions.paddingSmall)
                            .testTag("loading_indicator"))
              }

              Button(
                  onClick = { navigationActions.navigateToSpeakingScreen() },
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = AppDimensions.paddingSmall)
                          .testTag("record_response_button"),
                  enabled = !isLoading) {
                    Text(
                        text = "Record Response",
                        modifier = Modifier.testTag("record_response_button_text"))
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
                      .padding(top = AppDimensions.paddingSmall),
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
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = AppDimensions.paddingSmall)
                          .testTag("request_feedback_button"),
              ) {
                Text(
                    text = "Request Feedback",
                    modifier = Modifier.testTag("request_feedback_button_text"))
              }
            }
      })
}

@Composable
fun ChatMessageItem(message: Message) {
  val backgroundColor =
      if (message.role == "user") {
        AppColors.userMessageBackgroundColor
      } else {
        AppColors.assistantMessageBackgroundColor
      }

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = AppDimensions.paddingExtraSmall)
              .testTag("chat_message_item"),
      horizontalArrangement = if (message.role == "user") Arrangement.End else Arrangement.Start) {
        Box(
            modifier =
                Modifier.background(
                        backgroundColor,
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall))
                    .padding(AppDimensions.paddingSmallMedium)
                    .testTag("chat_message_box")) {
              Text(
                  text = message.content,
                  color = AppColors.textColor,
                  modifier = Modifier.testTag("chat_message_text"))
            }
      }
}
