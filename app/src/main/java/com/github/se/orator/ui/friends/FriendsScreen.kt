package com.github.se.orator.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.ProjectTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  val filteredFriends =
      friendsProfiles.filter { friend -> friend.name.contains(searchQuery, ignoreCase = true) }

  // Manage focus for the search bar
  val focusRequester = FocusRequester()
  val focusManager = LocalFocusManager.current
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  ProjectTheme {
    // ModalDrawer Scaffold
    ModalNavigationDrawer(
        modifier = Modifier.testTag("viewFriendsDrawerMenu"),
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet {
            Column(modifier = Modifier.fillMaxHeight().padding(AppDimensions.drawerPadding)) {
              Text(
                  "Actions",
                  modifier = Modifier.testTag("viewFriendsDrawerTitle"),
                  style = MaterialTheme.typography.titleMedium)
              Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

              // Option to Add Friend
              TextButton(
                  modifier = Modifier.testTag("viewFriendsAddFriendButton"),
                  onClick = {
                    // Navigate to Add Friend screen
                    scope.launch {
                      drawerState.close() // Close the drawer
                      navigationActions.navigateTo(Screen.ADD_FRIENDS)
                    }
                  }) {
                    Text("➕ Add a friend")
                  }

              Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

              // Option to Leaderboard
              TextButton(
                  modifier = Modifier.testTag("viewFriendsLeaderboardButton"),
                  onClick = {
                    // Close drawer and navigate to Leaderboard screen
                    scope.launch {
                      drawerState.close() // Close the drawer
                      navigationActions.navigateTo(Screen.LEADERBOARD)
                    }
                  }) {
                    Text("⭐ Leaderboard")
                  }
            }
          }
        }) {
          Scaffold(
              topBar = {
                TopAppBar(
                    title = {
                      Text(
                          "My Friends",
                          modifier = Modifier.testTag("myFriendsTitle") // Added testTag
                          )
                    },
                    navigationIcon = {
                      IconButton(
                          modifier = Modifier.testTag("viewFriendsMenuButton"),
                          onClick = {
                            scope.launch {
                              drawerState.open() // Open the drawer
                            }
                          }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                          }
                    })
              },
              bottomBar = {
                BottomNavigationMenu(
                    onTabSelect = { route ->
                      scope.launch {
                        drawerState.close() // Close the drawer before navigating
                        navigationActions.navigateTo(route)
                      }
                    },
                    tabList = LIST_TOP_LEVEL_DESTINATION,
                    selectedItem = navigationActions.currentRoute())
              }) { innerPadding ->
                // Main container that will remove focus from the search bar when clicked
                Column(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(innerPadding)
                            .padding(
                                horizontal = AppDimensions.paddingMedium,
                                vertical = AppDimensions.paddingSmall)) {
                      // Search bar to filter friends by name
                      OutlinedTextField(
                          value = searchQuery,
                          onValueChange = { searchQuery = it },
                          label = { Text("Search for a friend") },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .padding(bottom = AppDimensions.paddingSmall)
                                  .focusRequester(
                                      focusRequester) // Attach focusRequester to search bar
                                  .testTag("viewFriendsSearch"))

                      if (filteredFriends.isEmpty()) {
                        // Show "No user found" when there are no matches
                        Box(
                            modifier =
                                Modifier.fillMaxSize().testTag("noUserFound"), // Added testTag
                            contentAlignment = Alignment.Center) {
                              Text(
                                  text = "No user found",
                                  style = MaterialTheme.typography.bodyLarge,
                                  modifier = Modifier.testTag("noUserFoundText") // Added testTag
                                  )
                            }
                      } else {
                        // LazyColumn for displaying friends
                        LazyColumn(
                            modifier = Modifier.testTag("viewFriendsList"),
                            contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                            verticalArrangement =
                                Arrangement.spacedBy(AppDimensions.paddingSmall)) {
                              items(filteredFriends) { friend ->
                                FriendItem(friend = friend, userProfileViewModel)
                              }
                            }
                      }
                    }
              }
        }
  }
}

@Composable
fun FriendItem(friend: UserProfile, userProfileViewModel: UserProfileViewModel) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .background(MaterialTheme.colorScheme.surface)
              .padding(AppDimensions.paddingMedium)
              .testTag("viewFriendsItem#${friend.uid}"),
      verticalAlignment = Alignment.CenterVertically) {
        ProfilePicture(
            profilePictureUrl = friend.profilePic,
            onClick = {},
        )
        Spacer(modifier = Modifier.width(AppDimensions.spacerWidthMedium))

        Column {
          Text(
              text = friend.name,
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.testTag("friendName#${friend.uid}") // Added testTag
              )
          Text(
              text = friend.bio ?: "No bio available",
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.testTag("friendBio#${friend.uid}") // Added testTag
              )
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes the delete button to the right

        DeleteFriendButton(friend = friend, userProfileViewModel = userProfileViewModel)
      }
}

/**
 * A composable function that displays a profile picture. If no profile picture URL is provided, a
 * default image is shown. The image is clickable, triggering the provided [onClick] function.
 *
 * @param profilePictureUrl The URL of the profile picture to be displayed. If null, a default image
 *   is shown.
 * @param onClick A lambda function that is triggered when the profile picture is clicked.
 */
@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {
  val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)
  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          Modifier.size(AppDimensions.iconSize).clip(CircleShape).clickable(onClick = onClick))
}

/**
 * Button triggering the removing of a friend in the user's friend list.
 *
 * @param friend The friend to be removed.
 * @param userProfileViewModel The view model for the user's profile.
 */
@Composable
fun DeleteFriendButton(friend: UserProfile, userProfileViewModel: UserProfileViewModel) {
  IconButton(
      onClick = { userProfileViewModel.deleteFriend(friend) },
      modifier = Modifier.testTag("deleteFriendButton#${friend.uid}")) {
        Icon(
            imageVector = Icons.Default.Delete, // Built-in delete icon
            contentDescription = "Delete",
            tint = Color.Red)
      }
}
