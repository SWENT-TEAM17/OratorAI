package com.github.orator.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.mainScreen.MainScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions

    @Before
    fun setUp() {
        // "mock" navigation for testing
        navigationActions = mock(NavigationActions::class.java)
        `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
    }

    @Test
    fun testWelcomeTextIsDisplayed() {
        composeTestRule.setContent {
            MainScreen(navigationActions)
        }

        // Check if the welcome text is displayed correctly
        composeTestRule
            .onNodeWithTag("mainScreenText1")
            .assertExists()
            .assertTextContains("Find your")

        composeTestRule
            .onNodeWithTag("mainScreenText2")
            .assertExists()
            .assertTextContains("practice mode")
    }

    @Test
    fun testToolbarIsDisplayedAndClickable() {
        composeTestRule.setContent {
            MainScreen(navigationActions)
        }

        // Check if the toolbar is displayed
        composeTestRule
            .onNodeWithTag("toolbar")
            .assertExists()
            .assertIsDisplayed()

        // Check if the buttons are clickable
        composeTestRule
            .onNodeWithTag("toolbarPopularButton")
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag("toolbarFunButton")
            .assertHasClickAction()
        composeTestRule
            .onNodeWithTag("toolbarConnectButton")
            .assertHasClickAction()
    }

    @Test
    fun testStackedCardsAreDisplayed() {
        composeTestRule.setContent {
            MainScreen(navigationActions)
        }

        // Check if the animated cards exists
        composeTestRule
            .onNodeWithTag("animatedCards")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun testBottomNavigationMenuIsDisplayed() {
        composeTestRule.setContent {
            MainScreen(navigationActions)
        }

        // Check if the bottom navigation menu is displayed
        composeTestRule
            .onNodeWithTag("bottomNavigationMenu")
            .assertExists()
            .assertIsDisplayed()
    }
}