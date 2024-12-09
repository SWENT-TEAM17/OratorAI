package com.github.se.orator.ui.profile

import android.Manifest
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.MaterialTheme // Changed from material3 to material
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.AppShapes
import java.io.File

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

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().testTag("edit_profile_app_bar"),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
            elevation = AppDimensions.elevationSmall,
            title = {
              Text(
                  modifier = Modifier.testTag("edit_profile_title"),
                  text = "Edit Profile",
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
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
                        tint = MaterialTheme.colorScheme.onSurface)
                  }
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settings_button")) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Logout icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      },
      backgroundColor = MaterialTheme.colorScheme.background) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(it)
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
                            .width(AppDimensions.spacingXLarge)
                            .height(AppDimensions.spacingXLarge)
                            .align(Alignment.BottomEnd),
                    shape = AppShapes.circleShape,
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colorScheme.inverseOnSurface),
                    contentPadding = PaddingValues(AppDimensions.nullPadding)) {
                      Icon(
                          Icons.Outlined.PhotoCamera,
                          contentDescription = "Edit button",
                          modifier =
                              Modifier.size(AppDimensions.iconSizeMedium).testTag("edit_button"),
                          tint = MaterialTheme.colorScheme.primary)
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
                          backgroundColor = MaterialTheme.colorScheme.surface,
                          textColor = MaterialTheme.colorScheme.onSurface,
                          focusedBorderColor = MaterialTheme.colorScheme.outline,
                          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                          cursorColor = MaterialTheme.colorScheme.primary,
                          focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                          unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))

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
                          backgroundColor = MaterialTheme.colorScheme.surface,
                          textColor = MaterialTheme.colorScheme.onSurface,
                          focusedBorderColor = MaterialTheme.colorScheme.outline,
                          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                          cursorColor = MaterialTheme.colorScheme.primary,
                          focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                          unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))

              Spacer(modifier = Modifier.height(AppDimensions.paddingLarge)) // Replaced 24.dp

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
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(AppDimensions.buttonHeightLarge) // Replaced 50.dp
                          .testTag("save_profile_button"),
                  shape = AppShapes.circleShape, // Replaced CircleShape,
                  colors =
                      ButtonDefaults.buttonColors(
                          backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text(
                        modifier = Modifier.testTag("save_profile_button_text"),
                        text = "Save changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = AppFontSizes.bodyLarge, // Replaced 16.sp
                        color = MaterialTheme.colorScheme.onSurface)
                  }
            }

        // Integrate the ImagePicker
        ImagePicker(
            isDialogOpen = isDialogOpen,
            onDismiss = { isDialogOpen = false },
            onImageSelected = { uri ->
              newProfilePicUri = uri
              Toast.makeText(context, "Profile picture updated.", Toast.LENGTH_SHORT).show()
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
      title = {
        Text(
            "Choose Profile Picture",
            modifier = Modifier.testTag("ProfilePictureTitle"),
            color = MaterialTheme.colorScheme.onSurface)
      },
      text = {
        Text(
            "Select an option to update your profile picture.",
            modifier = Modifier.testTag("ProfilePictureButton"),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      backgroundColor = MaterialTheme.colorScheme.surface,
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
                      ButtonDefaults.buttonColors(
                          backgroundColor = MaterialTheme.colorScheme.primary)) {
                    Text(
                        "Take Photo",
                        modifier = Modifier.testTag("TakePhotoText"),
                        color = MaterialTheme.colorScheme.primary)
                  }
              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 8.dp
              Button(
                  onClick = { onPickFromGallery() },
                  modifier = Modifier.testTag("PhotoOnPick"),
                  colors =
                      ButtonDefaults.buttonColors(
                          backgroundColor = MaterialTheme.colorScheme.primary)) {
                    Text(
                        "Upload from Gallery",
                        modifier = Modifier.testTag("UploadGalleryText"),
                        color = MaterialTheme.colorScheme.primary)
                  }
              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 8.dp
              Button(
                  onClick = { onDismiss() },
                  modifier = Modifier.testTag("PhotoOnDismiss"),
                  colors =
                      ButtonDefaults.buttonColors(
                          backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text(
                        "Cancel",
                        modifier = Modifier.testTag("CancelText"),
                        color = MaterialTheme.colorScheme.primary)
                  }
            }
      })
}

/**
 * A reusable composable that handles image picking from the gallery or capturing a photo using the
 * camera.
 *
 * @param isDialogOpen Controls the visibility of the image selection dialog.
 * @param onDismiss Callback to dismiss the dialog.
 * @param onImageSelected Callback with the selected image URI.
 */
@Composable
fun ImagePicker(isDialogOpen: Boolean, onDismiss: () -> Unit, onImageSelected: (Uri) -> Unit) {
  val context = LocalContext.current
  var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
  var isCameraRequested by remember { mutableStateOf(false) }

  // Launcher for picking an image from the gallery
  val pickImageLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImageSelected(it) }
      }

  // Launcher to take a picture using the camera
  val takePictureLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
          pendingImageUri?.let { onImageSelected(it) }
          pendingImageUri = null
        } else {
          Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
        }
      }

  // Request camera permission launcher
  val cameraPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && isCameraRequested) {
          isCameraRequested = false
          pendingImageUri?.let { uri -> takePictureLauncher.launch(uri) }
        } else if (!granted && isCameraRequested) {
          isCameraRequested = false
          Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
      }

  // Function to create a URI for the image file where the camera app will save the photo
  fun createImageFileUri(): Uri? {
    return try {
      val imageFileName = "profile_picture_${System.currentTimeMillis()}.jpg"
      val storageDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
      val imageFile = File(storageDir, imageFileName)
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  if (isDialogOpen) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Choose Profile Picture") },
        text = { Text("Select an option to update your profile picture.") },
        buttons = {
          Column(
              modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingSmallMedium),
          ) {
            Button(
                onClick = {
                  onDismiss()
                  // Check camera permission
                  if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                      PackageManager.PERMISSION_GRANTED) {
                    // Launch camera with pendingImageUri
                    val uri = createImageFileUri()
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
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = androidx.compose.material.MaterialTheme.colors.primary)) {
                  Text(
                      "Take Photo",
                      color = androidx.compose.material.MaterialTheme.colors.onPrimary)
                }
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))
            Button(
                onClick = {
                  onDismiss()
                  pickImageLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = androidx.compose.material.MaterialTheme.colors.primary)) {
                  Text(
                      "Upload from Gallery",
                      color = androidx.compose.material.MaterialTheme.colors.onPrimary)
                }
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        backgroundColor = androidx.compose.material.MaterialTheme.colors.surface)) {
                  Text("Cancel", color = androidx.compose.material.MaterialTheme.colors.primary)
                }
          }
        })
  }
}
