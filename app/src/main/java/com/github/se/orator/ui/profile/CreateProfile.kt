package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun CreateAccountScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  var username by remember { mutableStateOf("") }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Create an OratorAI account") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp))
              }
            },
            backgroundColor = Color.White,
            contentColor = Color.Black)
      },
      content = {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Spacer(modifier = Modifier.height(32.dp))

              // Profile Picture (Optional)
              Box(
                  modifier = Modifier.size(128.dp).clip(CircleShape),
                  contentAlignment = Alignment.Center) {
                    // Placeholder profile picture
                    Icon(
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = "Profile picture placeholder",
                        modifier = Modifier.size(128.dp),
                        tint = Color.Gray)
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
                  modifier = Modifier.fillMaxWidth(),
                  singleLine = true)

              Spacer(modifier = Modifier.height(48.dp))

              // Save Button
              Button(
                  onClick = {
                    if (username.isNotBlank()) {
                      // Create a new UserProfile for new users
                      val newProfile =
                          UserProfile(
                              uid = userProfileViewModel.repository.getCurrentUserUid().toString(),
                              name = username,
                              age = 0, // Set a default age
                              profilePic = null, // Set to null for now
                              statistics = UserStatistics(), // Default statistics
                              friends = emptyList())

                      // Add the new profile using the ViewModel
                      userProfileViewModel.addUserProfile(newProfile)

                      // Navigate to the next screen (e.g., profile screen)
                      navigationActions.navigateTo(TopLevelDestinations.PROFILE)
                    }
                  },
                  modifier = Modifier.fillMaxWidth(),
                  shape = CircleShape,
                  enabled = username.isNotBlank()) {
                    Text(text = "Save profile", fontWeight = FontWeight.Bold)
                  }
            }
      })
}
