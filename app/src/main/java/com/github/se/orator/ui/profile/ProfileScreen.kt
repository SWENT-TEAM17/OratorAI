package com.github.se.orator.ui.profile

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun ProfileScreen(
    profilePictureUrl: String?, // URL for profile picture
    username: String, // Username to be displayed
    onEditProfile: () -> Unit, // Callback when edit profile is clicked
    onAchievementsClick: () -> Unit, // Callback for achievements section
    onOldSessionsClick: () -> Unit, // Callback for old training sessions
    onSettingsClick: () -> Unit // Callback for settings icon
) {
  // State to control whether the profile picture dialog is open
  var isDialogOpen by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = Color.White,
            contentColor = Color.Black,
            elevation = 4.dp,
            title = { Text(text = "Profile", fontWeight = FontWeight.Bold) },
            actions = {
              IconButton(onClick = { onSettingsClick() }) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(32.dp))
              }
            },
            navigationIcon = {
              IconButton(onClick = { /* Handle left icon click */}) {
                Image(
                    painter = painterResource(id = R.drawable.sign_out),
                    contentDescription = "Sign out",
                    modifier = Modifier.size(32.dp))
              }
            })
      },
      bottomBar = { BottomNavigationBar() }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Profile Picture with clickable action to open it in larger format
              ProfilePicture(
                  profilePictureUrl = profilePictureUrl, onClick = { isDialogOpen = true })

              Spacer(modifier = Modifier.height(8.dp))

              // Username
              Text(text = username, fontSize = 20.sp, fontWeight = FontWeight.Bold)

              Spacer(modifier = Modifier.height(16.dp))

              // Edit Profile Button
              Button(onClick = { onEditProfile() }) { Text(text = "Edit Profile") }

              Spacer(modifier = Modifier.height(24.dp))

              // Achievements Section
              CardSection(
                  title = "Achievements",
                  iconId = R.drawable.trophy_frame,
                  onClick = onAchievementsClick)

              Spacer(modifier = Modifier.height(16.dp))

              // Old Training Sessions Section
              CardSection(
                  title = "Previous Sessions",
                  iconId = R.drawable.history_frame,
                  onClick = onOldSessionsClick)
            }

        // Dialog to show the profile picture in larger format
        if (isDialogOpen && profilePictureUrl != null) {
          ProfilePictureDialog(
              profilePictureUrl = profilePictureUrl, onDismiss = { isDialogOpen = false })
        }
      }
}

@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {
  val painter =
      if (profilePictureUrl != null) {
        rememberAsyncImagePainter(model = profilePictureUrl)
      } else {
        painterResource(id = R.drawable.profile_picture)
      }

  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          Modifier.size(128.dp)
              .clip(CircleShape)
              .clickable { onClick() } // Trigger the click action to open the image
              .testTag("ProfilePicture") // Test tag for profile picture
      )
}

@Composable
fun ProfilePictureDialog(profilePictureUrl: String, onDismiss: () -> Unit) {
  Dialog(onDismissRequest = { onDismiss() }) {
    Box(
        modifier =
            Modifier.fillMaxSize().padding(16.dp).clickable {
              onDismiss()
            } // Dismiss the dialog when the user clicks outside
        ) {
          Image(
              painter = rememberAsyncImagePainter(model = profilePictureUrl),
              contentDescription = "Large Profile Picture",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().align(Alignment.Center).clip(CircleShape))
        }
  }
}

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

@Composable
fun BottomNavigationBar() {
  BottomNavigation(backgroundColor = Color.White, contentColor = Color.Black) {
    BottomNavigationItem(
        icon = {
          Image(
              painter = painterResource(id = R.drawable.home),
              contentDescription = "Home",
              modifier = Modifier.testTag("HomeButton") // Test tag for Home button
              )
        },
        selected = true,
        onClick = { /* Handle home click */})
    BottomNavigationItem(
        icon = {
          Image(
              painter = painterResource(id = R.drawable.profile),
              contentDescription = "Profile",
              modifier = Modifier.testTag("ProfileButton") // Test tag for Profile button
              )
        },
        selected = false,
        onClick = { /* Handle profile click */})
    BottomNavigationItem(
        icon = {
          Image(
              painter = painterResource(id = R.drawable.friends),
              contentDescription = "Friends",
              modifier = Modifier.testTag("FriendsButton") // Test tag for Friends button
              )
        },
        selected = false,
        onClick = { /* Handle settings click */})
  }
}
