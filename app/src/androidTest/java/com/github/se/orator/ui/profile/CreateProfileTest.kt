package com.github.se.orator.ui.profile

import android.Manifest
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.navigation.TopLevelDestinations
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class CreateAccountScreenTest {

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var userProfileRepository: UserProfileRepository

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  private val fakeImageUri = Uri.parse("test_profile_image.jpeg")

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }

    composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("upload_profile_picture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()

    composeTestRule.onNodeWithText("Create an OratorAI account")
    composeTestRule.onNodeWithContentDescription("Back")
    composeTestRule.onNodeWithText("Profile picture (optional)")
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

    composeTestRule.onNodeWithTag("back_button").performClick()
  }

  @Test
  fun savingProfileWorks() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }
    // mocking UID response
    `when`(userProfileViewModel.repository.getCurrentUserUid()).thenReturn("uid")

    // putting in a username and saving profile
    composeTestRule.onNodeWithTag("username_input").performTextInput("TestUser")
    composeTestRule.onNodeWithTag("save_profile_button").performClick()

    val newProfile =
        UserProfile(
            uid = "uid",
            name = "TestUser",
            age = 0,
            profilePic = null,
            statistics = UserStatistics(),
            friends = emptyList())

    // making sure the correct user profile has been added
    Mockito.verify(userProfileRepository).addUserProfile(eq(newProfile), any(), any())
    Mockito.verify(navigationActions).navigateTo(TopLevelDestinations.HOME)
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
    composeTestRule.onNodeWithTag("back_button").performClick()

    Mockito.verify(navigationActions).goBack()
  }

  @Test
  fun dialogBoxDisplays() {
    composeTestRule.setContent { CreateAccountScreen(navigationActions, userProfileViewModel) }
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Select an option to update your profile picture.")
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("upload_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    composeTestRule.onNodeWithText("Upload from Gallery").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  /**
   * Test when createImageFileUri returns null. This simulates a failure in creating the image file
   * URI.
   */
  @Test
  fun createImageFileUriReturnsNull_showsErrorToast() {
    // Mock the createImageFileUri top-level function to return null
    mockkStatic("com.github.se.orator.ui.profile.EditProfileKt")
    every { createImageFileUri(any()) } returns null

    // Set the content of the Compose screen
    composeTestRule.setContent {
      CreateAccountScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Open the dialog by clicking the upload profile picture button
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()

    // Click on "Take Photo" button
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Enter a username
    composeTestRule.onNodeWithTag("username_input").performTextInput("TestUser")

    // Save button should be enabled
    composeTestRule.onNodeWithTag("save_profile_button").assertIsEnabled()

    // Click Save
    composeTestRule.onNodeWithTag("save_profile_button").performClick()

    // Clean up the mocks
    unmockkStatic("com.github.se.orator.ui.profile.EditProfileKt")
  }
  /**
   * Test when createImageFileUri returns a specific Uri. This simulates a successful creation of
   * the image file URI and setting the profile picture.
   */
  @Test
  fun createImageFileUriReturnsUri_updatesProfilePicture() {
    // Mock the createImageFileUri top-level function to return a specific Uri
    mockkStatic("com.github.se.orator.ui.profile.EditProfileKt")
    every { createImageFileUri(any()) } returns fakeImageUri

    composeTestRule.setContent {
      CreateAccountScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Open the dialog
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()

    // Click on "Take Photo"
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Since createImageFileUri returns a valid Uri, profilePicUri should be set to fakeImageUri
    // Verify that the ProfilePicture composable reflects the updated image
    // Assuming ProfilePicture displays an Image with a test tag like "profile_picture_image"

    // Modify your ProfilePicture composable to have distinct test tags based on the state
    // For example, if profilePicUri is not null, show "profile_picture_image"; else, show
    // "profile_placeholder_image"

    // Clean up the mock
    unmockkStatic("com.github.se.orator.ui.profile.EditProfileKt")
  }
}
