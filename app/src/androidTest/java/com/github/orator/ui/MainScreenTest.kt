package com.github.orator.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.orator.ui.theme.mainScreen.MainScreen
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWelcomeTextIsDisplayed() {
        composeTestRule.setContent {
            MainScreen()
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
            MainScreen()
        }

        // Check if the toolbar is displayed
        composeTestRule
            .onNodeWithTag("toolbar")
            .assertExists()
            .assertIsDisplayed()

        // Check if the buttons are clickable
        composeTestRule
            .onNodeWithTag("button")
            .assertHasClickAction()
    }

    @Test
    fun testStackedCardsAreDisplayed() {
        composeTestRule.setContent {
            MainScreen()
        }

        // Check if the animated cards exists
        composeTestRule
            .onNodeWithTag("animatedCards")
            .assertExists()
            .assertIsDisplayed()
    }
}