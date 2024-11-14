package com.github.se.orator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationMenuTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomNavigationMenu_displaysTabs() {
    val tabs =
        listOf(
            TopLevelDestination(Route.HOME, Icons.Outlined.Home, Icons.Filled.Home, "Home"),
            TopLevelDestination(Route.FRIENDS, Icons.Outlined.Star, Icons.Filled.Star, "Friends"),
            TopLevelDestination(
                Route.PROFILE, Icons.Outlined.Person, Icons.Filled.Person, "Profile"))

    composeTestRule.setContent {
      BottomNavigationMenu(onTabSelect = {}, tabList = tabs, selectedItem = Route.HOME)
    }

    // Check if both tabs are displayed
    tabs.forEach { tab -> composeTestRule.onNodeWithTag(tab.textId).assertExists() }
  }

  @Test
  fun bottomNavigationMenu_selectsTabOnClick() {
    val tabs =
        listOf(
            TopLevelDestination(Route.HOME, Icons.Outlined.Home, Icons.Filled.Home, "Home"),
            TopLevelDestination(Route.FRIENDS, Icons.Outlined.Star, Icons.Filled.Star, "Friends"),
            TopLevelDestination(
                Route.PROFILE, Icons.Outlined.Person, Icons.Filled.Person, "Profile"))
    var selectedTab: TopLevelDestination? = null

    composeTestRule.setContent {
      BottomNavigationMenu(
          onTabSelect = { selectedTab = it }, tabList = tabs, selectedItem = Route.HOME)
    }

    // Click on the "Friends" tab and verify if the onTabSelect callback was triggered with the
    // correct item
    composeTestRule.onNodeWithTag("Friends").performClick()
    assert(selectedTab?.route == Route.FRIENDS)
  }

  @Test
  fun bottomNavigationMenu_highlightsSelectedTab() {
    val tabs =
        listOf(
            TopLevelDestination(Route.HOME, Icons.Outlined.Home, Icons.Filled.Home, "Home"),
            TopLevelDestination(Route.FRIENDS, Icons.Outlined.Star, Icons.Filled.Star, "Friends"),
            TopLevelDestination(
                Route.PROFILE, Icons.Outlined.Person, Icons.Filled.Person, "Profile"))

    composeTestRule.setContent {
      BottomNavigationMenu(onTabSelect = {}, tabList = tabs, selectedItem = Route.FRIENDS)
    }

    // Verify that the "Friends" tab is selected and highlighted
    composeTestRule.onNodeWithTag("Friends").assertIsSelected()
    composeTestRule.onNodeWithTag("Home").assertIsNotSelected()
  }
}
