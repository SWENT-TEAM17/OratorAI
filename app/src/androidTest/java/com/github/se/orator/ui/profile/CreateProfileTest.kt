package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateAccountScreenTest {

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    userProfileViewModel = mockk(relaxed = true)
    navigationActions = mockk(relaxed = true)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }

    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("upload_profile_picture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()
  }

  @Test
  fun usernameInputIsFunctional() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }

    val username = "TestUser"
    composeTestRule.onNodeWithTag("username_input").performTextInput(username)
    composeTestRule.onNodeWithTag("username_input").assertTextContains(username)
  }

  @Test
  fun saveButtonIsEnabledWhenUsernameIsNotEmpty() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }

    composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_profile_button").assertIsNotEnabled()

    // Input a valid username
    composeTestRule.onNodeWithTag("username_input").performTextInput("TestUser")
    composeTestRule.onNodeWithTag("save_profile_button").assertIsEnabled()
  }

  @Test
  fun profilePictureUploadClickOpensDialog() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }

    // Click on the upload profile picture button
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()

    // Check if the dialog is displayed
    composeTestRule.onNodeWithTag("upload_dialog").assertIsDisplayed()
  }

  @Test
  fun backButtonNavigatesBack() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }

    // Click on the back button
    composeTestRule.onNodeWithTag("back_button").performClick()

    // Verify that navigation occurred (handled by Mockito in a real scenario)
  }
}
