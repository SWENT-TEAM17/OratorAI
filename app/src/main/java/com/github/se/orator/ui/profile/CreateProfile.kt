package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations

/**
 * The CreateAccountScreen composable is a composable screen that displays the create account
 * screen.
 *
 * @param navigationActions The navigation actions that can be performed.
 * @param userProfileViewModel The view model for the user profile.
 */
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateAccountScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  // State for storing the username input
  var username by remember { mutableStateOf("") }
  // State for controlling the dialog visibility
  var isDialogOpen by remember { mutableStateOf(false) }
  // State variable to hold the selected image URI
  var profilePicUri by remember { mutableStateOf<Uri?>(null) }
  // State for tracking the upload status
  var isUploading by remember { mutableStateOf(false) }

  val context = LocalContext.current

  // Launcher for picking an image from the gallery
  val pickImageLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
          profilePicUri = it // Update the profilePicUri to display the image
        }
      }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Create an OratorAI account") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp).testTag("back_button"))
              }
            },
            backgroundColor = Color.White,
            contentColor = Color.Black)
      },
      content = {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {

              // Profile Picture
              Box(contentAlignment = Alignment.Center) {
                ProfilePicture(
                    profilePictureUrl = profilePicUri?.toString(),
                    onClick = { isDialogOpen = true })
                IconButton(
                    onClick = { isDialogOpen = true },
                    modifier = Modifier.size(32.dp).align(Alignment.BottomEnd)) {
                      Image(
                          painter = painterResource(id = R.drawable.camera),
                          contentDescription = "Upload profile picture",
                          modifier = Modifier.size(32.dp).testTag("upload_profile_picture"))
                    }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(text = "Profile picture (optional)", fontSize = 14.sp, color = Color.Gray)

              Spacer(modifier = Modifier.height(24.dp))

              // Username Input Field
              TextField(
                  value = username,
                  onValueChange = { username = it },
                  label = { Text("Username") },
                  placeholder = { Text("Enter your username") },
                  modifier = Modifier.fillMaxWidth().testTag("username_input"),
                  singleLine = true)

              Spacer(modifier = Modifier.height(48.dp))

              // Save Button
              Button(
                  onClick = {
                    if (username.isNotBlank()) {
                      isUploading = true
                      val uid = userProfileViewModel.repository.getCurrentUserUid().toString()

                      // Create the new user profile without the profilePic URL initially
                      val newProfile =
                          UserProfile(
                              uid = uid,
                              name = username,
                              age = 0, // TODO: Add age input field if needed
                              profilePic = null,
                              statistics = UserStatistics(),
                              friends = emptyList())

                      // Add the user profile to Firestore
                      userProfileViewModel.addUserProfile(newProfile)

                      // If a profile picture is selected, upload it
                      if (profilePicUri != null) {
                        userProfileViewModel.uploadProfilePicture(uid, profilePicUri!!)
                      }

                      isUploading = false
                      // Navigate to home screen
                      navigationActions.navigateTo(TopLevelDestinations.HOME)
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
                  shape = CircleShape,
                  enabled = username.isNotBlank() && !isUploading) {
                    Text(text = "Save profile", fontWeight = FontWeight.Bold)
                  }
            }
      })

  // Dialog for choosing between camera and gallery
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
