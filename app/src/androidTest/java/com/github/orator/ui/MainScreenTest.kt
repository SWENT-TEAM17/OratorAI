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
            .onNodeWithTag("mainScreenText")
            .assertExists()
            //Hard coded the "name" for now
            .assertTextContains("Hi name, what do you want to practice today ?")
    }

    @Test
    fun testProgressButtonIsDisplayedAndClickable() {
        composeTestRule.setContent {
            MainScreen()
        }

        // Check if the progress button is displayed
        composeTestRule
            .onNodeWithTag("mainScreenButton")
            .assertExists()
            .assertIsDisplayed()

        // Check if it's clickable
        composeTestRule
            .onNodeWithTag("mainScreenButton")
            .assertHasClickAction()
    }

    @Test
    fun testStackedCardsAreDisplayed() {
        composeTestRule.setContent {
            MainScreen()
        }

        // Check if the "stacked cards" exists
        composeTestRule
            .onNodeWithTag("stackedCards")
            .assertExists()
            .assertIsDisplayed()
    }
}