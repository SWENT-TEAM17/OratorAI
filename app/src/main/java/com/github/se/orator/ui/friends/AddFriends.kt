package com.github.se.orator.ui.friends

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.profile.ProfilePictureDialog
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.ProjectTheme

/**
 * Composable function that displays the "Add Friends" screen, allowing users to search and add
 * friends. The screen contains a top app bar with a back button, a search field to look for
 * friends, and a list of matching user profiles based on the search query.
 *
 * @param navigationActions Actions to handle navigation within the app.
 * @param userProfileViewModel ViewModel for managing user profile data and friend addition logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  val userProfile by userProfileViewModel.userProfile.collectAsState()
  val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()
  var query by remember { mutableStateOf("") } // Holds the search query input
  var expanded by remember { mutableStateOf(false) } // Controls if search results are visible
  val allProfiles by userProfileViewModel.allProfiles.collectAsState() // All user profiles
  val focusRequester = FocusRequester() // Manages focus for the search field
  val sentReqProfiles by userProfileViewModel.sentReqProfiles.collectAsState()
  // Exclude the current user's profile and their friends' profiles from the list
  val filteredProfiles =
      allProfiles.filter { profile ->
        profile.uid != userProfile?.uid && // Exclude own profile
            friendsProfiles.none { friend -> friend.uid == profile.uid } && // Exclude friends
            sentReqProfiles.none { sent -> sent.uid == profile.uid } && // Exclude sent requests
            profile.name.contains(query, ignoreCase = true) // Match search query
      }

  // State variable to keep track of the selected user's profile picture
  var selectedProfilePicUser by remember { mutableStateOf<UserProfile?>(null) }

  // State variable to control the expansion of Sent Friend Requests
  var isSentRequestsExpanded by remember { mutableStateOf(false) }

  ProjectTheme {
    Scaffold(
        topBar = {
          TopAppBar(
              title = { Text("Add a Friend", modifier = Modifier.testTag("addFriendTitle")) },
              navigationIcon = {
                IconButton(
                    onClick = { navigationActions.goBack() },
                    modifier = Modifier.testTag("addFriendBackButton")) {
                      Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
              },
          )
          Divider()
        },
        bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { route -> navigationActions.navigateTo(route) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = Route.FRIENDS)
        }) { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .padding(AppDimensions.paddingMedium)) {
                // Search Field
                OutlinedTextField(
                    value = query,
                    onValueChange = { newValue ->
                      query = newValue
                      expanded = newValue.isNotEmpty()
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(AppDimensions.mediumHeight)
                            .focusRequester(focusRequester)
                            .testTag("addFriendSearchField"),
                    label = { Text("Username", modifier = Modifier.testTag("searchFieldLabel")) },
                    leadingIcon = {
                      Icon(
                          Icons.Default.Search,
                          contentDescription = "Search Icon",
                          modifier = Modifier.testTag("searchIcon"))
                    },
                    trailingIcon = {
                      if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.testTag("clearSearchButton")) {
                              Icon(
                                  Icons.Default.Clear,
                                  contentDescription = "Clear Icon",
                                  modifier = Modifier.testTag("clearIcon"))
                            }
                      }
                    },
                    singleLine = true,
                    keyboardActions = KeyboardActions.Default)

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

                // **Expandable Section: Sent Friend Requests**
                if (sentReqProfiles.isNotEmpty()) {
                  // Header with Toggle Button
                  Row(
                      modifier =
                          Modifier.fillMaxWidth()
                              .clickable { isSentRequestsExpanded = !isSentRequestsExpanded }
                              .padding(vertical = AppDimensions.smallPadding),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Sent Friend Requests",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f).testTag("sentFriendRequestsHeader"))
                        IconButton(
                            onClick = { isSentRequestsExpanded = !isSentRequestsExpanded },
                            modifier = Modifier.testTag("toggleSentRequestsButton")) {
                              Icon(
                                  imageVector =
                                      if (isSentRequestsExpanded) Icons.Default.ExpandLess
                                      else Icons.Default.ExpandMore,
                                  contentDescription =
                                      if (isSentRequestsExpanded) "Collapse Sent Requests"
                                      else "Expand Sent Requests")
                            }
                      }

                  // Animated Visibility for the Sent Requests List
                  AnimatedVisibility(
                      visible = isSentRequestsExpanded,
                      enter = expandVertically(),
                      exit = shrinkVertically()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().testTag("sentFriendRequestsList"),
                            contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                            verticalArrangement =
                                Arrangement.spacedBy(AppDimensions.paddingSmall)) {
                              items(sentReqProfiles) { sentRequest ->
                                SentFriendRequestItem(
                                    sentRequest = sentRequest,
                                    userProfileViewModel = userProfileViewModel)
                              }
                            }
                      }

                  Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
                }

                // Display search results if there is a query
                if (query.isNotEmpty()) {
                  LazyColumn(
                      contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                      verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall),
                      modifier = Modifier.testTag("searchResultsList")) {
                        // Filter and display profiles matching the query
                        items(
                            filteredProfiles.filter { profile ->
                              profile.name.contains(query, ignoreCase = true)
                            }) { user ->
                              UserItem(
                                  user = user,
                                  userProfileViewModel = userProfileViewModel,
                                  onProfilePictureClick = { selectedUser ->
                                    selectedProfilePicUser = selectedUser
                                  })
                            }
                      }
                }
              }

          // Dialog to show the enlarged profile picture
          if (selectedProfilePicUser?.profilePic != null) {
            ProfilePictureDialog(
                profilePictureUrl = selectedProfilePicUser?.profilePic ?: "",
                onDismiss = { selectedProfilePicUser = null })
          }
        }
  }
}

/**
 * Composable function that represents a single sent friend request item in the list. Displays the
 * friend's profile picture, name, bio, and an option to cancel the request.
 *
 * @param sentRequest The [UserProfile] object representing the friend to whom the request was sent.
 * @param userProfileViewModel The [UserProfileViewModel] that handles request cancellation.
 */
