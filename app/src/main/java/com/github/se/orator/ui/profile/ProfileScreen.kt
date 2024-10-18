package com.github.se.orator.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * Composable function to display the profile screen.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param profileViewModel ViewModel for managing user profile data.
 */
@Composable
fun ProfileScreen(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {
  // State to control whether the profile picture dialog is open
  var isDialogOpen by remember { mutableStateOf(false) }

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = Color.White,
            contentColor = Color.Black,
            elevation = 4.dp,
            title = { Text(text = "Profile", fontWeight = FontWeight.Bold) },
            actions = {
              IconButton(onClick = { navigationActions.navigateTo(Screen.SETTINGS) }) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(32.dp))
              }
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    // Sign out the user

                  }) {
                    Image(
                        painter = painterResource(id = R.drawable.sign_out),
                        contentDescription = "Sign out",
                        modifier = Modifier.size(32.dp))
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              userProfile?.let { profile ->
                // Profile Picture with clickable action to open it in larger format
                ProfilePicture(
                    profilePictureUrl = profile.profilePic, onClick = { isDialogOpen = true })

                Spacer(modifier = Modifier.height(8.dp))

                // Username
                Text(text = profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                Button(onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
                  Text(text = "Edit Profile")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Achievements Section
                CardSection(
                    title = "Achievements",
                    iconId = R.drawable.trophy_frame,
                    onClick = { /*TODO: Handle achievements click */})

                Spacer(modifier = Modifier.height(16.dp))

                // Old Training Sessions Section
                CardSection(
                    title = "Previous Sessions",
                    iconId = R.drawable.history_frame,
                    onClick = { /*TODO: Handle previous sessions click */})
              }
                  ?: run {
                    // Show a loading state if the profile is not yet available
                    Text("Loading profile...")
                  }
            }

        // Dialog to show the profile picture in larger format
        if (isDialogOpen && userProfile?.profilePic != null) {
          ProfilePictureDialog(
              profilePictureUrl = userProfile!!.profilePic!!, onDismiss = { isDialogOpen = false })
        }
      }
}

/**
 * Composable function to display a profile picture.
 *
 * @param profilePictureUrl URL of the profile picture.
 * @param onClick Callback to handle click events.
 */
@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {
  val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)

  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          Modifier.size(100.dp)
              .clip(CircleShape)
              .clickable(onClick = onClick)
              .testTag("upload_profile_picture"))
}

/**
 * Composable function to display a dialog with a larger profile picture.
 *
 * @param profilePictureUrl URL of the profile picture.
 * @param onDismiss Callback to handle dismiss events.
 */
@Composable
fun ProfilePictureDialog(profilePictureUrl: String, onDismiss: () -> Unit) {
  Dialog(onDismissRequest = { onDismiss() }) {
    Box(
        modifier =
            Modifier.fillMaxSize().padding(16.dp).clickable {
              onDismiss()
            } // Dismiss the dialog when clicked outside
        ) {
          Image(
              painter = rememberAsyncImagePainter(model = profilePictureUrl),
              contentDescription = "Large Profile Picture",
              contentScale =
                  ContentScale
                      .Crop, // Ensures the image fills the shape and is cropped if necessary
              modifier =
                  Modifier.size(200.dp) // Set the size to twice the original (100.dp * 2 = 200.dp)
                      .align(Alignment.Center)
                      .clip(CircleShape) // Keep the circular shape
              )
        }
  }
}

/**
 * Composable function to display a card section with an icon and title.
 *
 * @param title Title of the card section.
 * @param iconId Resource ID of the icon.
 * @param onClick Callback to handle click events.
 * @param modifier Modifier to be applied to the card.
 */
@Composable
fun CardSection(title: String, iconId: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Card(
      modifier = modifier.fillMaxWidth().height(100.dp).clickable { onClick() }, elevation = 4.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
          Image(
              painter = painterResource(id = iconId),
              contentDescription = title,
              modifier =
                  Modifier.size(24.dp)
                      .testTag("titleIcon") // Test tag for icons in the card sections
              )
          Spacer(modifier = Modifier.width(16.dp))
          Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
      }
}
