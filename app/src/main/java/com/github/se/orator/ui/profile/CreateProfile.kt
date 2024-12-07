package com.github.se.orator.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.AppShapes
import com.github.se.orator.ui.theme.AppTypography

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

  // -----------------------------------------------------------
  // Added camera-related variables and launchers here

  // Temporary variable to hold the pending image URI before confirmation
  var pendingImageUri by remember { mutableStateOf<Uri?>(null) }

  val context = LocalContext.current

  // Launcher for picking an image from the gallery
  val pickImageLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
          profilePicUri = it // Update the profilePicUri to display the image
        }
      }

  // Launcher to take a picture using the camera
  val takePictureLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
          // Only set profilePicUri if capture was successful
          pendingImageUri?.let { uri ->
            profilePicUri = uri
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
            title = { Text("") },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Image(
                        painter = painterResource(id = R.drawable.chevron_left),
                        contentDescription = "Back",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeSmall)
                                .testTag("BackImage") // Replaced 32.dp
                        )
                  }
            },
            backgroundColor = AppColors.surfaceColor, // Replaced Color.White
            contentColor = AppColors.textColor, // Replaced Color.Black
            elevation = AppDimensions.appBarElevation // Replaced 4.dp
            )
      },
      content = {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(it)
                    .padding(AppDimensions.paddingMedium), // Replaced 16.dp
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = "Create your Orator profile", // Adjust the title text here
                  style = AppTypography.smallTitleStyle,
                  modifier =
                      Modifier.padding(bottom = AppDimensions.paddingLarge) // Add bottom padding
                  )

              // Profile Picture
              Box(
                  contentAlignment = Alignment.Center,
                  modifier =
                      Modifier.size(
                              AppDimensions
                                  .slightlyLargerProfilePictureSize) // Slightly larger to
                                                                     // accommodate the IconButton
                                                                     // outside the circle
                          .testTag("profile_picture_container")) {
                    // Circle with background image and profile picture
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier.size(AppDimensions.slightlyLowerProfilePictureSize)
                                .clip(CircleShape)) {
                          // Background Image
                          Image(
                              painter = painterResource(id = R.drawable.profile_background),
                              contentDescription = "Profile Picture Background",
                              modifier = Modifier.fillMaxSize(), // Fill the circle
                              contentScale = ContentScale.Crop)

                          // Profile Picture overlay
                          ProfilePicture(
                              profilePictureUrl = profilePicUri?.toString(),
                              onClick = { isDialogOpen = true },
                          )
                        }

                    // Camera icon overlay, positioned outside the circle at the bottom end
                    IconButton(
                        onClick = { isDialogOpen = true },
                        modifier =
                            Modifier.size(AppDimensions.paddingMediumSmall)
                                .align(Alignment.BottomEnd)
                                .testTag("upload_profile_picture")) {
                          Image(
                              painter = painterResource(id = R.drawable.camera),
                              contentDescription = "Upload profile picture",
                              modifier = Modifier.size(AppDimensions.paddingMediumSmall))
                        }
                  }

              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall)) // Replaced 16.dp

              Text(
                  modifier = Modifier.testTag("profile_picture_label"),
                  text = "Profile picture (optional)",
                  fontSize = AppFontSizes.bodySmall, // 14.sp
                  color = Color.Gray // Can be replaced with AppColors.secondaryTextColor
                  )

              Spacer(modifier = Modifier.height(AppDimensions.paddingLarge)) // Replaced 24.dp

              // Username Input Field
              TextField(
                  value = username,
                  onValueChange = { username = it },
                  label = { Text("Username") },
                  placeholder = { Text("Enter your username") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(AppDimensions.inputFieldHeight) // Replaced with a new variable
                          .testTag("username_input"),
                  singleLine = true,
                  colors =
                      TextFieldDefaults.textFieldColors(
                          textColor = AppColors.textColor, // Text color
                          placeholderColor = AppColors.placeholderColor, // Placeholder text color
                          backgroundColor =
                              AppColors.placeholderColor, // Background color of the TextField
                          focusedLabelColor = AppColors.textColor, // Label color when focused
                          unfocusedLabelColor = AppColors.textColor,
                          focusedIndicatorColor =
                              AppColors.textColor, // The color of the focus indicator
                          unfocusedIndicatorColor =
                              AppColors.textColor // The color of the unfocused indicator
                          ))

              Spacer(modifier = Modifier.height(AppDimensions.paddingExtraLarge)) // Replaced 48.dp

              // Save Button
              Button(
                  onClick = {
                    if (username.isNotBlank()) {
                      isUploading = true
                      val uid = userProfileViewModel.repository.getCurrentUserUid().toString()

                      val newProfile =
                          UserProfile(
                              uid = uid,
                              name = username,
                              age = 0,
                              profilePic = null,
                              statistics = UserStatistics(),
                              friends = emptyList())

                      userProfileViewModel.addUserProfile(newProfile)

                      if (profilePicUri != null) {
                        userProfileViewModel.uploadProfilePicture(uid, profilePicUri!!)
                      }

                      isUploading = false
                      navigationActions.navigateTo(TopLevelDestinations.HOME)
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag("save_profile_button")
                          .height(AppDimensions.buttonHeightLarge),
                  shape = AppShapes.circleShape,
                  enabled = username.isNotBlank() && !isUploading,
                  colors =
                      ButtonDefaults.buttonColors(
                          backgroundColor = AppColors.buttonBackgroundColor,
                          contentColor = AppColors.buttonTextColor)) {
                    Text(
                        modifier = Modifier.testTag("save_profile_button_text"),
                        text = "Save profile",
                        style = AppTypography.buttonTextStyle)
                  }

              Spacer(modifier = Modifier.weight(1f))

              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(bottom = AppDimensions.paddingMedium), // Optional bottom padding
                  horizontalArrangement = Arrangement.Start // Aligns content to the start (left)
                  ) {
                    Image(
                        painter =
                            painterResource(
                                id =
                                    R.drawable
                                        .create_profile_speaker), // Replace with your drawable
                                                                  // image name
                        contentDescription = "Decorative bottom-left image",
                        modifier =
                            Modifier.height(AppDimensions.imageLargeXXL) // Adjust height as needed
                                .padding(
                                    start = AppDimensions.paddingSmall), // Optional start padding
                        contentScale = ContentScale.Crop)
                  }
            }
      })

  // Dialog for choosing between camera and gallery
  if (isDialogOpen) {
    ChoosePictureDialog(
        onDismiss = { isDialogOpen = false },
        onTakePhoto = {
          isDialogOpen = false
          // Camera functionality below

          // Check camera permission
          if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
              PackageManager.PERMISSION_GRANTED) {
            // Launch camera with pendingImageUri
            val uri = createImageFileUri(context)
            if (uri != null) {
              pendingImageUri = uri
              takePictureLauncher.launch(uri)
            } else {
              Toast.makeText(context, "Failed to create image file.", Toast.LENGTH_SHORT).show()
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
