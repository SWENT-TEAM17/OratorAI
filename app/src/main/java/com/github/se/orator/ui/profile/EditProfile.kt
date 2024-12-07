package com.github.se.orator.ui.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme // Changed from material3 to material
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.AppShapes

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

  // -----------------------------------------------------------
  // Added camera-related variables and launchers here

  // Temporary variable to hold the pending image URI
  var pendingImageUri by remember { mutableStateOf<Uri?>(null) }

  // Launcher to take a picture using the camera
  val takePictureLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
          // Only set newProfilePicUri if capture was successful
          pendingImageUri?.let { uri ->
            newProfilePicUri = uri
            pendingImageUri = null
          }
        } else {
          Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
        }
      }

  // State to track if the user initiated a camera request
  var isCameraRequested by remember { mutableStateOf(false) }

  // Request camera permission launcher
  val cameraPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && isCameraRequested) {
          isCameraRequested = false
          // Launch camera with the pending URI
          pendingImageUri?.let { uri -> takePictureLauncher.launch(uri) }
        } else if (!granted && isCameraRequested) {
          isCameraRequested = false
          Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
      }
  // -----------------------------------------------------------

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().testTag("edit_profile_app_bar"),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = AppDimensions.elevationSmall,
            title = {
              Text(
                  modifier = Modifier.testTag("edit_profile_title"),
                  text = "Edit Profile",
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colors.onSurface,
              )
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Icon(
                        Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back arrow",
                        modifier = Modifier.size(AppDimensions.iconSizeMedium),
                        tint = MaterialTheme.colors.onSurface)
                  }
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settings_button")) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint =
                            MaterialTheme.colors.onSurface.copy(
                                alpha = 0.7f) // replacing onSurfaceVariant
                        )
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      },
      backgroundColor = MaterialTheme.colors.background) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(AppDimensions.paddingMedium), // Replaced 16.dp
            horizontalAlignment = Alignment.CenterHorizontally) {

              // Profile Picture with Camera Icon Overlay
              Box(contentAlignment = Alignment.Center) {
                ProfilePicture(
                    profilePictureUrl = newProfilePicUri?.toString() ?: userProfile?.profilePic,
                    onClick = { isDialogOpen = true })

                // Edit profile picture button
                Button(
                    onClick = { isDialogOpen = true },
                    modifier =
                        Modifier.testTag("upload_profile_picture_button")
                            .width(40.dp)
                            .height(40.dp)
                            .align(Alignment.BottomEnd),
                    shape = AppShapes.circleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(0.dp)) {
                      Icon(
                          Icons.Outlined.PhotoCamera,
                          contentDescription = "Edit button",
                          modifier =
                              Modifier.size(AppDimensions.iconSizeMedium).testTag("edit_button"),
                          tint = MaterialTheme.colors.primary)
                    }
              }

              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 16.dp

              // Username Input Field
              OutlinedTextField(
                  value = updatedUsername,
                  onValueChange = { newUsername -> updatedUsername = newUsername },
                  label = { Text("Username", modifier = Modifier.testTag("UsernameText")) },
                  modifier = Modifier.fillMaxWidth().testTag("username_field"),
                  singleLine = true,
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          backgroundColor = MaterialTheme.colors.surface,
                          textColor = MaterialTheme.colors.onSurface,
                          focusedBorderColor = MaterialTheme.colors.primary,
                          unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                          cursorColor = MaterialTheme.colors.primary,
                          focusedLabelColor = MaterialTheme.colors.primary,
                          unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)))

              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 16.dp

              // Bio Input Field
              OutlinedTextField(
                  value = updatedBio,
                  onValueChange = { newBio -> updatedBio = newBio },
                  label = { Text("Bio", modifier = Modifier.testTag("bio_text")) },
                  placeholder = { Text("Tell us about yourself") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(AppDimensions.bioFieldHeight) // Replaced 150.dp
                          .testTag("bio_field"),
                  maxLines = 5,
                  colors =
                      TextFieldDefaults.outlinedTextFieldColors(
                          backgroundColor = MaterialTheme.colors.surface,
                          textColor = MaterialTheme.colors.onSurface,
                          focusedBorderColor = MaterialTheme.colors.primary,
                          unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                          cursorColor = MaterialTheme.colors.primary,
                          focusedLabelColor = MaterialTheme.colors.primary,
                          unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)))

              Spacer(modifier = Modifier.height(AppDimensions.paddingLarge)) // Replaced 24.dp

              // Save Changes Button
              Button(
                  onClick = {
                    // Save the updated profile information
                    val updatedProfile = userProfile?.copy(name = updatedUsername, bio = updatedBio)
                    if (updatedProfile != null) {
                      if (newProfilePicUri != null) {
                        // Upload the new profile picture
                        userProfile?.let { nonNullUser ->
                          userProfileViewModel.uploadProfilePicture(
                              nonNullUser.uid, newProfilePicUri!!)
                        }
                      }
                      // Update the profile
                      userProfileViewModel.createOrUpdateUserProfile(updatedProfile)
                    }
                    navigationActions.goBack()
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(AppDimensions.buttonHeightLarge) // Replaced 50.dp
                          .testTag("save_profile_button"),
                  shape = AppShapes.circleShape, // Replaced CircleShape,
                  colors =
                      ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)) {
                    Text(
                        modifier = Modifier.testTag("save_profile_button_text"),
                        text = "Save changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppFontSizes.bodyLarge, // Replaced 16.sp
                        color = MaterialTheme.colors.onPrimary)
                  }
            }

        // Dialog for choosing between camera and gallery
        if (isDialogOpen) {
          ChoosePictureDialog(
              onDismiss = { isDialogOpen = false },
              onTakePhoto = {
                isDialogOpen = false
                // Check camera permission
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                  // Launch camera with pendingImageUri
                  val uri = createImageFileUri(context)
                  if (uri != null) {
                    pendingImageUri = uri
                    takePictureLauncher.launch(uri)
                  } else {
                    Toast.makeText(context, "Failed to create image file.", Toast.LENGTH_SHORT)
                        .show()
                  }
                } else {
                  // Request camera permission
                  isCameraRequested = true
                  cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
              },
              onPickFromGallery = {
                isDialogOpen = false
                pickImageLauncher.launch("image/*")
              })
        }
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
      title = {
        Text(
            "Choose Profile Picture",
            modifier = Modifier.testTag("ProfilePictureTitle"),
            color = MaterialTheme.colors.onSurface)
      },
      text = {
        Text(
            "Select an option to update your profile picture.",
            modifier = Modifier.testTag("ProfilePictureButton"),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f) // replaced variant
            )
      },
      backgroundColor = MaterialTheme.colors.surface,
      buttons = {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(AppDimensions.paddingSmallMedium) // Replaced 16.dp
                    .testTag("upload_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Button(
                  onClick = { onTakePhoto() },
                  modifier = Modifier.testTag("PhotoOnTake"),
                  colors =
                      ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)) {
                    Text(
                        "Take Photo",
                        modifier = Modifier.testTag("TakePhotoText"),
                        color = MaterialTheme.colors.onPrimary)
                  }
              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 8.dp
              Button(
                  onClick = { onPickFromGallery() },
                  modifier = Modifier.testTag("PhotoOnPick"),
                  colors =
                      ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)) {
                    Text(
                        "Upload from Gallery",
                        modifier = Modifier.testTag("UploadGalleryText"),
                        color = MaterialTheme.colors.onPrimary)
                  }
              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 8.dp
              Button(
                  onClick = { onDismiss() },
                  modifier = Modifier.testTag("PhotoOnDismiss"),
                  colors =
                      ButtonDefaults.buttonColors(
                          backgroundColor = MaterialTheme.colors.secondary)) {
                    Text(
                        "Cancel",
                        modifier = Modifier.testTag("CancelText"),
                        color = MaterialTheme.colors.onSecondary)
                  }
            }
      })
}

// -----------------------------------------------------------
// Added these functions at the bottom without removing anything:

/**
 * Helper function to launch the camera after creating a file URI.
 *
 * @param context The current context.
 * @param takePictureLauncher The lambda to handle launching the camera.
 * @param onUriCreated Callback to handle the created URI.
 */
private fun launchCamera(
    context: Context,
    takePictureLauncher: (Uri) -> Unit,
    onUriCreated: (Uri) -> Unit
) {
  val uri = createImageFileUri(context)
  if (uri != null) {
    onUriCreated(uri)
    takePictureLauncher(uri)
  } else {
    Toast.makeText(context, "Failed to create image file.", Toast.LENGTH_SHORT).show()
  }
}
