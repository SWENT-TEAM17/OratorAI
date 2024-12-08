package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class EditProfileTest {
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var navigationActions: NavigationActions
  private lateinit var userProfileRepository: UserProfileRepository

  private val testUserProfile =
      UserProfile(
          uid = "testUid",
          name = "Test User",
          age = 25,
          statistics = UserStatistics(),
          friends = listOf("friend1", "friend2"),
          bio = "Test bio")

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
    `when`(userProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<(UserProfile) -> Unit>(1)(testUserProfile)
    }
  }

  @Test
  fun everyingIsDisplayed() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    composeTestRule.onNodeWithText("Edit Profile").assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button", useUnmergedTree = true).assertIsDisplayed()
    // composeTestRule.onNodeWithTag("BackArrowImage").assertContentDescriptionEquals("Back")
    composeTestRule.onNodeWithTag("settings_button", useUnmergedTree = true).assertIsDisplayed()
    // composeTestRule
    //    .onNodeWithTag("settings_button", useUnmergedTree = true)
    //    .assertContentDescriptionEquals("Settings")
    composeTestRule.onNodeWithTag("upload_profile_picture_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bio_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()
  }

  @Test
  fun saveChangesWorks() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }
    composeTestRule.onNodeWithTag("save_profile_button").performClick()

    verify(navigationActions).goBack()
  }

  @Test
  fun backArrowWorks() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }
    composeTestRule.onNodeWithTag("back_button", useUnmergedTree = true).performClick()

    Thread.sleep(5000)
    verify(navigationActions).goBack()
  }

  @Test
  fun settingsButtonWorks() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }
    composeTestRule.onNodeWithTag("settings_button", useUnmergedTree = true).performClick()

    Mockito.verify(navigationActions).navigateTo(Screen.SETTINGS)
  }

  @Test
  fun editPicture() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()
    composeTestRule.onNodeWithTag("upload_profile_picture_button").assertIsDisplayed()
  }

  @Test
  fun changeName() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    composeTestRule.onNodeWithTag("username_field").performTextInput("TestName")
    composeTestRule.onNodeWithTag("username_field").assertTextContains("TestName")

    composeTestRule.onNodeWithTag("bio_field").performTextInput("I like cats")

    composeTestRule.onNodeWithText("Save changes").performClick()

    // update or create either one
    //        verify(userProfileRepository).updateUserProfile(any(), any(), any())
  }

  @Test
  fun edit_uploadFromGalleryButton_launchesGalleryPicker() {
    composeTestRule.setContent {
      EditProfileScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Click the upload profile picture button to open ImagePicker
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Click the "Upload from Gallery" button within ImagePicker
    composeTestRule.onNodeWithTag("PhotoOnPick").performClick()

    // Since launching gallery involves external intents, we can verify that the ImagePicker dialog
    // is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun edit_cancelButton_dismissesImagePickerDialog() {
    composeTestRule.setContent {
      EditProfileScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Click the upload profile picture button to open ImagePicker
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Click the "Cancel" button within ImagePicker
    composeTestRule.onNodeWithTag("PhotoOnDismiss").performClick()

    // Verify that the ImagePicker dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun editProfileScreen_dialogBoxDisplaysCorrectly() {
    composeTestRule.setContent {
      EditProfileScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Click the upload profile picture button to open ImagePicker
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that all elements of the ImagePicker dialog are displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Select an option to update your profile picture.")
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    composeTestRule.onNodeWithText("Upload from Gallery").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }
}
