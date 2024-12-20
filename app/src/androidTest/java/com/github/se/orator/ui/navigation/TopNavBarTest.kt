package com.github.se.orator.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class TopNavBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topNavigationMenu_displaysDefaultContent() {
    // Set the content with default parameters
    composeTestRule.setContent { TopNavigationMenu("") }

    // Verify the top app bar with the default test tag is displayed
    composeTestRule.onNodeWithTag("top_app_bar").assertIsDisplayed()
  }

  @Test
  fun topNavigationMenu_displaysCustomTitle() {
    // Set the content with a custom title
    composeTestRule.setContent { TopNavigationMenu(title = "Custom Title") }

    // Verify the custom title is displayed
    composeTestRule.onNodeWithText("Custom Title").assertIsDisplayed()
  }

  @Test
  fun topNavigationMenu_displaysCustomActions() {
    // Set the content with custom actions
    composeTestRule.setContent { TopNavigationMenu(actions = { Text("Action") }) }

    // Verify the custom action is displayed
    composeTestRule.onNodeWithText("Action").assertIsDisplayed()
  }

  @Test
  fun topNavigationMenu_displaysCustomNavigationIcon() {
    // Set the content with a custom navigation icon
    composeTestRule.setContent { TopNavigationMenu(navigationIcon = { Text("NavIcon") }) }

    // Verify the custom navigation icon is displayed
    composeTestRule.onNodeWithText("NavIcon").assertIsDisplayed()
  }

  @Test
  fun topNavigationMenu_appliesModifiersCorrectly() {
    // Set the content
    composeTestRule.setContent { TopNavigationMenu(testTag = "custom_test_tag") }

    // Verify the test tag is applied correctly
    composeTestRule.onNodeWithTag("custom_test_tag").assertIsDisplayed()
  }

  @Test
  fun topNavigationMenu_displaysHorizontalDivider() {
    // Set the content
    composeTestRule.setContent { TopNavigationMenu() }

    // Verify the horizontal divider is displayed (indirectly by ensuring TopAppBar renders)
    composeTestRule.onNodeWithTag("top_app_bar").assertIsDisplayed()
  }
}
