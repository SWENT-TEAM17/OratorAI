package com.github.se.orator.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen

/**
 * Composable function for editing the user profile.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param userProfileViewModel ViewModel for managing user profile data.
 */
@Composable
fun EditProfileScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  // Fetch the user's profile data
  val userProfile by userProfileViewModel.userProfile.collectAsState()

  // States for username, bio, and dialog visibility
  var isDialogOpen by remember { mutableStateOf(false) }
  var updatedUsername by remember(key1 = userProfile) { mutableStateOf(userProfile?.name ?: "") }
  var updatedBio by remember(key1 = userProfile) { mutableStateOf(userProfile?.bio ?: "") }

  // State to hold the new profile picture URI
  var newProfilePicUri by remember { mutableStateOf<Uri?>(null) }

  val context = LocalContext.current

  // Create a launcher for picking an image from the gallery
  val pickImageLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newProfilePicUri = it }
      }

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = Color.White,
            contentColor = Color.Black,
            elevation = 4.dp,
            title = { Text(text = "Edit Profile", fontWeight = FontWeight.Bold) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp).testTag("back_button"))
              }
            },
            actions = {
              IconButton(onClick = { navigationActions.navigateTo(Screen.SETTINGS) }) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(32.dp).testTag("settings_button"))
              }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Profile Picture with Camera Icon Overlay
              Box(contentAlignment = Alignment.Center) {
                ProfilePicture(
                    profilePictureUrl = newProfilePicUri?.toString() ?: userProfile?.profilePic,
                    onClick = { isDialogOpen = true })
                IconButton(
                    onClick = { isDialogOpen = true },
                    modifier = Modifier.size(32.dp).align(Alignment.BottomEnd)) {
                      Image(
                          painter = painterResource(id = R.drawable.camera),
                          contentDescription = "Change Profile Picture",
                          modifier = Modifier.size(32.dp))
                    }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Username Input Field
              OutlinedTextField(
                  value = updatedUsername,
                  onValueChange = { newUsername -> updatedUsername = newUsername },
                  label = { Text("Username") },
                  modifier = Modifier.fillMaxWidth().testTag("username_field"))

              Spacer(modifier = Modifier.height(16.dp))

              // Bio Input Field
              Text(
                  text = "BIO",
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.align(Alignment.Start))
              OutlinedTextField(
                  value = updatedBio,
                  onValueChange = { newBio -> updatedBio = newBio },
                  placeholder = { Text("Tell us about yourself") },
                  modifier = Modifier.fillMaxWidth().height(150.dp).testTag("bio_field"),
                  maxLines = 5)

              Spacer(modifier = Modifier.height(24.dp))

              // Save Changes Button
              Button(
                  onClick = {
                    // Save the updated profile information
                    val updatedProfile = userProfile?.copy(name = updatedUsername, bio = updatedBio)
                    if (updatedProfile != null) {
                      if (newProfilePicUri != null) {
                        // Upload the new profile picture
                        userProfile?.let { it1 ->
                          userProfileViewModel.uploadProfilePicture(it1.uid, newProfilePicUri!!)
                        }
                      }
                      // Update the profile
                      userProfileViewModel.createOrUpdateUserProfile(updatedProfile)
                    }
                    navigationActions.goBack()
                  },
                  modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(text = "Save changes")
                  }
            }
      }

  // Display the dialog to choose an image from the gallery
  if (isDialogOpen) {
    ChoosePictureDialog(
        onDismiss = { isDialogOpen = false },
        onTakePhoto = {
          isDialogOpen = false
          Toast.makeText(context, "Taking a photo is not supported yet.", Toast.LENGTH_SHORT).show()
        },
        onPickFromGallery = {
          isDialogOpen = false
          pickImageLauncher.launch("image/*")
        })
  }
}

/**
 * Composable function to display a dialog for choosing a profile picture.
 *
 * @param onDismiss Callback to dismiss the dialog.
 * @param onTakePhoto Callback to take a photo using the camera.
 * @param onPickFromGallery Callback to pick an image from the gallery.
 */
@Composable
fun ChoosePictureDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
  AlertDialog(
      onDismissRequest = { onDismiss() },
      title = { Text("Choose Profile Picture") },
      text = { Text("Select an option to update your profile picture.") },
      buttons = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("upload_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Button(onClick = { onTakePhoto() }) { Text("Take Photo") }
              Spacer(modifier = Modifier.height(8.dp))
              Button(onClick = { onPickFromGallery() }) { Text("Upload from Gallery") }
              Spacer(modifier = Modifier.height(8.dp))
              Button(onClick = { onDismiss() }) { Text("Cancel") }
            }
      })
}
