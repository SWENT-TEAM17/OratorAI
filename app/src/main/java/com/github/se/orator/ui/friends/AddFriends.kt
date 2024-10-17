package com.github.se.orator.ui.friends

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val allProfiles by userProfileViewModel.allProfiles.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Background color
                    .padding(horizontal = 16.dp, vertical = 12.dp), // Padding for proper spacing
                verticalAlignment = Alignment.CenterVertically // Center the content vertically
            ) {
                IconButton(onClick = { navigationActions.goBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Add a Friend")
            }
        },
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute()
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(100.dp)
        ) {
            // Text field with search icon and clear button
            OutlinedTextField(
                value = query,
                onValueChange = { newValue ->
                    query = newValue
                    expanded = newValue.isNotEmpty()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Username") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Icon")
                        }
                    }
                },
                singleLine = true,
                keyboardActions = KeyboardActions.Default
            )
            if (query != "") {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        allProfiles.filter { profile -> (profile.name.contains(query)) }
                    )
                    { user -> UserItem(user = user, userProfileViewModel) }
                }
            }
        }
    }
}

/**
 * A composable function that represents a single user item in a list.
 * Displays the user's profile picture, name, and bio, and allows adding the user as a friend.
 *
 * @param user The [UserProfile] object representing the user being displayed.
 * @param userProfileViewModel The [UserProfileViewModel] that handles the logic of adding a user as a friend.
 */
@Composable
fun UserItem(user: UserProfile, userProfileViewModel: UserProfileViewModel) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Displays the profile picture and allows the user to be added as a friend when clicked
        ProfilePicture(profilePictureUrl = user.profilePic,
            onClick = { userProfileViewModel.addFriend(user) })
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            // Displays the user's name
            Text(text = user.name, style = MaterialTheme.typography.titleMedium)
            // Displays the user's bio, or a default text if the bio is null
            Text(
                text = user.bio ?: "No bio available",
                style = MaterialTheme.typography.bodySmall, color = Color.Gray
            )
        }
    }
}

/**
 * A composable function that displays a profile picture.
 * If no profile picture URL is provided, a default image is shown.
 * The image is clickable, triggering the provided [onClick] function.
 *
 * @param profilePictureUrl The URL of the profile picture to be displayed. If null, a default image is shown.
 * @param onClick A lambda function that is triggered when the profile picture is clicked.
 */
@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {
    val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)
    Image(
        painter = painter, contentDescription = "Profile Picture",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    )
}

