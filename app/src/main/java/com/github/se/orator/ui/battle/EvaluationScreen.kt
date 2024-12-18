package com.github.se.orator.ui.battle

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.navigation.TopNavigationMenu
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

/**
 * The composable that is displayed on the evaluation screen
 *
 * @param userId the user id
 * @param battleId the battle id
 * @param navigationActions an instance of navigation actions
 * @param battleViewModel the battle view model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationScreen(
    userId: String,
    battleId: String,
    navigationActions: NavigationActions,
    battleViewModel: BattleViewModel,
    chatViewModel: ChatViewModel
) {
  val battle by battleViewModel.getBattleByIdFlow(battleId).collectAsState(initial = null)
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // If challenger and no evaluationResult yet, start evaluation
  LaunchedEffect(battle) {
    battle?.let { b ->
      if (b.status == BattleStatus.EVALUATING &&
          b.evaluationResult == null &&
          b.challenger == userId) {
        Log.d("EvalScreen", "Calling eval battle")
        battleViewModel.evaluateBattle(battleId) { e -> errorMessage = e.message }
      }
    }
  }

  Scaffold(
      topBar = { TopNavigationMenu("Battle Evaluation", testTag = "battleEvaluation") },
      content = { paddingValues ->
        when {
          errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      "Error: $errorMessage",
                      color = MaterialTheme.colorScheme.error,
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.bodyLarge,
                      modifier = Modifier.testTag("errorText"))
                }
          }
          battle == null -> {
            Log.d("EvalScreen", "Battle is null")
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      "Loading battle data...",
                      color = MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.bodyLarge,
                      modifier = Modifier.testTag("loadingText"))
                }
          }
          battle!!.status == BattleStatus.EVALUATING -> {
            // Show loading while waiting for evaluation to complete
            Log.d("EvalScreen", "Evaluating result")
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = AppColors.loadingIndicatorColor,
                        strokeWidth = AppDimensions.strokeWidth,
                        modifier =
                            Modifier.size(AppDimensions.loadingIndicatorSize)
                                .testTag("loadingIndicator"))

                    Spacer(modifier = Modifier.size(AppDimensions.paddingMedium))

                    Text(
                        "Evaluating performance and determining the winner",
                        textAlign = TextAlign.Center,
                        style = AppTypography.loadingTextStyle,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.testTag("loadingText"))
                  }
                }
          }
          battle!!.status == BattleStatus.COMPLETED -> {
            if (battle!!.evaluationResult != null) {
              // Battle is completed, show the results
              Log.d("EvalScreen", "Battle completed")
              DisplayResultAndFeedback(
                  battle, userId, paddingValues, navigationActions, chatViewModel)
            } else {
              // EvaluationResult not yet available
              Log.d("EvalScreen", "Battle is COMPLETED but evaluationResult is null")
              Box(
                  modifier = Modifier.fillMaxSize().padding(paddingValues),
                  contentAlignment = Alignment.Center) {
                    Text(
                        "Processing results...",
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("processingResults"))
                  }
            }
          }
          else -> {
            Log.d("EvalScreen", "Else statement: ${battle!!.status}")
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      "Waiting for evaluation to start...",
                      color = MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.bodyLarge,
                      modifier = Modifier.testTag("waitingForEvaluation"))
                }
          }
        }
      })
}

/**
 * Helper method to display result and feedback depending on the user
 *
 * @param battle the battle data
 * @param userId the user id
 * @param paddingValues the padding values
 * @param navigationActions the navigation action instance
 */
@Composable
fun DisplayResultAndFeedback(
    battle: SpeechBattle?,
    userId: String,
    paddingValues: PaddingValues,
    navigationActions: NavigationActions,
    chatViewModel: ChatViewModel
) {

  val result = battle!!.evaluationResult
  val userIsWinner = result!!.winnerUid == userId
  val message = if (userIsWinner) result.winnerMessage.content else result.loserMessage.content
  val resultText = if (userIsWinner) "You Won!" else "You Lost."

  Log.d("EvalScreen", "Displaying result $resultText $userId")

  Box(
      modifier = Modifier.fillMaxSize().padding(paddingValues),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          // Display the result
          Text(
              resultText,
              style = AppTypography.mediumTitleStyle,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center,
              modifier =
                  Modifier.padding(horizontal = AppDimensions.paddingSmall).testTag("resultText"))

          Spacer(modifier = Modifier.size(AppDimensions.paddingSmall))

          // Display the feedback
          Text(
              message,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier =
                  Modifier.padding(horizontal = AppDimensions.paddingSmall).testTag("message"))

          Spacer(modifier = Modifier.size(AppDimensions.paddingSmall))

          // Buttons to retry, go to practice or return to home

          ActionButton(text = "Retry") {
            // Navigate to the battle screen again with the same friendUid
            // TODO: implement retry mechanism (with the same context -> maybe just check if
            // opponent wants to retry aswell and then just go to chat)
          }

          Spacer(modifier = Modifier.size(AppDimensions.paddingSmall))

          ActionButton(text = "Go to Practice") {
            chatViewModel.resetPracticeContext()
            chatViewModel.endConversation()
            navigationActions.navigateTo(Screen.SPEAKING_JOB_INTERVIEW)
          }

          Spacer(modifier = Modifier.size(AppDimensions.paddingSmall))

          ActionButton(text = "Return to Home") {
            chatViewModel.resetPracticeContext()
            chatViewModel.endConversation()
            navigationActions.navigateTo(Screen.HOME)
          }
        }
      }
}

/**
 * A helper composable to create a styled action button with consistent styling, reducing code
 * duplication.
 *
 * @param text The button label.
 * @param onClick The lambda to be invoked when the button is clicked.
 */
@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
  Button(
      onClick = onClick,
      modifier =
          Modifier.size(
              height = AppDimensions.buttonHeightLarge, width = AppDimensions.buttonWidthMax),
      colors =
          ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Text(
        text = text, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.testTag(text))
  }
}
