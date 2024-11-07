package com.github.se.orator.ui.friends

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.LightPurpleGrey

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  var query by remember { mutableStateOf("") }
  var expanded by remember { mutableStateOf(false) }
  val allProfiles by userProfileViewModel.allProfiles.collectAsState()
  val focusManager = LocalFocusManager.current
  val focusRequester = FocusRequester()

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
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp).clickable {
                  focusManager.clearFocus()
                }) {
              Box(
                  modifier = Modifier.padding(vertical = 20.dp) // Apply padding to the container
                  ) {
                    // Text field with search icon and clear button
                    OutlinedTextField(
                        value = query,
                        onValueChange = { newValue ->
                          query = newValue
                          expanded = newValue.isNotEmpty()
                        },
                        modifier =
                            Modifier.fillMaxWidth(0.55f)
                                .height(64.dp)
                                .focusRequester(focusRequester)
                                .testTag("addFriendSearchField"),
                        label = {
                          Text("Username", modifier = Modifier.testTag("searchFieldLabel"))
                        },
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
                  }
              if (query.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("searchResultsList")) {
                      items(
                          allProfiles.filter { profile ->
                            profile.name.contains(query, ignoreCase = true)
                          }) { user ->
                            UserItem(user = user, userProfileViewModel = userProfileViewModel)
                          }
                    }
              }
            }
      }
}

/**
 * A composable function that represents a single user item in a list. Displays the user's profile
 * picture, name, and bio, and allows adding the user as a friend.
 *
 * @param user The [UserProfile] object representing the user being displayed.
 * @param userProfileViewModel The [UserProfileViewModel] that handles the logic of adding a user as
 *   a friend.
 */
@Composable
fun UserItem(user: UserProfile, userProfileViewModel: UserProfileViewModel) {
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 4.dp) // Side padding for each item
              .clip(RoundedCornerShape(20.dp)),
      color = LightPurpleGrey,
      shadowElevation = 4.dp // Subtle shadow with low elevation
      ) {
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(16.dp).testTag("addFriendUserItem#${user.uid}")) {
              ProfilePicture(
                  profilePictureUrl = user.profilePic,
                  onClick = { userProfileViewModel.addFriend(user) })
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp))
                Text(
                    text = user.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }
            }
      }
}
