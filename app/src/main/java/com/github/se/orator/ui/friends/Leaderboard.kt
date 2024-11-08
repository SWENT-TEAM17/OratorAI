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

/**
 * Composable function that displays the "Leaderboard" screen, which shows a ranked list of friends
 * based on their improvement statistics. Users can also select different practice modes.
 *
 * @param navigationActions Actions to handle navigation within the app.
 * @param userProfileViewModel ViewModel for managing user profile data and fetching leaderboard
 *   entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  // Fetch user profile and friends' profiles to create leaderboard entries
  val userProfile by userProfileViewModel.userProfile.collectAsState()
  val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()

  // Combine and sort profiles by improvement for leaderboard display
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
                      navigationActions.goBack() // Navigate back
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
                // Dropdown selector for choosing practice mode
                PracticeModeSelector()

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

                // Leaderboard list displaying ranked profiles
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

/**
 * Composable function that displays a dropdown menu for selecting different practice modes. The
 * selected mode is shown and can be changed by the user.
 */
@Composable
fun PracticeModeSelector() {
  var expanded by remember { mutableStateOf(false) } // Controls dropdown menu visibility
  var selectedMode by remember { mutableStateOf("Practice mode 1") } // Holds the selected mode

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

        // Dropdown menu options for selecting a practice mode
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
          // Additional items can be added as needed
        }
      }
}

/**
 * Composable function that represents a single leaderboard item, displaying the profile's rank,
 * name, and improvement statistics.
 *
 * @param rank The rank position of the user in the leaderboard.
 * @param profile The [UserProfile] object containing user information and improvement statistics.
 */
@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile) {
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.paddingExtraSmall) // Side padding for each item
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .testTag("leaderboardItem#$rank"),
      color = LightPurpleGrey,
      shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
      ) {
        Row(modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingMedium)) {
          ProfilePicture(profilePictureUrl = profile.profilePic, onClick = {})
          Spacer(modifier = Modifier.width(AppDimensions.paddingSmallMedium))

          Column {
            // Display user's name in bold
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            // Display user's improvement statistics
            Text(
                text = "Improvement: ${profile.statistics.improvement}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.secondaryTextColor,
                modifier = Modifier.testTag("leaderboardItemImprovement#$rank"))
          }

          Spacer(modifier = Modifier.weight(AppDimensions.full))

          // Display rank as a badge on the left side
          Text(
              text = "#$rank",
              fontWeight = FontWeight.Bold,
              modifier =
                  Modifier.align(Alignment.CenterVertically).testTag("leaderboardItemName#$rank"))
        }
      }
}
