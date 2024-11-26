package com.github.se.orator.ui.friends

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.github.se.orator.ui.profile.ProfilePictureDialog
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.ProjectTheme
import com.github.se.orator.utils.getCurrentDate
import com.github.se.orator.utils.getDaysDifference
import com.github.se.orator.utils.parseDate
import kotlinx.coroutines.launch

/**
 * Composable function that displays the "View Friends" screen, showing a list of friends with a
 * search bar and options to navigate to other screens like "Add Friends" and "Leaderboard."
 * Additionally, displays received friend requests with options to accept or decline them.
 *
 * @param navigationActions Actions to handle navigation within the app.
 * @param userProfileViewModel ViewModel for managing user profile data and fetching friends.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
    // State variables for managing friends list and search functionality
    val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()
    val recReqProfiles by userProfileViewModel.sentReqProfiles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends =
        friendsProfiles.filter { friend -> friend.name.contains(searchQuery, ignoreCase = true) }

    // State variables for managing UI components
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // State variable to manage the currently selected friend for enlarged profile picture
    var selectedFriend by remember { mutableStateOf<UserProfile?>(null) }

    // State variable for Snackbar messages
    val snackbarHostState = remember { SnackbarHostState() }

    ProjectTheme {
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
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Column {
                        CenterAlignedTopAppBar(
                            title = { Text("My Friends", modifier = Modifier.testTag("myFriendsTitle")) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("viewFriendsMenuButton")) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            })
                        HorizontalDivider() // Adds a separation line below the TopAppBar
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
                    // Search bar for filtering friends
                    Box(
                        modifier =
                        Modifier.padding(vertical = AppDimensions.paddingMediumSmall)) {
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

                    Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

                    // Section for Received Friend Requests
                    if (recReqProfiles.isNotEmpty()) {
                        Text(
                            text = "Friend Requests",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .padding(bottom = AppDimensions.smallPadding)
                                .testTag("friendRequestsHeader")
                        )
                        LazyColumn(
                            modifier = Modifier.testTag("receivedFriendRequestsList"),
                            contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                            verticalArrangement =
                            Arrangement.spacedBy(AppDimensions.paddingSmall)) {
                            items(recReqProfiles) { friendRequest ->
                                FriendRequestItem(
                                    friendRequest = friendRequest,
                                    userProfileViewModel = userProfileViewModel
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
                    }

                    // Section for Friends List
                    Text(
                        text = "Your Friends",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(bottom = AppDimensions.smallPadding)
                            .testTag("friendsListHeader")
                    )
                    // Display message if no friends match the search query
                    if (filteredFriends.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().testTag("noFriendsFound"),
                            contentAlignment = Alignment.Center) {
                            Text(
                                "No friends found.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag("noFriendsFoundText"))
                        }
                    } else {
                        // Display the list of friends if any match the search query
                        LazyColumn(
                            modifier = Modifier.testTag("friendsList"),
                            contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                            verticalArrangement =
                            Arrangement.spacedBy(AppDimensions.paddingSmall)) {
                            items(filteredFriends) { friend ->
                                FriendItem(
                                    friend = friend,
                                    userProfileViewModel = userProfileViewModel,
                                    onProfilePictureClick = { selectedFriend = it })
                            }
                        }
                    }

                    // Dialog to display the enlarged profile picture
                    if (selectedFriend != null && !selectedFriend!!.profilePic.isNullOrEmpty()) {
                        ProfilePictureDialog(
                            profilePictureUrl = selectedFriend!!.profilePic!!,
                            onDismiss = { selectedFriend = null })
                    }
                }
            }
        }
    }
}

/**
 * Composable function that represents a single friend item in the list. Displays the friend's
 * profile picture, name, and bio, along with an option to delete the friend.
 *
 * @param friend The [UserProfile] object representing the friend being displayed.
 * @param userProfileViewModel The [UserProfileViewModel] that handles friend deletion.
 * @param onProfilePictureClick Callback when the profile picture is clicked.
 */
@Composable
fun FriendItem(
    friend: UserProfile,
    userProfileViewModel: UserProfileViewModel,
    onProfilePictureClick: (UserProfile) -> Unit
) {
    // Compute the displayedStreak
    val displayedStreak = currentFriendStreak(friend.lastLoginDate, friend.currentStreak)
    Log.d("FriendItem", "Days Since Last Login: ${friend.lastLoginDate}")

    // Compute days since last login for broken streak
    val daysSinceLastLogin =
        remember(friend.lastLoginDate) {
            if (!friend.lastLoginDate.isNullOrEmpty()) {
                val lastLoginDate = parseDate(friend.lastLoginDate)
                if (lastLoginDate != null) {
                    getDaysDifference(lastLoginDate, getCurrentDate())
                } else {
                    0L
                }
            } else {
                0L
            }
        }

    Surface(
        modifier =
        Modifier.fillMaxWidth()
            .padding(horizontal = AppDimensions.smallPadding)
            .clip(RoundedCornerShape(AppDimensions.paddingMediumSmall))
            .testTag("viewFriendsItem#${friend.uid}"),
        color = AppColors.LightPurpleGrey,
        shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePicture(
                profilePictureUrl = friend.profilePic,
                onClick = { onProfilePictureClick(friend) }
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
            Column(
                modifier = Modifier.weight(1f), // Expand to push DeleteFriendButton to the end
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(bottom = AppDimensions.smallPadding)
                        .testTag("friendName#${friend.uid}")
                )
                Text(
                    text = friend.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("friendBio#${friend.uid}")
                )
                Spacer(modifier = Modifier.height(AppDimensions.smallPadding))
                // Display streak or last login message
                if (displayedStreak > 0) {
                    // Display Whatshot icon with streak count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot, // Using Whatshot as the fire icon
                            contentDescription = "Active Streak",
                            tint = AppColors.amber,
                            modifier = Modifier.size(AppDimensions.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
                        Text(
                            text =
                            "$displayedStreak day${if (displayedStreak > 1) "s" else ""} streak",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.amber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                            Modifier
                                .testTag("friendStreak")
                                .weight(1f) // Allow the text to take available space
                        )
                    }
                } else {
                    // Display last login message
                    Text(
                        text =
                        "Last login ${daysSinceLastLogin} day${if (daysSinceLastLogin > 1) "s" else ""} ago",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("friendLastLogin#${friend.uid}")
                    )
                }
            }
            DeleteFriendButton(friend = friend, userProfileViewModel = userProfileViewModel)
        }
    }
}

