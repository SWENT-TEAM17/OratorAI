package com.github.se.orator.ui.overview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(chatViewModel: ChatViewModel, navigationActions: NavigationActions) {
    // State variables for feedback message, decision, loading status, and error message.
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var decision by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            feedbackMessage = chatViewModel.generateFeedback()
            feedbackMessage?.let {
                decision = parseDecisionFromFeedback(it)
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
        } finally {
            isLoading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.endConversation()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("feedbackScreen"),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().testTag("feedbackTopAppBar"),
                title = {
                    Text(
                        text = "Feedback",
                        modifier = Modifier.testTag("FeedbackText"),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(AppDimensions.iconSizeSmall),
                            tint = AppColors.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.surfaceColor,
                    titleContentColor = AppColors.textColor
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .testTag("feedbackContent"),
            ) {
                Divider()
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = AppDimensions.paddingMedium)
                        .padding(top = AppDimensions.paddingSmall)
                        .testTag("feedbackTitle"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().testTag("feedbackSubtitle")
                    ) {
                        ChatMessageItem(
                            message = Message(
                                content = "Here's what you did well and where you can improve:",
                                role = "assistant"
                            )
                        )
                    }

                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                    .padding(AppDimensions.paddingMedium)
                                    .testTag("loadingIndicator"),
                                color = AppColors.loadingIndicatorColor
                            )
                        }
                        errorMessage != null -> {
                            Text(
                                text = "Error: $errorMessage",
                                color = AppColors.errorColor,
                                modifier = Modifier.testTag("errorText")
                            )
                        }
                        feedbackMessage != null -> {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .testTag("feedbackMessage")
                            ) {
                                // Display the decision prominently.
                                decision?.let { decisionText ->
                                    Text(
                                        text = decisionText,
                                        style = AppTypography.largeTitleStyle,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(AppDimensions.paddingMedium)
                                            .testTag("decisionText")
                                    )
                                }
                                // Display the detailed feedback message.
                                ChatMessageItem(
                                    message = Message(
                                        content = feedbackMessage!!,
                                        role = "assistant"
                                    )
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "No feedback available.",
                                style = AppTypography.bodyLargeStyle,
                                color = AppColors.textColor,
                                modifier = Modifier.testTag("feedbackNoMessage")
                            )
                        }
                    }

                    Button(
                        onClick = {
                            chatViewModel.resetPracticeContext()
                            navigationActions.navigateTo(TopLevelDestinations.HOME)
                        },
                        modifier = Modifier.fillMaxWidth()
                            .padding(top = AppDimensions.paddingMedium)
                            .border(
                                width = AppDimensions.borderStrokeWidth,
                                color = AppColors.buttonBorderColor,
                                shape = MaterialTheme.shapes.medium
                            )
                            .testTag("retryButton"),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.buttonOverviewColor,
                            contentColor = AppColors.textColor
                        )
                    ) {
                        Text(
                            text = "Try Again",
                            modifier = Modifier.testTag("retryButtonText")
                        )
                    }
                }
            }
        }
    )
}

// Function to parse the decision from the feedback message
private fun parseDecisionFromFeedback(feedback: String): String? {
    val feedbackLower = feedback.lowercase()
    return when {
        "would recommend hiring" in feedbackLower || "would hire" in feedbackLower -> "Congratulations! You would be hired."
        "would not recommend hiring" in feedbackLower || "would not hire" in feedbackLower -> "Unfortunately, you would not be hired."
        "would win the competition" in feedbackLower -> "Great job! You would win the competition."
        "would not win the competition" in feedbackLower -> "You might need to improve to win the competition."
        "successfully convinced" in feedbackLower -> "Success! You have convinced the client."
        "did not convince" in feedbackLower -> "You did not convince the client this time."
        else -> null
    }
}
