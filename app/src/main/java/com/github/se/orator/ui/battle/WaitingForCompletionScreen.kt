package com.github.se.orator.ui.battle

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopNavigationMenu
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingForCompletionScreen(
    battleId: String,
    friendUid: String,
    navigationActions: NavigationActions,
    battleViewModel: BattleViewModel,
    userProfileViewModel: UserProfileViewModel
) {
  // State to observe the battle status
  val battle by battleViewModel.getBattleByIdFlow(battleId).collectAsState(initial = null)
  val friendName = userProfileViewModel.getName(friendUid)

  // LaunchedEffect to check both users' completion statuses and navigate accordingly
  LaunchedEffect(battle) {
    battle?.let {
      // Check if the other user has completed
      val otherUserCompleted =
          if (friendUid == it.opponent) it.opponentCompleted else it.challengerCompleted

      if (otherUserCompleted) {
        // Update the battle status to EVALUATING
        battleViewModel.updateBattleStatus(battleId, BattleStatus.EVALUATING) { success ->
          if (!success) {
            Log.e("WaitingForCompletionScreen", "Failed to update battle status.")
          }
        }
        // Navigate to the evaluation screen
        navigationActions.navigateToEvaluationScreen(battleId)
      } else {
        Log.d("WaitingForCompletionScreen", "Waiting for the other user to finish.")
      }
    }
  }

  Scaffold(
      topBar = { TopNavigationMenu(title = "Waiting for Completion") },
      content = { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppDimensions.paddingMedium)
                    .wrapContentSize(Alignment.Center)
                    .testTag("waitingForCompletionScreen"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = "You have completed your interview. Waiting for $friendName to finish.",
                  style = MaterialTheme.typography.bodyLarge,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier =
                      Modifier.padding(bottom = AppDimensions.paddingMedium).testTag("waitingText"))

              CircularProgressIndicator(
                  color = AppColors.loadingIndicatorColor,
                  strokeWidth = AppDimensions.strokeWidth,
                  modifier =
                      Modifier.size(AppDimensions.loadingIndicatorSize).testTag("loadingIndicator"))
            }
      })
}
