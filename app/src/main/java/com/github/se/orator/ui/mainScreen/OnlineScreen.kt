package com.github.se.orator.ui.mainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions

/**
 * The main screen's composable responsible to display the welcome text, the practice mode cards and
 * the toolbar containing buttons for different sections
 */
@Composable
fun OnlineScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          MainTitle("onlineScreenText1", "Battle online", "onlineScreenText2", "with friends!")

          ButtonRowOnline(navigationActions)

          LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium),
              contentPadding = PaddingValues(AppDimensions.paddingMedium)) {
                // Online mode (BOTI) cards
                item {
                  ModeCard(
                      "Battle Of The Interviews",
                      "Click on a friend's profile to initiate",
                      true,
                      painterResource(R.drawable.speaking_interview),
                      true) {
                        navigationActions.navigateTo(Screen.FRIENDS)
                      }
                }
              }
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.HOME)
      })
}

/**
 * The implementation of the toolbar containing the different selection buttons of the main screen
 */
@Composable
fun ButtonRowOnline(navigationActions: NavigationActions) {
  Row(
      modifier =
          Modifier.testTag("toolbar").fillMaxWidth().padding(top = AppDimensions.paddingMedium),
      horizontalArrangement =
          Arrangement.spacedBy(AppDimensions.spacingXLarge, Alignment.CenterHorizontally),
  ) {
    SectionButton("Popular", { navigationActions.navigateTo(Screen.HOME) }, false)

    SectionButton("Online", {}, true)
  }
}
