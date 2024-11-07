package com.github.se.orator.ui.navigation

import androidx.compose.material.icons.Icons
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
            TopLevelDestination(Route.HOME, Icons.Outlined.Menu, Screen.HOME),
            TopLevelDestination(Route.FRIENDS, Icons.Outlined.Star, Screen.FRIENDS),
            TopLevelDestination(Route.PROFILE, Icons.Outlined.Person, Screen.PROFILE))
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
            TopLevelDestination(Route.HOME, Icons.Outlined.Menu, Screen.HOME),
            TopLevelDestination(Route.FRIENDS, Icons.Outlined.Star, Screen.FRIENDS),
            TopLevelDestination(Route.PROFILE, Icons.Outlined.Person, Screen.PROFILE))
    var selectedTab: TopLevelDestination? = null

    composeTestRule.setContent {
      BottomNavigationMenu(
          onTabSelect = { selectedTab = it }, tabList = tabs, selectedItem = Route.HOME)
    }

    // Click on the "Friends" tab and verify if the onTabSelect callback was triggered with the
    // correct item
    composeTestRule.onNodeWithTag(Screen.FRIENDS).performClick()
    assert(selectedTab?.route == Route.FRIENDS)
  }

  @Test
  fun bottomNavigationMenu_highlightsSelectedTab() {
    val tabs =
        listOf(
            TopLevelDestination(Route.HOME, Icons.Outlined.Menu, Screen.HOME),
            TopLevelDestination(Route.FRIENDS, Icons.Outlined.Star, Screen.FRIENDS),
            TopLevelDestination(Route.PROFILE, Icons.Outlined.Person, Screen.PROFILE))

    composeTestRule.setContent {
      BottomNavigationMenu(
          onTabSelect = {},
          tabList = tabs,
          selectedItem = Route.FRIENDS // "Friends" tab is selected
          )
    }

    // Verify that the "Friends" tab is selected and highlighted
    composeTestRule.onNodeWithTag(Screen.FRIENDS).assertIsSelected()
    composeTestRule.onNodeWithTag(Screen.HOME).assertIsNotSelected()
  }
}
