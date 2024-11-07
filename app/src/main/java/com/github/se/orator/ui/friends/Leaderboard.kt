package com.github.se.orator.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppColors.LightPurpleGrey
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.ProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  val userProfile by userProfileViewModel.userProfile.collectAsState()
  val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()

  val leaderboardEntries =
      remember(userProfile, friendsProfiles) {
        (listOfNotNull(userProfile) + friendsProfiles).sortedByDescending {
          it.statistics.improvement
        }
      }

  ProjectTheme {
    Scaffold(
        topBar = {
          TopAppBar(
              title = { Text("Leaderboard", modifier = Modifier.testTag("leaderboardTitle")) },
              navigationIcon = {
                IconButton(
                    onClick = {
                      navigationActions.goBack() // Only navigate back, no drawer action
                    },
                    modifier = Modifier.testTag("leaderboardBackButton")) {
                      Icon(
                          Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Back",
                          modifier = Modifier.testTag("leaderboardBackIcon"))
                    }
              })
        },
        bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { route -> navigationActions.navigateTo(route) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = Route.FRIENDS)
        }) { innerPadding ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(innerPadding)
                      .padding(
                          horizontal = AppDimensions.paddingMedium,
                          vertical = AppDimensions.paddingSmall)
                      .testTag("leaderboardList"),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Dropdown selector
                PracticeModeSelector()

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

                // Leaderboard list
                LazyColumn(
                    contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall),
                    modifier = Modifier.testTag("leaderboardLazyColumn")) {
                      itemsIndexed(leaderboardEntries) { index, profile ->
                        LeaderboardItem(rank = index + 1, profile = profile)
                      }
                    }
              }
        }
  }
}

@Composable
fun PracticeModeSelector() {
  var expanded by remember { mutableStateOf(false) }
  var selectedMode by remember { mutableStateOf("Practice mode 1") }

  Box(
      modifier =
          Modifier.clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
              .clickable { expanded = true }
              .padding(AppDimensions.paddingSmallMedium)
              .testTag("practiceModeSelector"),
      contentAlignment = Alignment.Center) {
        Text(
            text = selectedMode,
            fontSize = AppDimensions.mediumText,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("selectedMode"))

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          DropdownMenuItem(
              text = { Text("Practice mode 1") },
              onClick = {
                selectedMode = "Practice mode 1"
                expanded = false
              },
              modifier = Modifier.testTag("practiceModeOption1"))
          DropdownMenuItem(
              text = { Text("Practice mode 2") },
              onClick = {
                selectedMode = "Practice mode 2"
                expanded = false
              },
              modifier = Modifier.testTag("practiceModeOption2"))
          // Add more items as needed
        }
      }
}

@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile) {

  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.paddingExtraSmall) // Side padding for each item
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius)),
      color = LightPurpleGrey,
      shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
      ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(AppDimensions.paddingMedium)
                    .testTag("leaderboardItem#$rank")) {
              ProfilePicture(profilePictureUrl = profile.profilePic, onClick = {})
              Spacer(modifier = Modifier.width(AppDimensions.paddingSmallMedium))

              Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "Improvement: ${profile.statistics.improvement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryTextColor,
                    modifier = Modifier.testTag("leaderboardItemImprovement#$rank"))
              }

              Spacer(modifier = Modifier.weight(AppDimensions.full))

              // Display rank as badge on the left side
              Text(
                  text = "#$rank",
                  fontWeight = FontWeight.Bold,
                  modifier =
                      Modifier.align(Alignment.CenterVertically)
                          .testTag("leaderboardItemName#$rank"))
            }
      }
}
