package com.github.se.orator.ui.mainScreen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class OnlineScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun onlineScreen_displaysTitleCorrectly() {
    composeTestRule.setContent { OnlineScreen(navigationActions = navigationActions) }

    composeTestRule.onNodeWithTag("onlineScreenText1").assertTextEquals("Battle online")
    composeTestRule.onNodeWithTag("onlineScreenText2").assertTextEquals("with friends!")
  }

  @Test
  fun onlineScreen_toolbarButtons_areDisplayedAndClickable() {
    composeTestRule.setContent { OnlineScreen(navigationActions = navigationActions) }

    composeTestRule
        .onNodeWithTag("toolbar")
        .onChildAt(0)
        .assertTextEquals("Popular")
        .assertHasClickAction()

    composeTestRule
        .onNodeWithTag("toolbar")
        .onChildAt(1)
        .assertTextEquals("Online")
        .assertHasClickAction()
  }

  @Test
  fun onlineScreen_modeCard_navigatesToFriends() {

    composeTestRule.setContent { OnlineScreen(navigationActions = navigationActions) }

    composeTestRule.onNodeWithText("Battle Of The Interviews").performClick()
  }
}
