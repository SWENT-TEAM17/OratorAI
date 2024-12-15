package com.github.se.orator.ui.battle

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.*
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
import com.github.se.orator.ui.theme.AppTypography

/**
 * Composable function that displays a message indicating that the battle request has been sent, and
 * shows a loading indicator while waiting for the friend to accept.
 *
 * @param friendUid The UID of the friend.
 * @param navigationActions Actions to handle navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleRequestSentScreen(
    friendUid: String,
    battleId: String,
    userProfileViewModel: UserProfileViewModel,
    navigationActions: NavigationActions,
    battleViewModel: BattleViewModel
) {
  // Get the friend's name
  val friendName = userProfileViewModel.getName(friendUid)

  // State to observe the battle status
  val battleStatus by battleViewModel.getBattleStatus(battleId).collectAsState(initial = null)

  // LaunchedEffect to monitor the battle status and redirect when it's updated to IN_PROGRESS
  LaunchedEffect(battleStatus) {
    if (battleStatus == BattleStatus.IN_PROGRESS) {
      Log.d("BattleRequestSentScreen", "Battle in progress")
      // Navigate to the battle screen
      battleViewModel.startBattle(battleId)
    }
  }

  Scaffold(
      topBar = {
          TopNavigationMenu(
              testTag = "topAppBar",
              title = {
                  Text(
                      "Battle Request Sent",
                      color = MaterialTheme.colorScheme.onSurface,
                      style = AppTypography.mediumTopBarStyle
                  )
              },
              navigationIcon = {
                  IconButton(onClick = { navigationActions.goBack() }) {
                      Icon(
                          Icons.Outlined.ArrowBackIosNew,
                          contentDescription = "Back",
                          modifier = Modifier.size(AppDimensions.iconSizeMedium).testTag("backButton"),
                          tint = MaterialTheme.colorScheme.onSurface)
                  }
              },
          )
      },
      content = { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppDimensions.paddingMedium)
                    .wrapContentSize(Alignment.Center)
                    .testTag("battleRequestSentScreen"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text =
                      "Interview Battle request has been successfully sent to $friendName.\nWaiting for $friendName to accept the battle.",
                  style = MaterialTheme.typography.bodyLarge,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier =
                      Modifier.padding(bottom = AppDimensions.paddingMedium)
                          .testTag("battleRequestSentText"))

              CircularProgressIndicator(
                  color = AppColors.loadingIndicatorColor,
                  strokeWidth = AppDimensions.strokeWidth,
                  modifier =
                      Modifier.size(AppDimensions.loadingIndicatorSize).testTag("loadingIndicator"))
            }
      })
}
