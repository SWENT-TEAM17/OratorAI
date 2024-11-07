package com.github.se.orator.ui.profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
import com.github.se.orator.ui.theme.AppTypography

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
            backgroundColor = AppColors.surfaceColor, // Replaced Color.White
            contentColor = AppColors.textColor, // Replaced Color.Black
            elevation = AppDimensions.appBarElevation, // Replaced 4.dp
            title = {
              Text(
                  modifier = Modifier.testTag("profile_title"),
                  text = "Profile",
                  fontWeight = FontWeight.Bold,
                  style = AppTypography.appBarTitleStyle)
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settings_button")) {
                    Image(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Settings",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium)
                                .testTag("SettingsImage") // Replaced 32.dp with iconSizeMedium
                        )
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    // TODO: Implement sign-out functionality
                  },
                  modifier = Modifier.testTag("sign_out_button")) {
                    Image(
                        painter = painterResource(id = R.drawable.sign_out),
                        contentDescription = "Sign out",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium)
                                .testTag("SignOutImage") // Replaced 32.dp with iconSizeMedium
                        )
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      }) {
        Log.d("aa", "the current route is ${navigationActions.currentRoute()}")
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(it)
                    .padding(AppDimensions.paddingMedium), // Replaced 16.dp with paddingMedium
            horizontalAlignment = Alignment.CenterHorizontally) {
              userProfile?.let { profile ->
                // Profile Picture with clickable action to open it in larger format
                ProfilePicture(
                    profilePictureUrl = profile.profilePic, onClick = { isDialogOpen = true })

                Spacer(
                    modifier =
                        Modifier.height(
                            AppDimensions.paddingSmall)) // Replaced 8.dp with paddingSmall

                // Username
                Text(
                    text = profile.name,
                    fontSize = 20.sp, // Kept as is since no corresponding theme variable
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("profile_name"))

                Spacer(
                    modifier =
                        Modifier.height(
                            AppDimensions.paddingMedium)) // Replaced 16.dp with paddingMedium

                // Edit Profile Button
                Button(
                    onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                    modifier = Modifier.testTag("edit_button").fillMaxWidth(),
                    shape = AppShapes.circleShape // Replaced CircleShape with theme shape
                    ) {
                      Text(text = "Edit Profile", fontWeight = FontWeight.Bold)
                    }

                Spacer(
                    modifier =
                        Modifier.height(
                            AppDimensions.paddingLarge)) // Replaced 24.dp with paddingLarge

                // Achievements Section
                CardSection(
                    title = "Achievements",
                    iconId = R.drawable.trophy_frame,
                    onClick = { /*TODO: Handle achievements click */},
                    modifier = Modifier.testTag("achievements_section"))

                Spacer(
                    modifier =
                        Modifier.height(
                            AppDimensions.paddingSmall)) // Replaced 16.dp with paddingSmall

                // Previous Sessions Section
                CardSection(
                    title = "Previous Sessions",
                    iconId = R.drawable.history_frame,
                    onClick = { /*TODO: Handle previous sessions click */},
                    modifier = Modifier.testTag("previous_sessions_section"))
              }
                  ?: run {
                    // Show a loading state if the profile is not yet available
                    Text(
                        text = "Loading profile...",
                        style = AppTypography.bodyLargeStyle, // Replaced manual styling
                        modifier = Modifier.testTag("loading_profile_text"))
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
          Modifier.size(AppDimensions.profilePictureSize) // Replaced 100.dp with profilePictureSize
              .clip(CircleShape)
              .clickable(onClick = onClick)
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
                .padding(AppDimensions.paddingMedium) // Replaced 16.dp with paddingMedium
                .clickable { onDismiss() }
                .testTag("OnDismiss"), // Dismiss the dialog when clicked outside
        contentAlignment = Alignment.Center) {
          Image(
              painter = rememberAsyncImagePainter(model = profilePictureUrl),
              contentDescription = "Large Profile Picture",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(AppDimensions.profilePictureDialogSize) // Replaced 200.dp with
                      // profilePictureDialogSize
                      .clip(CircleShape)
                      .testTag("profile_picture_dialog"))
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
      modifier =
          modifier
              .fillMaxWidth()
              .height(AppDimensions.cardSectionHeight) // Replaced 100.dp with cardSectionHeight
              .clickable { onClick() }
              .testTag("cardSection"),
      elevation = AppDimensions.elevationSmall // Replaced 4.dp with elevationSmall
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.padding(
                    AppDimensions.paddingSmallMedium) // Replaced 16.dp with paddingSmallMedium
            ) {
              Image(
                  painter = painterResource(id = iconId),
                  contentDescription = title,
                  modifier =
                      Modifier.size(
                              AppDimensions.iconSizeMedium) // Replaced 24.dp with iconSizeMedium
                          .testTag("titleIcon"))
              Spacer(
                  modifier =
                      Modifier.width(
                          AppDimensions
                              .paddingSmallMedium)) // Replaced 16.dp with paddingSmallMedium
              Text(
                  text = title,
                  fontSize = 18.sp, // Kept as is since no corresponding theme variable
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("titleText"))
            }
      }
}
