package com.github.se.orator.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.navigation.TopNavigationMenu
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.AppShapes
import com.github.se.orator.ui.theme.COLOR_AMBER
import com.google.firebase.auth.FirebaseAuth

/**
 * Displays the Profile screen, including user information, stats, and offline recordings.
 *
 * @param navigationActions Provides navigation functions for the app.
 * @param profileViewModel The ViewModel providing user profile data and actions.
 */
@Composable
fun ProfileScreen(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {
  val colors = MaterialTheme.colorScheme

  // Get the context
  val context = LocalContext.current

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()

  Scaffold(
      topBar = {
        TopNavigationMenu(
            textTestTag = "profile_title",
            title = "Profile",
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settings_button")) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("settings_icon"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    // Sign out the user
                    FirebaseAuth.getInstance().signOut()
                    // Display a toast message
                    Toast.makeText(context, "Logout successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the sign in screen
                    navigationActions.navigateTo(Screen.AUTH)
                  },
                  modifier = Modifier.testTag("sign_out_button")) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign out",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("sign_out_icon"),
                        tint = MaterialTheme.colorScheme.onSurface)
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      },
      backgroundColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppDimensions.paddingMedium)
                    .verticalScroll(rememberScrollState()), // Enable scrolling
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium),
        ) {
          userProfile?.let { profile ->
            /**
             * Displays user profile information, including profile picture, name, streak, and bio.
             */
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(AppDimensions.profileBoxHeight)
                        .padding(top = AppDimensions.paddingXXXLarge),
                contentAlignment = Alignment.TopCenter) {
                  // Background "card" behind the profile picture
                  Card(
                      modifier =
                          Modifier.fillMaxWidth(0.95f)
                              .height(AppDimensions.profileCardHeight)
                              .shadow(
                                  elevation = AppDimensions.elevationSmall,
                                  shape = RoundedCornerShape(size = AppDimensions.statusBarPadding),
                                  clip = false),
                      backgroundColor = colors.surfaceVariant,
                      shape = RoundedCornerShape(size = AppDimensions.statusBarPadding),
                      elevation = AppDimensions.elevationSmall) {}

                  // Profile Picture with overlapping positioning
                  ProfilePicture(
                      profilePictureUrl = profile.profilePic,
                      modifier =
                          Modifier.align(Alignment.TopCenter)
                              .offset(y = (-AppDimensions.profilePictureSize / 2)))

                  // Edit button
                  Button(
                      onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                      modifier =
                          Modifier.testTag("edit_button")
                              .size(AppDimensions.spacingXLarge)
                              .align(Alignment.TopEnd)
                              .offset(y = -AppDimensions.paddingMediumSmall),
                      // .offset(x = (AppDimensions.profilePictureSize / 2.2f)),
                      shape = AppShapes.circleShape,
                      colors =
                          ButtonDefaults.buttonColors(
                              backgroundColor = MaterialTheme.colorScheme.inverseOnSurface),
                      contentPadding = PaddingValues(AppDimensions.nullPadding)) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit button",
                            modifier = Modifier.size(AppDimensions.iconSizeMedium),
                            tint = MaterialTheme.colorScheme.primary)
                      }

                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier =
                          Modifier.align(Alignment.TopCenter)
                              .padding(top = AppDimensions.paddingSmall)) {
                        Spacer(modifier = Modifier.height(AppDimensions.mediumSpacerHeight))

                        // Box to hold username and streak
                        Box(
                            modifier = Modifier.fillMaxWidth().testTag("profile_name_box"),
                            contentAlignment = Alignment.Center) {
                              // Username remains centered
                              Text(
                                  text = profile.name,
                                  fontSize = AppFontSizes.titleMedium,
                                  fontWeight = FontWeight.Bold,
                                  modifier = Modifier.testTag("profile_name"),
                                  color = MaterialTheme.colorScheme.primary)

                              // Current Streak aligned to the end with fire icon
                              Row(
                                  verticalAlignment = Alignment.CenterVertically,
                                  modifier =
                                      Modifier.align(Alignment.CenterEnd)
                                          .offset(
                                              x =
                                                  -AppDimensions
                                                      .paddingLarge) // Push a little to the
                                          // left
                                          .testTag("current_streak")) {
                                    Icon(
                                        imageVector = Icons.Filled.Whatshot, // Fire icon
                                        contentDescription = "Active Streak",
                                        tint = COLOR_AMBER,
                                        modifier = Modifier.size(AppDimensions.iconSizeSmall))
                                    Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
                                    Text(
                                        text = "${profile.currentStreak}",
                                        fontSize = AppFontSizes.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = COLOR_AMBER,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("current_streak_text"))
                                  }
                            }

                        Spacer(modifier = Modifier.height(AppDimensions.smallSpacerHeight))

                        Text(
                            text =
                                if (profile.bio.isNullOrBlank()) "Write your bio here"
                                else profile.bio,
                            modifier = Modifier.padding(horizontal = AppDimensions.paddingMedium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1, // Limit to one line
                            overflow = TextOverflow.Ellipsis // Truncate with ellipsis
                            )
                      }
                }

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
            Log.d("scn", "bio is: ${profile.bio}")

            ProfileSection(
                title = "My Stats",
                modifier = Modifier.testTag("statistics_section"),
                action = {
                  Button(
                      onClick = { navigationActions.navigateTo(Screen.STAT) },
                      modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingSmall),
                      colors =
                          ButtonDefaults.buttonColors(
                              backgroundColor = MaterialTheme.colorScheme.primary,
                              contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                        Text("View More Stats")
                      }
                }) {
                  // Streak Content
                  Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingSmall)) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = "Streak",
                            tint = COLOR_AMBER,
                            modifier = Modifier.size(AppDimensions.iconSizeMedium))
                        Spacer(modifier = Modifier.width(AppDimensions.paddingSmall))
                        Text(
                            text = "Current Streak: ${profile.currentStreak}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = COLOR_AMBER,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                      }
                }
          }

          Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))

          ProfileSection(
              title = "My Offline Recordings",
              modifier = Modifier.testTag("offline_recordings_section"),
              action = {
                Button(
                    onClick = { navigationActions.navigateTo(Screen.OFFLINE_RECORDING_PROFILE) },
                    modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingSmall),
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface)) {
                      Text("View Your Offline Recordings")
                    }
              }) {}
        }
      }
}

