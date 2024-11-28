package com.github.se.orator.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.AudioRecorder
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
import com.github.se.orator.ui.theme.AppTypography
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {
  val colors = MaterialTheme.colorScheme

  var isStatsVisible by remember { mutableStateOf(false) }

  // Get the context
  val context = LocalContext.current
  // Load saved recordings when the screen is displayed
  LaunchedEffect(Unit) { profileViewModel.loadSavedRecordings(context) }

  // State to control whether the profile picture dialog is open
  var isDialogOpen by remember { mutableStateOf(false) }
  val audioRecorder = remember { AudioRecorder(context, isOffline = true) }

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()
  // State to hold the saved recordings
  val savedRecordings by profileViewModel.savedRecordings.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.inverseSurface,
            elevation = AppDimensions.appBarElevation,
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
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("settings_icon"))
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
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("sign_out_icon"))
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(innerPadding).padding(AppDimensions.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally) {
              userProfile?.let { profile ->
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
                                      4.dp, shape = RoundedCornerShape(size = 10.dp), clip = false)
                                  .background(
                                      MaterialTheme.colorScheme.onSecondary,
                                      shape = RoundedCornerShape(size = 10.dp)),
                          elevation = AppDimensions.elevationSmall) {}

                      // Profile Picture with overlapping positioning
                      ProfilePicture(
                          profilePictureUrl = profile.profilePic,
                          onClick = { isDialogOpen = true },
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
                                  .offset(y = (-20).dp),
                          // .offset(x = (AppDimensions.profilePictureSize / 2.2f)),
                          shape = AppShapes.circleShape,
                          colors =
                              ButtonDefaults.buttonColors(backgroundColor = AppColors.surfaceColor),
                          contentPadding = PaddingValues(0.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit button",
                                modifier = Modifier.size(AppDimensions.iconSizeMedium),
                                tint = colors.errorContainer)
                          }

                      Column(
                          horizontalAlignment = Alignment.CenterHorizontally,
                          modifier = Modifier.align(Alignment.TopCenter)) {
                            Spacer(modifier = Modifier.height(AppDimensions.MediumSpacerHeight))

                            // Box to hold username and streak
                            Box(
                                modifier = Modifier.fillMaxWidth().testTag("profile_name_box"),
                                contentAlignment = Alignment.Center) {
                                  // Username remains centered
                                  Text(
                                      text = profile.name,
                                      fontSize = 20.sp,
                                      fontWeight = FontWeight.Bold,
                                      modifier = Modifier.testTag("profile_name"))

                                  // Current Streak aligned to the end with fire icon
                                  // TODO : remove this streak
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
                                            tint = AppColors.amber,
                                            modifier = Modifier.size(AppDimensions.iconSizeSmall))
                                        Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
                                        Text(
                                            text = "${profile.currentStreak}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.amber,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("current_streak_text"))
                                      }
                                }

                            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacerHeight))

                            Text(
                                text =
                                    if (profile.bio.isNullOrBlank()) "Write your bio here"
                                    else profile.bio,
                                modifier =
                                    Modifier.padding(horizontal = AppDimensions.paddingMedium))
                          }
                    }

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
                Log.d("scn", "bio is: ${profile.bio}")

                // Stats section
                StatsSection(
                    streak = profile.currentStreak,
                    totalSpeakingTime = "10",
                    onStatsClick = { isStatsVisible = !isStatsVisible })
                // Spacer
                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))

                // AnimatedVisibility for Stats Screen
                // TODO: we can even adapt it so it pops out a screen with a graph covering 75% of
                // the area for example
                AnimatedVisibility(
                    visible = isStatsVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.fillMaxSize() // You can adjust the modifier as needed
                    ) {
                      // This will be the content of your stats screen or graph
                      Box(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .fillMaxHeight()
                                  .background(Color.White) // Adjust this to match your design
                                  .padding(16.dp)) {
                            // Example content for the stats screen (you can replace this with your
                            // graph)
                            Text("Graph or Stats content goes here")
                          }
                    }
              }
              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))

              Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {

                // Title for Offline Recordings
                Text(
                    text = "My Offline Recordings",
                    style =
                        TextStyle(
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_black)),
                            color = colors.onSurface),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 8.dp))

                // Display saved recordings
                if (savedRecordings.isNotEmpty()) {
                  savedRecordings.forEach { audioFile ->
                    AudioRecordingPlaceholder(
                        fileName = audioFile.name,
                        onPlayClicked = { audioRecorder.playAudio(audioFile) })
                  }
                } else {
                  Text(
                      text = "No offline recordings available",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                      modifier = Modifier.padding(8.dp))
                }
              }
            }
            ?: run {
              Text(
                  text = "Loading profile...",
                  style = AppTypography.bodyLargeStyle,
                  modifier = Modifier.testTag("loading_profile_text"))
            }
      }

  // Dialog to show the profile picture in larger format
  if (isDialogOpen && userProfile?.profilePic != null) {
    ProfilePictureDialog(
        profilePictureUrl = userProfile!!.profilePic!!, onDismiss = { isDialogOpen = false })
  }
}

