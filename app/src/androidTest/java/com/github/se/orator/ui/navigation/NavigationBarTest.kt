package com.github.se.orator.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq

class NavigationBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp(): Unit {
    navigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent {
      BottomNavigationMenu(
          { tld -> navigationActions.navigateTo(tld) },
          LIST_TOP_LEVEL_DESTINATION,
          TopLevelDestinations.HOME.route)
    }
  }

  @Test
  fun clickOnItemNavigatesToScreen(): Unit {
    // Click on the item
    composeTestRule.onNodeWithTag("Friends").performClick()
    // Verify that the navigation action was called
    verify(navigationActions).navigateTo(eq(TopLevelDestinations.FRIENDS))

    composeTestRule.onNodeWithTag("Profile").performClick()
    verify(navigationActions).navigateTo(eq(TopLevelDestinations.PROFILE))
  }
}