@Composable
fun SentFriendRequestItem(sentRequest: UserProfile, userProfileViewModel: UserProfileViewModel) {
  val context = LocalContext.current

  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.smallPadding)
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .testTag("sentFriendRequestItem#${sentRequest.uid}"),
      color = AppColors.LightPurpleGrey,
      shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
      ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically) {
              // Friend's Profile Picture
              ProfilePicture(
                  profilePictureUrl = sentRequest.profilePic,
                  onClick = { /* Optionally, show enlarged picture */})
              Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
              Column(
                  modifier = Modifier.weight(1f), // Expand to push Cancel button to the end
                  verticalArrangement = Arrangement.Center) {
                    // Friend's Name
                    Text(
                        text = sentRequest.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier =
                            Modifier.padding(bottom = AppDimensions.smallPadding)
                                .testTag("sentFriendRequestName#${sentRequest.uid}"))
                    // Friend's Bio
                    Text(
                        text = sentRequest.bio ?: "No bio available",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.secondaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("sentFriendRequestBio#${sentRequest.uid}"))
                  }
              // Cancel Friend Request Button
              IconButton(
                  onClick = {
                    userProfileViewModel.cancelFriendRequest(sentRequest)
                    Toast.makeText(
                            context,
                            "Friend request to ${sentRequest.name} has been canceled.",
                            Toast.LENGTH_SHORT)
                        .show()
                  },
                  modifier = Modifier.testTag("cancelFriendRequestButton#${sentRequest.uid}")) {
                    Icon(
                        imageVector = Icons.Default.Close, // Using Close icon for cancellation
                        contentDescription = "Cancel Friend Request",
                        tint = Color.Red)
                  }
            }
      }
}

/**
 * Composable function that represents a single user item in a list. Displays the user's profile
 * picture, name, and bio, and allows adding the user as a friend.
 *
 * @param user The [UserProfile] object representing the user being displayed.
 * @param userProfileViewModel The [UserProfileViewModel] that handles the logic of adding a user as
 *   a friend.
 * @param onProfilePictureClick Callback when the profile picture is clicked.
 */
@Composable
fun UserItem(
    user: UserProfile,
    userProfileViewModel: UserProfileViewModel,
    onProfilePictureClick: (UserProfile) -> Unit
) {
  val context = LocalContext.current // Get the context for showing Toast
  val recReqProfiles by userProfileViewModel.recReqProfiles.collectAsState()

  // State to manage the visibility of the mutual request dialog
  var showMutualRequestDialog by remember { mutableStateOf(false) }

  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.smallPadding)
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .clickable {
                // Check if the target user has already sent a friend request to the current user
                val hasIncomingRequest = recReqProfiles.any { it.uid == user.uid }

                if (hasIncomingRequest) {
                  // Show the mutual request dialog
                  showMutualRequestDialog = true
                } else {
                  // Send the friend request as usual
                  userProfileViewModel.sendRequest(user)
                  // Show a toast message confirming the action
                  Toast.makeText(
                          context,
                          "You have sent a friend request to ${user.name}.",
                          Toast.LENGTH_SHORT)
                      .show()
                }
              },
      color = AppColors.LightPurpleGrey,
      shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
      ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(AppDimensions.paddingMedium)
                    .testTag("addFriendUserItem#${user.uid}"),
            verticalAlignment = Alignment.CenterVertically) {
              // Profile picture click listener to show enlarged picture
              ProfilePicture(
                  profilePictureUrl = user.profilePic, onClick = { onProfilePictureClick(user) })
              Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
              Column {
                // Display user name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = AppDimensions.smallPadding))
                // Display user bio, with ellipsis if it exceeds one line
                Text(
                    text = user.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }
            }

        // Mutual request dialog
        if (showMutualRequestDialog) {
            AlertDialog(
                onDismissRequest = { showMutualRequestDialog = false },
                title = { Text("Friend Request") },
                text = {
                    Text(
                        text = "${user.name} has already sent you a friend request. Would you like to accept, reject, or decide later?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Accept the incoming friend request
                            userProfileViewModel.acceptFriend(user)
                            Toast.makeText(
                                context,
                                "You are now friends with ${user.name}.",
                                Toast.LENGTH_SHORT
                            ).show()
                            showMutualRequestDialog = false
                        }
                    ) {
                        Text("Accept")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                // Reject the incoming friend request
                                userProfileViewModel.declineFriendRequest(user)
                                Toast.makeText(
                                    context,
                                    "Friend request from ${user.name} has been rejected.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showMutualRequestDialog = false
                            }
                        ) {
                            Text("Reject")
                        }
                        TextButton(
                            onClick = {
                                // Decide Later: Just dismiss the dialog
                                showMutualRequestDialog = false
                            }
                        ) {
                            Text("Decide Later")
                        }
                    }
                }
            )
        }
  }
}