@Composable
fun StatsSection(
    streak: Long, // The streak value from your data model
    totalSpeakingTime: String, // Hardcoded for now
    onStatsClick: () -> Unit
) {
  val colors = MaterialTheme.colorScheme
  // This column will hold two sections: Streak and Total Speaking Time
  Column(modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingMedium)) {
    // Title for the stats section (matching the "My Offline Recordings" style)
    Text(
        text = "My Stats",
        style =
            TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.poppins_black)),
                fontWeight = FontWeight.Bold,
                color = colors.onBackground),
        modifier = Modifier.padding(AppDimensions.paddingMedium))

    // Section 1: Streak
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onStatsClick)
                .padding(AppDimensions.paddingSmall)) {
          Icon(
              imageVector = Icons.Filled.Whatshot,
              contentDescription = "Streak",
              tint = AppColors.amber,
              modifier = Modifier.size(AppDimensions.iconSizeMedium))
          Spacer(modifier = Modifier.width(AppDimensions.paddingSmall))
          Text(
              text = "Current Streak: $streak",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = AppColors.amber,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }

    // Section 2: Total Speaking Time
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onStatsClick)
                .padding(AppDimensions.paddingSmall)) {
          Icon(
              imageVector = Icons.Filled.PlayArrow, // Use an appropriate icon for time
              contentDescription = "Total Speaking Time",
              modifier = Modifier.size(AppDimensions.iconSizeMedium))
          Spacer(modifier = Modifier.width(AppDimensions.paddingSmall))
          Text(
              text = "Total Speaking Time: $totalSpeakingTime",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
        }
  }
}

@Composable
fun AudioRecordingPlaceholder(fileName: String, onPlayClicked: () -> Unit) {

  // TODO : Display the Title of the saved audio in the placeholder
  val colors = MaterialTheme.colorScheme

  Row(
      modifier =
          Modifier.width(351.dp)
              .height(96.dp)
              .shadow(4.dp, shape = RoundedCornerShape(size = 10.dp), clip = false)
              .background(colors.onSecondary, shape = RoundedCornerShape(size = 10.dp))
              .clickable { onPlayClicked() }
              .padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Play Button Icon
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play",
            modifier =
                Modifier.size(36.dp)
                    .align(Alignment.CenterVertically)
                    .background(colors.errorContainer, shape = CircleShape))
        // Title of the Recording
        Text(
            text = fileName, // /////// recording.title !
            style =
                TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.inter)),
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onBackground),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.align(Alignment.CenterVertically)
                    .weight(1f) // Ensures text takes available space
            )
      }
}

@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)

  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          modifier
              .size(AppDimensions.profilePictureSize)
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
 * Composable function to display a card section with an icon and title.
 *
 * @param title Title of the card section.
 * @param imageVector Icon of the card.
 * @param onClick Callback to handle click events.
 * @param modifier Modifier to be applied to the card.
 */
@Composable
fun CardSection(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .height(AppDimensions.cardSectionHeight)
              .clickable { onClick() }
              .testTag("cardSection"),
      elevation = AppDimensions.elevationSmall) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(AppDimensions.paddingSmallMedium)) {
              Icon(
                  imageVector,
                  contentDescription = "Card icon",
                  modifier = Modifier.size(AppDimensions.iconSizeMedium))
              Spacer(modifier = Modifier.width(AppDimensions.paddingSmallMedium))
              Text(
                  text = title,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("titleText"))
            }
      }
}