/**
 * Composable function to display a profile picture with a circular shape. Uses Coil to load the
 * image asynchronously.
 *
 * @param profilePictureUrl The URL of the profile picture to display. Defaults to a placeholder if
 *   null.
 * @param onClick Action to be performed when the profile picture is clicked.
 */
@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {
    val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)
    Image(
        painter = painter,
        contentDescription = "Profile Picture",
        contentScale = ContentScale.Crop,
        modifier =
        Modifier
            .size(AppDimensions.buttonHeight)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("profilePicture")
    )
}

/**
 * Button triggering the removing of a friend in the user's friend list.
 *
 * @param friend The friend to be removed.
 * @param userProfileViewModel The view model for the user's profile.
 */
@Composable
fun DeleteFriendButton(friend: UserProfile, userProfileViewModel: UserProfileViewModel) {
    val context = LocalContext.current

    IconButton(
        onClick = {
            userProfileViewModel.deleteFriend(friend)
            Toast.makeText(
                context, "${friend.name} has been removed from your friends.", Toast.LENGTH_SHORT
            ).show()
        },
        modifier = Modifier.testTag("deleteFriendButton#${friend.uid}")
    ) {
        Icon(
            imageVector = Icons.Default.Delete, // Built-in delete icon
            contentDescription = "Delete",
            tint = Color.Red
        )
    }
}

/**
 * Computes the current streak of a friend based on their last login date and current streak.
 *
 * @param lastLoginDateString The last login date as a string in "yyyy-MM-dd" format. Can be null.
 * @param currentStreak The current streak value.
 * @return The streak to be displayed: either currentStreak or 0.
 */
fun currentFriendStreak(lastLoginDateString: String?, currentStreak: Long): Long {
    if (!lastLoginDateString.isNullOrEmpty()) {
        val lastLoginDate = parseDate(lastLoginDateString) ?: return 0L
        val currentDate = getCurrentDate()
        val daysDifference = getDaysDifference(lastLoginDate, currentDate)
        return when (daysDifference) {
            0L,
            1L -> currentStreak // Same day or consecutive day login
            else -> 0L // Streak broken
        }
    }
    return -1L // No last login date recorded
}
/**
 * Composable function that represents a single friend request item in the list.
 * Displays the friend's profile picture, name, bio, and options to accept or decline the request.
 *
 * @param friendRequest The [UserProfile] object representing the friend who sent the request.
 * @param userProfileViewModel The [UserProfileViewModel] that handles accepting or declining requests.
 */
@Composable
fun FriendRequestItem(
    friendRequest: UserProfile,
    userProfileViewModel: UserProfileViewModel
) {
    Surface(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.smallPadding)
            .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
            .testTag("friendRequestItem#${friendRequest.uid}"),
        color = AppColors.LightPurpleGrey,
        shadowElevation = AppDimensions.elevationSmall
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(AppDimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Friend's Profile Picture
            ProfilePicture(
                profilePictureUrl = friendRequest.profilePic,
                onClick = { /* Optionally, show enlarged picture */ }
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Friend's Name
                Text(
                    text = friendRequest.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(bottom = AppDimensions.smallPadding)
                        .testTag("friendRequestName#${friendRequest.uid}")
                )
                // Friend's Bio
                Text(
                    text = friendRequest.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("friendRequestBio#${friendRequest.uid}")
                )
            }
            // Accept Friend Request Button
            IconButton(
                onClick = {
                    userProfileViewModel.acceptFriend(friendRequest)
                },
                modifier = Modifier
                    .size(AppDimensions.iconSizeMedium)
                    .testTag("acceptFriendButton#${friendRequest.uid}")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept Friend Request",
                    tint = Color.Green
                )
            }
            // Decline Friend Request Button
            IconButton(
                onClick = {
                    userProfileViewModel.declineFriendRequest(friendRequest)
                },
                modifier = Modifier
                    .size(AppDimensions.iconSizeMedium)
                    .testTag("declineFriendButton#${friendRequest.uid}")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Decline Friend Request",
                    tint = Color.Red
                )
            }
        }
    }
}