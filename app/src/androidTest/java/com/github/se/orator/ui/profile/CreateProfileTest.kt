package com.github.se.orator.ui.profile

import android.Manifest
import android.content.Context
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
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.mockkStatic
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
  private lateinit var mockContext: Context

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
    MockKAnnotations.init(this)
    mockContext = mockk(relaxed = true)
    mockkStatic("android.widget.Toast") // Mock Toast
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
    composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    composeTestRule.onNodeWithText("Upload from Gallery").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun createAccountScreen_uploadButton_opensImagePickerDialog() {
    composeTestRule.setContent {
      CreateAccountScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Click the upload profile picture button
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()

    // Verify that the ImagePicker dialog is displayed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()
    composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    composeTestRule.onNodeWithText("Upload from Gallery").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
  }

  @Test
  fun createAccountScreen_uploadFromGalleryButton_launchesGalleryPicker() {
    composeTestRule.setContent {
      CreateAccountScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Click the upload profile picture button to open ImagePicker
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()

    // Click the "Upload from Gallery" button within ImagePicker
    composeTestRule.onNodeWithTag("PhotoOnPick").performClick()

    // Since launching gallery involves external intents, we can verify that the launcher was called
    // However, Compose testing does not support verifying ActivityResultLauncher calls directly
    // Alternatively, you can verify side effects or state changes if any
  }

  @Test
  fun createAccountScreen_cancelButton_dismissesImagePickerDialog() {
    composeTestRule.setContent {
      CreateAccountScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Click the upload profile picture button to open ImagePicker
    composeTestRule.onNodeWithTag("upload_profile_picture").performClick()

    // Click the "Cancel" button within ImagePicker
    composeTestRule.onNodeWithTag("PhotoOnDismiss").performClick()

    // Verify that the ImagePicker dialog is dismissed
    composeTestRule.onNodeWithText("Choose Profile Picture").assertDoesNotExist()
  }
}
