package com.github.se.orator.ui.friends

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
import androidx.compose.material.icons.filled.Delete
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
import kotlinx.coroutines.launch

/**
 * Composable function that displays the "View Friends" screen, showing a list of friends with a
 * search bar and options to navigate to other screens like "Add Friends" and "Leaderboard."
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

    ProjectTheme {
        ModalNavigationDrawer(
            modifier = Modifier.testTag("viewFriendsDrawerMenu"),
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(AppDimensions.paddingMedium)
                    ) {
                        Text(
                            "Actions",
                            modifier = Modifier.testTag("viewFriendsDrawerTitle"),
                            style = MaterialTheme.typography.titleMedium
                        )
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
                                    modifier = Modifier.testTag("myFriendsTitle")
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("viewFriendsMenuButton")
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
                        selectedItem = Route.FRIENDS
                    )
                }) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = AppDimensions.paddingMedium)
                        .clickable { focusManager.clearFocus() }
                ) {
                    // Search bar for filtering friends
                    Box(modifier = Modifier.padding(vertical = AppDimensions.paddingMediumSmall)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search for a friend.") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon"
                                )
                            },
                            modifier = Modifier
                                .wrapContentWidth()
                                .horizontalScroll(rememberScrollState())
                                .height(AppDimensions.mediumHeight)
                                .focusRequester(focusRequester)
                                .testTag("viewFriendsSearch")
                        )
                    }

                    // Display message if no friends match the search query
                    if (filteredFriends.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("noUserFound"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No user found",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.testTag("noUserFoundText")
                            )
                        }
                    } else {
                        // Display the list of friends if any match the search query
                        LazyColumn(
                            modifier = Modifier.testTag("viewFriendsList"),
                            contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                            verticalArrangement =
                            Arrangement.spacedBy(AppDimensions.paddingSmall)
                        ) {
                            items(filteredFriends) { friend ->
                                FriendItem(
                                    friend = friend,
                                    userProfileViewModel = userProfileViewModel,
                                    onProfilePictureClick = { selectedFriend = it }
                                )
                            }
                        }
                    }

                    // Dialog to display the enlarged profile picture
                    if (selectedFriend != null && !selectedFriend!!.profilePic.isNullOrEmpty()) {
                        ProfilePictureDialog(
                            profilePictureUrl = selectedFriend!!.profilePic!!,
                            onDismiss = { selectedFriend = null }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function that represents a single friend item in the list. Displays the friend's
 * profile picture, name, and bio.
 *
 * @param friend The [UserProfile] object representing the friend being displayed.
 * @param onProfilePictureClick Callback when the profile picture is clicked.
 */
@Composable
fun FriendItem(
    friend: UserProfile,
    userProfileViewModel: UserProfileViewModel,
    onProfilePictureClick: (UserProfile) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.smallPadding)
            .clip(RoundedCornerShape(AppDimensions.paddingMediumSmall))
            .testTag("viewFriendsItem#${friend.uid}"),
        color = AppColors.LightPurpleGrey,
        shadowElevation = AppDimensions.elevationSmall // Subtle shadow with low elevation
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimensions.paddingMedium)) {
            ProfilePicture(
                profilePictureUrl = friend.profilePic,
                onClick = { onProfilePictureClick(friend) }
            )
            Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
            Column {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier =
                    Modifier
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
            }
            Spacer(modifier = Modifier.weight(1f)) // Pushes the delete button to the right

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
                context,
                "${friend.name} has been removed from your friends.",
                Toast.LENGTH_SHORT
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