/** Composable function to display the profile picture. */
@Composable
fun ProfilePicture(profilePictureUrl: String?, modifier: Modifier = Modifier) {
  val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)

  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          modifier
              .size(AppDimensions.profilePictureSize)
              .clip(CircleShape)
              .testTag("profile_picture"))
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
            Modifier.fillMaxSize()
                .padding(AppDimensions.paddingMedium)
                .clickable { onDismiss() }
                .testTag("OnDismiss"), // Dismiss the dialog when clicked outside
        contentAlignment = Alignment.Center) {
          Image(
              painter = rememberAsyncImagePainter(model = profilePictureUrl),
              contentDescription = "Large Profile Picture",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(AppDimensions.profilePictureDialogSize)
                      .clip(CircleShape)
                      .testTag("profile_picture_dialog"))
        }
  }
}

/**
 * Composable function to display a reusable profile section with a title, content, and optional
 * action button.
 *
 * @param title The title of the section to be displayed.
 * @param modifier Modifier to apply to the section layout.
 * @param action Optional composable lambda for an action button or additional UI at the bottom of
 *   the section.
 * @param content Composable lambda to define the main content of the section.
 */
@Composable
fun ProfileSection(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
  Column(modifier = modifier.fillMaxWidth().padding(AppDimensions.paddingMedium)) {
    // Section Title
    Text(
        text = title,
        style =
            TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.poppins_black)),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(vertical = AppDimensions.paddingSmall))

    // Section Content
    content()

    // Optional Action Button
    action?.let {
      Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))
      it()
    }
  }
}
