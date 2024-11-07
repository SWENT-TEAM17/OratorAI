package com.github.se.orator.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppColors.LightPurpleGrey
import com.github.se.orator.ui.theme.AppDimensions
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

  val focusRequester = FocusRequester()
  val focusManager = LocalFocusManager.current
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  ModalNavigationDrawer(
      modifier = Modifier.testTag("viewFriendsDrawerMenu"),
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet {
          Column(modifier = Modifier.fillMaxHeight().padding(AppDimensions.paddingMedium)) {
            Text(
                "Actions",
                modifier = Modifier.testTag("viewFriendsDrawerTitle"),
                style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(AppDimensions.heightMedium))
            TextButton(
                modifier = Modifier.testTag("viewFriendsAddFriendButton"),
                onClick = {
                  scope.launch {
                    drawerState.close()
                    navigationActions.navigateTo(Screen.ADD_FRIENDS)
                  }
                }) {
                  Text("➕ Add a friend")
                }
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
            TextButton(
                modifier = Modifier.testTag("viewFriendsLeaderboardButton"),
                onClick = {
                  scope.launch {
                    drawerState.close()
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
              Column {
                CenterAlignedTopAppBar(
                    title = {
                      Text(
                          "My Friends",
                          modifier = Modifier.testTag("myFriendsTitle") // Added testTag
                          )
                    },
                    navigationIcon = {
                      IconButton(
                          onClick = { scope.launch { drawerState.open() } },
                          modifier = Modifier.testTag("viewFriendsMenuButton"),
                      ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                      }
                    })
                Divider() // Adds a separation line below the TopAppBar
              }
            },
            bottomBar = {
              BottomNavigationMenu(
                  onTabSelect = { route ->
                    scope.launch {
                      drawerState.close()
                      navigationActions.navigateTo(route)
                    }
                  },
                  tabList = LIST_TOP_LEVEL_DESTINATION,
                  selectedItem = Route.FRIENDS)
            }) { innerPadding ->
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(innerPadding)
                          .padding(horizontal = AppDimensions.paddingMedium)
                          .clickable { focusManager.clearFocus() }) {
                    Box(
                        modifier =
                            Modifier.padding(vertical = AppDimensions.paddingMediumSmall) // Apply padding to the container
                        ) {
                          OutlinedTextField(
                              value = searchQuery,
                              onValueChange = { searchQuery = it },
                              label = { Text("Search for a friend.") },
                              leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon")
                              },
                              modifier =
                                  Modifier.wrapContentWidth()
                                      .horizontalScroll(rememberScrollState())
                                      .height(AppDimensions.mediumHeight)
                                      .focusRequester(focusRequester)
                                      .testTag("viewFriendsSearch"))
                        }

                    if (filteredFriends.isEmpty()) {
                      Box(
                          modifier = Modifier.fillMaxSize().testTag("noUserFound"),
                          contentAlignment = Alignment.Center) {
                            Text(
                                "No user found",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag("noUserFoundText"))
                          }
                    } else {
                      LazyColumn(
                          modifier = Modifier.testTag("viewFriendsList"),
                          contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                          verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall)) {
                            items(filteredFriends) { friend -> FriendItem(friend = friend) }
                          }
                    }
                  }
            }
      }
}

@Composable
fun FriendItem(friend: UserProfile) {
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.smallPadding) // Side padding for each item
              .clip(RoundedCornerShape(AppDimensions.paddingMediumSmall))
              .testTag("viewFriendsItem#${friend.uid}"),
      color = LightPurpleGrey,
      shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
      ) {
        Row(modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingMedium)) {
          ProfilePicture(profilePictureUrl = friend.profilePic, onClick = {})
          Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
          Column {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = AppDimensions.smallPadding).testTag("friendName#${friend.uid}"))
            Text(
                text = friend.bio ?: "No bio available",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("friendBio#${friend.uid}"))
          }
        }
      }
}

@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {
  val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)
  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          Modifier.size(AppDimensions.buttonHeight)
              .clip(CircleShape)
              .background(Color.LightGray)
              .clickable(onClick = onClick))
}
