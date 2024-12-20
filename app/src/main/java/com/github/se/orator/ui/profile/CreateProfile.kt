package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
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
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.theme.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateAccountScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  var username by remember { mutableStateOf("") }
  var isDialogOpen by remember { mutableStateOf(false) }
  var profilePicUri by remember { mutableStateOf<Uri?>(null) }
  var isUploading by remember { mutableStateOf(false) }

  val context = LocalContext.current

  // Instead of using a Scaffold topBar, place a back button in a row at the top.
  Column(
      modifier =
          Modifier.fillMaxSize()
              .padding(
                  top = AppDimensions.paddingLarge,
                  start = AppDimensions.paddingMedium,
                  end = AppDimensions.paddingMedium),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Row containing the back button and spacing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Image(
                        painter = painterResource(id = R.drawable.chevron_left),
                        contentDescription = "Back",
                        modifier = Modifier.size(AppDimensions.iconSizeSmall).testTag("BackImage"))
                  }
            }

        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

        // Title
        Text(
            text = "Create your Orator profile",
            style = AppTypography.titleLargeStyle,
            modifier = Modifier.testTag("create_profile_title"))

        Spacer(modifier = Modifier.height(AppDimensions.paddingExtraLarge))

        // Profile Picture Section
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier.size(AppDimensions.slightlyLargerProfilePictureSize)
                    .testTag("profile_picture_container")) {
              Box(
                  contentAlignment = Alignment.Center,
                  modifier =
                      Modifier.size(AppDimensions.slightlyLowerProfilePictureSize)
                          .clip(CircleShape)) {
                    // Background Image
                    Image(
                        painter = painterResource(id = R.drawable.profile_background),
                        contentDescription = "Profile Picture Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop)

                    // Profile Picture overlay
                    ProfilePicture(profilePictureUrl = profilePicUri?.toString())
                  }

              // Camera icon overlay
              androidx.compose.material.IconButton(
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

        Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))
        Text(
            modifier = Modifier.testTag("profile_picture_label"),
            text = "Profile picture (optional)",
            fontSize = AppFontSizes.bodySmall,
            color = Color.Gray)

        Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

        // Username Input Field
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("Enter your username") },
            modifier =
                Modifier.fillMaxWidth()
                    .height(AppDimensions.inputFieldHeight)
                    .testTag("username_input"),
            singleLine = true,
            colors =
                TextFieldDefaults.textFieldColors(
                    textColor = AppColors.textColor,
                    placeholderColor = AppColors.placeholderColor,
                    backgroundColor = AppColors.placeholderColor,
                    focusedLabelColor = AppColors.textColor,
                    unfocusedLabelColor = AppColors.textColor,
                    focusedIndicatorColor = AppColors.textColor,
                    unfocusedIndicatorColor = AppColors.textColor))

        Spacer(modifier = Modifier.height(AppDimensions.paddingExtraLarge))

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
            modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
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
            modifier = Modifier.fillMaxWidth().padding(bottom = AppDimensions.paddingMedium),
            horizontalArrangement = Arrangement.Start) {
              Image(
                  painter = painterResource(id = R.drawable.create_profile_speaker),
                  contentDescription = "Decorative bottom-left image",
                  modifier =
                      Modifier.height(AppDimensions.imageLargeXXL)
                          .padding(start = AppDimensions.paddingSmall),
                  contentScale = ContentScale.Crop)
            }
      }

  // Integrate the ImagePicker
  ImagePicker(
      isDialogOpen = isDialogOpen,
      onDismiss = { isDialogOpen = false },
      onImageSelected = { uri ->
        profilePicUri = uri
        Toast.makeText(context, "Profile picture updated.", Toast.LENGTH_SHORT).show()
      })
}
