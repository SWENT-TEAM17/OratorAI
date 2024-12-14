package com.github.se.orator.ui.battle

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationScreen(
    userId: String,
    battleId: String,
    navigationActions: NavigationActions,
    battleViewModel: BattleViewModel
) {
  val battle by battleViewModel.getBattleByIdFlow(battleId).collectAsState(initial = null)
  var errorMessage by remember { mutableStateOf<String?>(null) }

  // If challenger and no evaluationResult yet, start evaluation
  LaunchedEffect(battle) {
    battle?.let { b ->
      if (b.status == BattleStatus.EVALUATING &&
          b.evaluationResult == null &&
          b.challenger == userId) {
        battleViewModel.evaluateBattle(battleId) { e -> errorMessage = e.message }
      }
    }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Battle Evaluation", color = MaterialTheme.colorScheme.onSurface) })
      },
      content = { paddingValues ->
        when {
          errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
                }
          }
          battle == null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text("Loading battle data...")
                }
          }
          battle!!.status == BattleStatus.EVALUATING -> {
            // Show loading while waiting for completion
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
                    Text(
                        "EVALUATING PERFORMANCE AND DETERMINING THE WINNER",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface)
                  }
                }
          }
          battle!!.status == BattleStatus.COMPLETED && battle!!.evaluationResult != null -> {
            // Battle is completed, show the results
            DisplayResultAndFeedback(battle, userId, paddingValues)
          }
          else -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      "Waiting for evaluation to start...",
                      color = MaterialTheme.colorScheme.onSurface,
                      textAlign = TextAlign.Center,
                      style = MaterialTheme.typography.bodyLarge)
                }
          }
        }
      })
}

@Composable
fun DisplayResultAndFeedback(battle: SpeechBattle?, userId: String, paddingValues: PaddingValues) {
  val result = battle!!.evaluationResult
  val userIsWinner = result!!.winnerUid == userId
  val message = if (userIsWinner) result.winnerMessage.content else result.loserMessage.content
  val resultText = if (userIsWinner) "You Won!" else "You Lost."

  Box(
      modifier = Modifier.fillMaxSize().padding(paddingValues),
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              resultText,
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp).testTag("resultText"))
          Spacer(modifier = Modifier.size(16.dp))
          Text(
              message,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.primary,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 16.dp).testTag("message"))
          Spacer(modifier = Modifier.size(24.dp))
          Text("You can now return to the main screen.")
          // TODO: implement navigation or retry mechanism
        }
      }
}
