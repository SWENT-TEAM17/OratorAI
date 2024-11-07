package com.github.se.orator.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.LightPurpleGrey
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
    val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val filteredFriends = friendsProfiles.filter { friend -> friend.name.contains(searchQuery, ignoreCase = true) }

    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
                    Text("Actions", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = {
                        scope.launch {
                            drawerState.close()
                            navigationActions.navigateTo(Screen.ADD_FRIENDS)
                        }
                    }) {
                        Text("➕ Add a friend")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = {
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
                        title = { Text("My Friends") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
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
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .clickable { focusManager.clearFocus() }
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 20.dp) // Apply padding to the container
                ) {
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
                            .fillMaxWidth(0.55f)
                            .height(64.dp)
                            .focusRequester(focusRequester)
                    )
                }

                if (filteredFriends.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No user found", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFriends) { friend ->
                            FriendItem(friend = friend)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItem(friend: UserProfile) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)  // Side padding for each item
            .clip(RoundedCornerShape(20.dp))
        ,
        color = LightPurpleGrey,
        shadowElevation = 4.dp  // Subtle shadow with low elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ProfilePicture(profilePictureUrl = friend.profilePic, onClick = {})
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = friend.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = friend.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.LightGray)
            .clickable(onClick = onClick)
    )
}
