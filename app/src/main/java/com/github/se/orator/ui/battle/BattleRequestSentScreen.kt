package com.github.se.orator.ui.battle

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions

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
  val context = LocalContext.current

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

  // Handle cancellation when the user exits the app via ProcessLifecycleOwner
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_STOP) {
        if (battleStatus == BattleStatus.PENDING) {
          battleViewModel.cancelBattle(battleId)
          Log.d("BattleRequestSentScreen", "Battle canceled due to app backgrounded.")
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Battle Request Sent") },
            navigationIcon = {
              IconButton(
                  onClick = {
                    navigationActions.goBack()
                    battleViewModel.cancelBattle(battleId)
                  }) {
                    Icon(
                        Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("backButton"),
                        tint = MaterialTheme.colorScheme.onSurface)
                  }
            },
            modifier = Modifier.testTag("topAppBar"))
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

              // Text indicating that the battle request has been sent
              Text(
                  text =
                      "Interview Battle request has been successfully sent to $friendName.\nWaiting for $friendName to accept the battle.",
                  style = MaterialTheme.typography.bodyLarge,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier =
                      Modifier.padding(bottom = AppDimensions.paddingMedium)
                          .testTag("battleRequestSentText"))

              Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

              // Loading Indicator
              CircularProgressIndicator(
                  color = AppColors.loadingIndicatorColor,
                  strokeWidth = AppDimensions.strokeWidth,
                  modifier =
                      Modifier.size(AppDimensions.loadingIndicatorSize).testTag("loadingIndicator"))

              Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

              // Cancel Button
              Button(
                  onClick = {
                    battleViewModel.cancelBattle(battleId)
                    navigationActions.navigateTo(TopLevelDestinations.HOME)
                    Toast.makeText(context, "Battle has been cancelled!", Toast.LENGTH_SHORT).show()
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(AppDimensions.buttonHeightLarge)
                          .testTag("cancelButton"),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = MaterialTheme.colorScheme.error,
                          contentColor = MaterialTheme.colorScheme.onError)) {
                    Text(
                        text = "Cancel battle",
                        color = Color.White,
                        modifier = Modifier.testTag("cancelBattleButtonText"))
                  }
            }
      })
}
