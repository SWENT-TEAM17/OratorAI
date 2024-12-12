package com.github.se.orator.ui.overview

import android.util.Log
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
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.SessionType
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    chatViewModel: ChatViewModel,
    userProfileViewModel: UserProfileViewModel,
    apiLinkViewModel: ApiLinkViewModel,
    navigationActions: NavigationActions
) {
  var feedbackMessage by remember { mutableStateOf<String?>(null) }
  var decisionResult by remember { mutableStateOf<ChatViewModel.DecisionResult?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val practiceContext by apiLinkViewModel.practiceContext.collectAsState()
  val userProfile by userProfileViewModel.userProfile.collectAsState()
  val latestAnalysisData = userProfile?.statistics?.recentData?.lastOrNull()

  val sessionType =
      when (practiceContext) {
        is InterviewContext -> SessionType.INTERVIEW
        is PublicSpeakingContext -> SessionType.SPEECH
        is SalesPitchContext -> SessionType.NEGOTIATION
        else -> null
      }

  val successfulSessionsCount =
      sessionType?.let { userProfile?.statistics?.successfulSessions?.get(it.name) ?: 0 } ?: 0

  LaunchedEffect(Unit) {
    try {
      feedbackMessage = chatViewModel.generateFeedback()
      feedbackMessage?.let {
        decisionResult = parseDecisionFromFeedback(it, sessionType)
        if (decisionResult != null && sessionType != null) {
          userProfileViewModel.updateSessionResult(
              isSuccess = decisionResult!!.isSuccess, sessionType = sessionType)
        } else {
          Log.e("FeedbackScreen", "Session type or decision result is null.")
        }
      }
    } catch (e: Exception) {
      errorMessage = e.localizedMessage
    } finally {
      isLoading = false
    }
  }

  DisposableEffect(Unit) { onDispose { chatViewModel.endConversation() } }

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
                  color = MaterialTheme.colorScheme.onSurface)
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(AppDimensions.iconSizeSmall),
                        tint = MaterialTheme.colorScheme.onSurface)
                  }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer))
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).testTag("feedbackContent"),
        ) {
          HorizontalDivider()
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(horizontal = AppDimensions.paddingMedium)
                      .padding(top = AppDimensions.paddingSmall)
                      .testTag("feedbackTitle"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth().testTag("feedbackSubtitle")) {
                  ChatMessageItem(
                      message =
                          Message(
                              content = "Here's what you did well and where you can improve:",
                              role = "assistant"))
                }

                when {
                  isLoading -> {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(Alignment.CenterHorizontally)
                                .padding(AppDimensions.paddingMedium)
                                .testTag("loadingIndicator"),
                        color = MaterialTheme.colorScheme.onBackground)
                  }
                  errorMessage != null -> {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("errorText"))
                  }
                  feedbackMessage != null -> {
                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .testTag("feedbackMessage")) {
                          decisionResult?.message?.let { decisionText ->
                            Text(
                                text = decisionText,
                                style = AppTypography.largeTitleStyle,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier =
                                    Modifier.align(Alignment.CenterHorizontally)
                                        .padding(AppDimensions.paddingMedium)
                                        .testTag("decisionText"))
                          }

                          Text(
                              text =
                                  "You have successfully completed $successfulSessionsCount ${
                                        when (sessionType) {
                                            SessionType.SPEECH -> "speeches"
                                            SessionType.INTERVIEW -> "interviews"
                                            SessionType.NEGOTIATION -> "negotiations"
                                            else -> "sessions"
                                        }
                                    } so far!",
                              style = AppTypography.bodyLargeStyle,
                              modifier =
                                  Modifier.align(Alignment.CenterHorizontally)
                                      .padding(AppDimensions.paddingMedium)
                                      .testTag("successfulSessionsText"),
                              color = MaterialTheme.colorScheme.tertiary)
                          ChatMessageItem(
                              message = Message(content = feedbackMessage!!, role = "assistant"))

                          Log.d("FeedbackScreen", "latestAnalysis: $latestAnalysisData")

                          if (latestAnalysisData != null) {
                            Text(
                                text = "Your Latest Performance Stats:",
                                style = AppTypography.mediumTitleStyle,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(AppDimensions.paddingMedium))
                            Text(text = "Transcription: ${latestAnalysisData.transcription}")
                            Text(text = "Filler Words: ${latestAnalysisData.fillerWordsCount}")
                            Text(
                                text =
                                    "Average Pause Duration: ${"%.2f".format(latestAnalysisData.averagePauseDuration)}")
                            Text(
                                text =
                                    "Sentiment Score: ${"%.2f".format(latestAnalysisData.sentimentScore)}")
                            Text(
                                text =
                                    "Talk Time Percentage: ${"%.2f".format(latestAnalysisData.talkTimePercentage)}%")
                            Text(
                                text =
                                    "Talk Time Seconds: ${"%.2f".format(latestAnalysisData.talkTimeSeconds)}")
                            Text(text = "Pace: ${latestAnalysisData.pace}")
                          } else {
                            Text(text = "No analysis data available.")
                          }
                        }
                  }
                  else -> {
                    Text(
                        text = "No feedback available.",
                        style = AppTypography.bodyLargeStyle,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("feedbackNoMessage"))
                  }
                }

                Button(
                    onClick = {
                      chatViewModel.resetPracticeContext()
                      navigationActions.navigateTo(TopLevelDestinations.HOME)
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = AppDimensions.paddingMedium)
                            .border(
                                width = AppDimensions.borderStrokeWidth,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium)
                            .testTag("retryButton"),
                    enabled = !isLoading,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                      Text(
                          text = "Try Again",
                          modifier = Modifier.testTag("retryButtonText"),
                          color = MaterialTheme.colorScheme.primary)
                    }
              }
        }
      })
}

private fun parseDecisionFromFeedback(
    feedback: String,
    sessionType: SessionType?
): ChatViewModel.DecisionResult? {
  if (sessionType == null) return null

  val feedbackLower = feedback.lowercase()
  return when {
    sessionType.positiveResponse.lowercase() in feedbackLower ->
        ChatViewModel.DecisionResult(sessionType.successMessage, true)
    sessionType.negativeResponse.lowercase() in feedbackLower ->
        ChatViewModel.DecisionResult(sessionType.failureMessage, false)
    else -> null
  }
}
