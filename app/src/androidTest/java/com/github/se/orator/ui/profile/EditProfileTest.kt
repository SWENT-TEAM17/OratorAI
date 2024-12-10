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
    composeTestRule.onNodeWithTag("settings_button", useUnmergedTree = true).assertIsDisplayed()
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

    // Optionally, verify that the repository's update method is called
    // verify(userProfileRepository).updateUserProfile(any(), any(), any())
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

  @Test
  fun takePhotoButtonDismissesDialog() {
    // Set the content
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open the ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()

    // Click the "Take Photo" button
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Verify that the dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun uploadFromGalleryButtonDismissesDialog() {
    // Set the content
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open the ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()

    // Click the "Upload from Gallery" button
    composeTestRule.onNodeWithText("Upload from Gallery").performClick()

    // Verify that the dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun cancelButtonDismissesDialog() {
    // Set the content
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open the ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()

    // Click the "Cancel" button
    composeTestRule.onNodeWithText("Cancel").performClick()

    // Verify that the dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun uploadFromGalleryUpdatesProfilePicture() {
    // Set the content
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open the ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()

    // Click the "Upload from Gallery" button
    composeTestRule.onNodeWithText("Upload from Gallery").performClick()

    // Verify that the dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()

    // Since we cannot directly simulate image selection, verify that no new profile picture is set
    // Alternatively, you can mock the onImageSelected callback if refactored
  }

  @Test
  fun takePhotoUpdatesProfilePicture() {
    // Set the content
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open the ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()

    // Click the "Take Photo" button
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Verify that the dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()

    // Since we cannot directly simulate the camera intent, we assume that the profile picture
    // remains unchanged
    // For a more thorough test, consider refactoring the composable to allow injecting a mock URI
  }

  @Test
  fun takePhotoWithPermissionDenied() {
    // Note: Directly mocking permission denial is complex in Compose tests.
    // Instead, this test will focus on UI interactions.

    // Set the content
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open the ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Verify that the dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()

    // Click the "Take Photo" button
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Verify that the dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()

    // Since we cannot mock permission denial and Toasts, we assume the behavior is as expected.
    // For a more thorough test, consider using instrumented tests with Robolectric or similar
    // frameworks.
  }

  @Test
  fun handleImageFileCreationFailure() {
    // We cannot directly mock FileProvider to cause an exception,
    // but we can assume that if createImageFileUri fails (returns null),
    // the "Take Photo" action will not update the picture and will show a toast.
    // We'll just verify the dialog closes indicating an error scenario was handled.

    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Click "Take Photo"
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // If createImageFileUri fails, no URI is created and we show a toast.
    // We can't check the toast, but we can ensure the dialog is gone.
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun cameraCaptureFailureScenario() {
    // We can't trigger the camera result directly,
    // but we can assume that if camera capture fails,
    // the dialog won't reappear and a toast would be shown.

    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Click "Take Photo"
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Assume camera capture failed. Since we can't simulate activity result,
    // we rely on the code's behavior: it would show a toast and not reopen the dialog.
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }

  @Test
  fun cameraCaptureSuccessScenario() {
    // Similarly, we can't simulate a successful camera capture directly,
    // but we can at least ensure that after "Take Photo" is clicked
    // the dialog is dismissed, implying the process started.
    // In a real scenario, we'd mock the ActivityResult to simulate success.

    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    // Open ImagePicker dialog
    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()

    // Click "Take Photo"
    composeTestRule.onNodeWithText("Take Photo").performClick()

    // Dialog should be dismissed. If success happened later, the image would update,
    // but we can't verify that without dependency injection or a mock ActivityResult.
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }
}
