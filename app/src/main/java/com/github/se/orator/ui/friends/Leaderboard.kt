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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.ProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  // Collect the user profile and friends' profiles from the ViewModel
  val userProfile by userProfileViewModel.userProfile.collectAsState()
  val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()

  // Combine user and friends into a single list and sort by improvement (highest to lowest)
  val leaderboardEntries =
      remember(userProfile, friendsProfiles) {
        (listOfNotNull(userProfile) + friendsProfiles).sortedByDescending {
          it.statistics.improvement
        }
      }

  val focusManager = LocalFocusManager.current
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  ProjectTheme {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet {
            Column(
                modifier =
                    Modifier.fillMaxHeight()
                        .padding(AppDimensions.drawerPadding)
                        .testTag("leaderboardDrawer")) {
                  Text(
                      "Actions",
                      style = MaterialTheme.typography.titleMedium,
                      modifier = Modifier.testTag("leaderboardDrawerTitle"))
                  Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

                  // Option to Add Friend
                  TextButton(
                      onClick = { navigationActions.navigateTo(Screen.ADD_FRIENDS) },
                      modifier = Modifier.testTag("leaderboardAddFriend")) {
                        Text("➕ Add a friend")
                      }

                  Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

                  // Option to Friends List
                  TextButton(
                      onClick = { navigationActions.navigateTo(Screen.FRIENDS) },
                      modifier = Modifier.testTag("leaderboardViewFriends")) {
                        Text(
                            "👥 View Friends",
                            modifier = Modifier.testTag("leaderboardViewFriendsText"))
                      }
                }
          }
        }) {
          Scaffold(
              topBar = {
                TopAppBar(
                    title = {
                      Text("Leaderboard", modifier = Modifier.testTag("leaderboardTitle"))
                    },
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
                    selectedItem = navigationActions.currentRoute())
              }) { innerPadding ->
                Column(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(innerPadding)
                            .padding(
                                horizontal = AppDimensions.paddingMedium,
                                vertical = AppDimensions.paddingSmall)
                            .clickable { focusManager.clearFocus() }
                            .testTag("leaderboardList")) {
                      LazyColumn(
                          contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                          verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall),
                          modifier = Modifier.testTag("leaderboardLazyColumn")) {
                            // Use itemsIndexed to get the index for ranks
                            itemsIndexed(leaderboardEntries) { index, profile ->
                              LeaderboardItem(rank = index + 1, profile = profile)
                            }
                          }
                    }
              }
        }
  }
}

@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile) {
  val rankColor =
      when (rank) {
        1 -> AppColors.goldColor // Gold color
        2 -> AppColors.silverColor // Silver color
        3 -> AppColors.bronzeColor // Bronze color
        else -> MaterialTheme.colorScheme.onSurface // Normal color for the rest
      }

  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .background(MaterialTheme.colorScheme.surface)
              .padding(AppDimensions.paddingMedium)
              .testTag("leaderboardItem#$rank"),
      verticalAlignment = Alignment.CenterVertically) {
        ProfilePicture(
            profilePictureUrl = profile.profilePic,
            onClick = {},
            modifier = Modifier.size(AppDimensions.profilePictureSize))
        Spacer(modifier = Modifier.width(AppDimensions.spacerWidthMedium))

        Column {
          // Show the rank with the specified color
          Text(
              text = "$rank. ${profile.name}",
              style = MaterialTheme.typography.titleMedium,
              color = rankColor, // Apply the color based on rank
              modifier = Modifier.testTag("leaderboardItemName#$rank"))
          Text(
              text = "Improvement: ${profile.statistics.improvement}",
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.testTag("leaderboardItemImprovement#$rank"))
        }
      }
}
