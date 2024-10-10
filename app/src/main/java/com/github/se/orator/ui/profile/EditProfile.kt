package com.github.se.orator.ui.profile

import android.net.Uri
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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions

@Composable
fun EditProfileScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
    // Fetch the user's profile data
    val userProfile by userProfileViewModel.userProfile.collectAsState()

    // States for username, bio, and dialog visibility
    var isDialogOpen by remember { mutableStateOf(false) }
    var updatedUsername by remember { mutableStateOf(userProfile?.name ?: "") }
    var updatedBio by remember { mutableStateOf(userProfile?.bio ?: "") }

    // Intent launcher to capture photo or pick image from gallery
    val context = LocalContext.current
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Handle the profile picture update here
        bitmap?.let {
            // Assuming you have a function in the ViewModel to upload the profile picture
            userProfileViewModel.uploadProfilePicture(userProfile?.uid ?: "", Uri.EMPTY)  // Replace with actual URI logic
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { userProfileViewModel.uploadProfilePicture(userProfile?.uid ?: "", it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                backgroundColor = Color.White,
                contentColor = Color.Black,
                elevation = 4.dp,
                title = { Text(text = "Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Image(
                            painter = painterResource(id = R.drawable.back_arrow),
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle settings click */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = "Settings",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture with Camera Icon Overlay
            Box(contentAlignment = Alignment.Center) {
                ProfilePicture(
                    profilePictureUrl = userProfile?.profilePic,  // Fetch profile picture URL from userProfile
                    onClick = { isDialogOpen = true }  // Open dialog to choose camera/gallery
                )
                IconButton(
                    onClick = { isDialogOpen = true },
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Change Profile Picture",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username Input Field
            OutlinedTextField(
                value = updatedUsername,
                onValueChange = { newUsername -> updatedUsername = newUsername },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bio Input Field
            Text(
                text = "BIO",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            OutlinedTextField(
                value = updatedBio,
                onValueChange = { newBio -> updatedBio = newBio },
                placeholder = { Text(text = "Tell us about yourself") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Changes Button
            Button(
                onClick = {
                    // Save the updated profile information
                    val updatedProfile = userProfile?.copy(name = updatedUsername, bio = updatedBio)
                    if (updatedProfile != null) {
                        userProfileViewModel.createOrUpdateUserProfile(updatedProfile)
                    }
                    navigationActions.goBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Save changes")
            }
        }
    }

    // Display the dialog to choose between camera and gallery
    if (isDialogOpen) {
        ChoosePictureDialog(
            onDismiss = { isDialogOpen = false },
            onTakePhoto = {
                isDialogOpen = false
                takePictureLauncher.launch(null)
            },
            onPickFromGallery = {
                isDialogOpen = false
                pickImageLauncher.launch("image/*")
            }
        )
    }
}

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
        confirmButton = {
            Column {
                Button(onClick = { onTakePhoto() }) {
                    Text("Take Photo")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onPickFromGallery() }) {
                    Text("Upload from Gallery")
                }
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BottomNavigationBar() {
    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = true,
            onClick = { /* Handle home click */ }
        )
        BottomNavigationItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = false,
            onClick = { /* Handle profile click */ }
        )
        BottomNavigationItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.friends),
                    contentDescription = "Friends",
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = false,
            onClick = { /* Handle friends click */ }
        )
    }
}
