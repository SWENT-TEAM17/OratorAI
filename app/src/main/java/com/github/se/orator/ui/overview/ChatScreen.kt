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

/**
 * Enum class representing the different types of buttons that can be displayed in the chat screen.
 *
 * @param testTag The test tag for the button.
 * @param buttonText The text to display on the button.
 */
enum class ChatButtonType(
    val testTag: String,
    val buttonText: String,
    val buttonTextTestTag: String
) {
    FEEDBACK_BUTTON(
        testTag = "feedback_button",
        buttonText = "Request Feedback",
        buttonTextTestTag = "request_feedback_button_text"
    ),
    FINISH_BATTLE_BUTTON(
        testTag = "finish_battle_button",
        buttonText = "Finish Battle",
        buttonTextTestTag = "finish_battle_button_text"
    );
}

/**
 * Composable function that represents the Chat Screen.
 *
 * This screen displays a list of chat messages between the user and the assistant. It includes a
 * top app bar with a back button, a scrollable list of messages, and buttons at the bottom for
 * recording a response or requesting feedback.
 *
 * @param navigationActions An instance of [NavigationActions] to handle navigation events.
 * @param chatViewModel The [ChatViewModel] that provides chat messages and loading state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel,
    chatButtonType: ChatButtonType = ChatButtonType.FEEDBACK_BUTTON,
    onChatButtonClick: () -> Unit = { navigationActions.navigateTo(Screen.FEEDBACK) }
) {
  // Collect the list of chat messages from the view model as a state.
  val chatMessages by chatViewModel.chatMessages.collectAsState()

  // Collect the loading state from the view model as a state.
  val isLoading by chatViewModel.isLoading.collectAsState()

  // Remember the list state for the LazyColumn to manage scrolling position.
  val listState = rememberLazyListState()

  // Side effect to auto-scroll to the last message when a new message is added.
  LaunchedEffect(chatMessages.size) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  // Scaffold provides the basic visual layout structure.
  Scaffold(
      // Top app bar with a centered title and a back button.
      topBar = {
        CenterAlignedTopAppBar(
            title = {
              Text(
                  text = "Chat Screen",
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface, // Use theme color for text
                  modifier = Modifier.testTag("chat_screen_title"))
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    navigationActions.goBack()
                    chatViewModel.resetPracticeContext()
                    chatViewModel.endConversation()
                  },
                  modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(AppDimensions.iconSizeSmall),
                        tint = MaterialTheme.colorScheme.onSurface) // Use theme color for icon
              }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.testTag("top_app_bar"))
      },
      content = { paddingValues ->
        Column(
            // Divider to separate the TopAppBar from the content.
            modifier =
                Modifier.fillMaxSize().padding(paddingValues).testTag("chat_screen_column")) {
              // Divider to separate the TopAppBar from the content.
              HorizontalDivider(modifier = Modifier.testTag("divider"))

              // Main content column containing the messages and buttons.
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(horizontal = AppDimensions.paddingMedium)
                          .padding(top = AppDimensions.paddingSmall)
                          .testTag("content_column")) {
                    // LazyColumn to display chat messages in a scrollable list.
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier.weight(1f)
                                . // Makes the LazyColumn fill available space.
                                fillMaxWidth()
                                .testTag(
                                    "chat_messages_list")) // Dynamically add chat message items.
                    {
                          items(chatMessages) { message -> ChatMessageItem(message) }
                        }

                    // Display a loading indicator when a message is being processed.
                    if (isLoading) {
                      CircularProgressIndicator(
                          modifier =
                              Modifier.align(Alignment.CenterHorizontally)
                                  .padding(AppDimensions.paddingSmall)
                                  .testTag("loading_indicator"),
                          color = MaterialTheme.colorScheme.onBackground) // Use theme color
                    }

                    // Button to navigate to the "Speaking" screen to record a response.
                    Button(
                        onClick = { navigationActions.navigateTo(Screen.SPEAKING) },
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = AppDimensions.paddingSmall)
                                .border(
                                    width = AppDimensions.borderStrokeWidth,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.medium)
                                .testTag("record_response_button"),
                        enabled = !isLoading,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                          Text(
                              text = "Record Response",
                              modifier = Modifier.testTag("record_response_button_text"),
                              color = MaterialTheme.colorScheme.primary)
                        }

                    // Dynamic "Request Feedback" button with customizable text and action.
                    Button(
                        onClick = { onChatButtonClick() }, // Custom action for the button
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = AppDimensions.paddingSmall)
                                .border(
                                    width = AppDimensions.borderStrokeWidth,
                                    color = MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.5f),
                                    shape = MaterialTheme.shapes.medium)
                                .testTag(chatButtonType.testTag),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                          Text(
                              text = chatButtonType.buttonText,
                              modifier = Modifier.testTag(chatButtonType.buttonTextTestTag),
                              color = MaterialTheme.colorScheme.primary)
                        }
                  }
            }
      },
      modifier = Modifier.testTag("scaffold"))
}

/**
 * Composable function that represents a single chat message item.
 *
 * This function displays a message bubble with different background colors and alignment based on
 * whether the message is from the user or the assistant.
 *
 * @param message An instance of [Message] containing the message content and role.
 */
@Composable
fun ChatMessageItem(message: Message) {
  // Determine the background color based on the message role.
  val backgroundColor =
      if (message.role == "user") {
        MaterialTheme.colorScheme.primaryContainer // Use theme color for user messages
      } else {
        MaterialTheme.colorScheme.secondaryContainer // Use theme color for assistant messages
      }

  val textColor =
      if (message.role == "user") {
        MaterialTheme.colorScheme.onPrimaryContainer // Use theme color for user messages
      } else {
        MaterialTheme.colorScheme.onSecondaryContainer // Use theme color for assistant messages
      }

  // Determine the alignment based on the message role.
  val alignment = if (message.role == "user") Arrangement.End else Arrangement.Start

  // Row to align the message bubble horizontally.
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = AppDimensions.paddingExtraSmall)
              .testTag("chat_message_row"),
      horizontalArrangement = alignment) {
        // Message bubble.
        Box(
            modifier =
                Modifier.background(
                        backgroundColor,
                        shape = RoundedCornerShape(AppDimensions.cornerRadiusSmall))
                    .padding(AppDimensions.paddingSmall)
                    .testTag("message_bubble")) {
              // Display the message content.
              Text(
                  text = message.content,
                  color = textColor,
                  modifier = Modifier.testTag("message_text"))
            }
      }
}
