package com.github.se.orator.ui.screens

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewFunScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) {
        Text("Fun Mode Screen")
      }
}
