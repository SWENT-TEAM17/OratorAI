package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
    //composeTestRule.onNodeWithTag("BackArrowImage").assertContentDescriptionEquals("Back")
    composeTestRule.onNodeWithTag("settings_button", useUnmergedTree = true).assertIsDisplayed()
    //composeTestRule
    //    .onNodeWithTag("settings_button", useUnmergedTree = true)
    //    .assertContentDescriptionEquals("Settings")
    composeTestRule.onNodeWithContentDescription("Change Profile Picture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("username_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("BIO").assertIsDisplayed()
    composeTestRule.onNodeWithTag("bio_field").assertIsDisplayed()
    composeTestRule.onNodeWithText("Save changes").assertIsDisplayed()
  }

  @Test
  fun saveChangesWorks() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }
    composeTestRule.onNodeWithText("Save changes").performClick()

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
    composeTestRule.onNodeWithContentDescription("Change Profile Picture").performClick()
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()
  }

  @Test
  fun changeName() {
    composeTestRule.setContent { EditProfileScreen(navigationActions, userProfileViewModel) }

    composeTestRule.onNodeWithTag("username_field").performTextInput("TestName")
    composeTestRule.onNodeWithTag("username_field").assertTextContains("TestName")

    composeTestRule.onNodeWithTag("bio_field").performTextInput("I like cats")
    composeTestRule.onNodeWithTag("bio_field").assertTextEquals("I like cats")

    composeTestRule.onNodeWithText("Save changes").performClick()

    // update or create either one
    //        verify(userProfileRepository).updateUserProfile(any(), any(), any())
  }
}
