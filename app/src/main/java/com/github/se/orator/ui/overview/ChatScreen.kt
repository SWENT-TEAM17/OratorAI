package com.github.se.orator.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navigationActions: NavigationActions, chatViewModel: ChatViewModel) {
  val chatMessages by chatViewModel.chatMessages.collectAsState()
  val isLoading by chatViewModel.isLoading.collectAsState()
  val listState = rememberLazyListState()

  LaunchedEffect(chatMessages.size) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  DisposableEffect(Unit) {
    chatViewModel.initializeConversation()
    onDispose { chatViewModel.endConversation() }
  }

  Scaffold(
      topBar = {
        CenterAlignedTopAppBar(
            title = {
              Text(
                  text = "Chat Screen",
                  fontWeight = FontWeight.Bold,
                  color = AppColors.textColor,
                  modifier = Modifier.testTag("chat_screen_title"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(AppDimensions.iconSizeSmall),
                        tint = AppColors.textColor)
                  }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.surfaceColor,
                    titleContentColor = AppColors.textColor),
            modifier = Modifier.testTag("top_app_bar"))
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(paddingValues).testTag("chat_screen_column")) {
              Divider(modifier = Modifier.testTag("divider"))
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(horizontal = AppDimensions.paddingMedium)
                          .padding(top = AppDimensions.paddingSmall)
                          .testTag("content_column")) {
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier.weight(1f).fillMaxWidth().testTag("chat_messages_list")) {
                          items(chatMessages) { message -> ChatMessageItem(message) }
                        }

                    if (isLoading) {
                      CircularProgressIndicator(
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally)
                                  .padding(AppDimensions.paddingSmall)
                                  .testTag("loading_indicator"),
                          color = AppColors.loadingIndicatorColor)
                    }

                    Button(
                        onClick = { navigationActions.navigateTo(Screen.SPEAKING) },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = AppDimensions.paddingSmall)
                                .border(
                                    width = AppDimensions.borderStrokeWidth,
                                    color = AppColors.buttonBorderColor,
                                    shape = MaterialTheme.shapes.medium)
                                .testTag("record_response_button"),
                        enabled = !isLoading,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = AppColors.buttonBackgroundColor,
                                contentColor = AppColors.textColor)) {
                          Text(
                              text = "Record Response",
                              modifier = Modifier.testTag("record_response_button_text"))
                        }

                    Button(
                        onClick = { navigationActions.navigateTo(Screen.FEEDBACK) },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = AppDimensions.paddingSmall)
                                .border(
                                    width = AppDimensions.borderStrokeWidth,
                                    color = AppColors.buttonBorderColor,
                                    shape = MaterialTheme.shapes.medium)
                                .testTag("request_feedback_button"),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = AppColors.buttonBackgroundColor,
                                contentColor = AppColors.textColor)) {
                          Text(
                              text = "Request Feedback",
                              modifier = Modifier.testTag("request_feedback_button_text"))
                        }
                  }
            }
      },
      modifier = Modifier.testTag("scaffold"))
}

@Composable
fun ChatMessageItem(message: Message) {
  val backgroundColor =
      if (message.role == "user") {
        AppColors.userMessageBackgroundColor
      } else {
        AppColors.assistantMessageBackgroundColor
      }

  val alignment = if (message.role == "user") Arrangement.End else Arrangement.Start

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = AppDimensions.paddingExtraSmall)
              .testTag("chat_message_row"),
      horizontalArrangement = alignment) {
        Box(
            modifier =
                Modifier.background(
                        backgroundColor,
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall))
                    .padding(AppDimensions.paddingSmall)
                    .testTag("message_bubble")) {
              Text(
                  text = message.content,
                  color = AppColors.textColor,
                  modifier = Modifier.testTag("message_text"))
            }
      }
}
