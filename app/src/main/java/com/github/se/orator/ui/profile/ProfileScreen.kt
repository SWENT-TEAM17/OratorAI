package com.github.se.orator.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions


@Composable
fun ProfileScreen(
    navigationActions: NavigationActions,
    profileViewModel: UserProfileViewModel
) {
    // State to control whether the profile picture dialog is open
    var isDialogOpen by remember { mutableStateOf(false) }

    // Collect the profile data from the ViewModel
    val userProfile by profileViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 4.dp,
                title = { Text(text = "Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /*TODO: Navigate to settings screen */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        //TODO: Sign out the user
                    }) {
                        Image(
                            painter = painterResource(id = R.drawable.sign_out),
                            contentDescription = "Sign out",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                })
        },
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute()
            )
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userProfile?.let { profile ->
                // Profile Picture with clickable action to open it in larger format
                ProfilePicture(
                    profilePictureUrl = profile.profilePic,
                    onClick = { isDialogOpen = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username
                Text(text = profile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                Button(onClick = { /*TODO: Navigate to edit profile screen */ }) {
                    Text(text = "Edit Profile")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Achievements Section
                CardSection(
                    title = "Achievements",
                    iconId = R.drawable.trophy_frame,
                    onClick = { /*TODO: Handle achievements click */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Old Training Sessions Section
                CardSection(
                    title = "Previous Sessions",
                    iconId = R.drawable.history_frame,
                    onClick = { /*TODO: Handle previous sessions click */ }
                )
            } ?: run {
                // Show a loading state if the profile is not yet available
                Text("Loading profile...")
            }
        }

        // Dialog to show the profile picture in larger format
        if (isDialogOpen && userProfile?.profilePic != null) {
            ProfilePictureDialog(
                profilePictureUrl = userProfile!!.profilePic!!,
                onDismiss = { isDialogOpen = false }
            )
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
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .clickable { onClick() } // Trigger the click action to open the image
            .testTag("ProfilePicture") // Test tag for profile picture
    )
}

@Composable
fun ProfilePictureDialog(profilePictureUrl: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clickable {
                    onDismiss()
                } // Dismiss the dialog when the user clicks outside
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = profilePictureUrl),
                contentDescription = "Large Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun CardSection(title: String, iconId: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        elevation = 4.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = title,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("titleIcon") // Test tag for icons in the card sections
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
