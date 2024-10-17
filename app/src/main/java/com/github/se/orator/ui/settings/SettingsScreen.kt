package com.github.se.orator.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    modifier = Modifier.size(32.dp),
                    contentDescription = "Back")
              }
            })
      }) {
        Text("Settings Screen")
      }
}
