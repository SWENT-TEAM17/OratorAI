package com.github.se.orator.ui.battle

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
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
    userProfileViewModel: UserProfileViewModel,
    navigationActions: NavigationActions
) {
  val friendName = userProfileViewModel.getName(friendUid)
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Battle Request Sent") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
              }
            })
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
                  modifier = Modifier.padding(bottom = AppDimensions.paddingMedium))

              CircularProgressIndicator(
                  color = AppColors.loadingIndicatorColor,
                  strokeWidth = AppDimensions.strokeWidth,
                  modifier =
                      Modifier.size(AppDimensions.loadingIndicatorSize).testTag("loadingIndicator"))
            }
      })
}
