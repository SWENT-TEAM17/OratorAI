package com.github.se.orator.endtoend

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.friends.AddFriendsScreen
import com.github.se.orator.ui.friends.ViewFriendsScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.profile.CreateAccountScreen
import com.github.se.orator.ui.profile.EditProfileScreen
import com.github.se.orator.ui.profile.ProfileScreen
import com.github.se.orator.ui.settings.SettingsScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

class EndToEndAppTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel
  private val testUserProfile =
      UserProfile(
          uid = "testUid",
          name = "",
          age = 25,
          statistics = UserStatistics(),
          friends = listOf("friend1", "friend2"),
          bio = "Test bio")

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    `when`(
            userProfileRepository.getUserProfile(
                org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .then { it.getArgument<(UserProfile) -> Unit>(1)(testUserProfile) }

    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)

    `when`(navigationActions.goBack()).then { navController?.popBackStack() ?: {} }
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel.getUserProfile(testUserProfile.uid)
  }

  var navController: NavHostController? = null

  @Test
  fun testEndToEndNavigationAndUI() {
    // Set up NavHost for navigation and initialize different screens within one setContent
    composeTestRule.setContent {
      navController = rememberNavController()
      NavHost(navController = navController!!, startDestination = Screen.HOME) {
        composable(Screen.HOME) { ProfileScreen(navigationActions, userProfileViewModel) }
        composable(Screen.SETTINGS) { SettingsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.FRIENDS) { ViewFriendsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.ADD_FRIENDS) { AddFriendsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.EDIT_PROFILE) {
          EditProfileScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.CREATE_PROFILE) {
          CreateAccountScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.PROFILE) { ProfileScreen(navigationActions, userProfileViewModel) }
      }
    }

    // Step 1: Navigate from Home to Profile
    composeTestRule.onNodeWithTag("Profile").performClick() // Simulate click on Profile button

    // Step 2: Navigate from Profile to Settings
    composeTestRule
        .onNodeWithContentDescription("Settings")
        .performClick() // Simulate click on Settings button
    verify(navigationActions).navigateTo(Screen.SETTINGS)
    composeTestRule.runOnUiThread {
      navController?.navigate(Screen.SETTINGS) // Force the navigation programmatically
    }

    // Test that each setting button exists and is clickable
    val settingsTags =
        listOf(
            "account_management",
            "storage_settings",
            "permissions",
            "theme",
            "invite_friends",
            "notifications",
            "rate_on_the_app_store",
            "about")

    settingsTags.forEach { tag ->
      composeTestRule.onNodeWithTag(tag).assertExists()
      composeTestRule.onNodeWithTag(tag).performClick()
    }

    // Step 3: Use the back button to return to Profile
    composeTestRule
        .onNodeWithTag("back_button")
        .performClick() // Simulate clicking the back button
    verify(navigationActions).goBack() // Ensure it navigates back to Profile

    // Step 4: Navigate to Edit Profile from Profile
    composeTestRule.onNodeWithTag("edit_button").performClick() // Simulate clicking Edit Profile
    verify(navigationActions).navigateTo(Screen.EDIT_PROFILE)

    composeTestRule.runOnUiThread {
      navController?.navigate(Screen.EDIT_PROFILE) // Force the navigation programmatically
    }
    composeTestRule.onNodeWithTag("username_field").performTextInput("TestName")
    composeTestRule.onNodeWithTag("username_field").assertTextContains("TestName")

    composeTestRule.onNodeWithTag("upload_profile_picture_button").performClick()
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.onNodeWithText("Choose Profile Picture").assertIsNotDisplayed()
    // Step 5: Return back to Profile using the back button
    composeTestRule
        .onNodeWithTag("back_button")
        .performClick() // Simulate back button click

    // Step 6: Navigate to Friends from Profile
    composeTestRule.onNodeWithTag("Friends").performClick() // Simulate clicking Friends button

    composeTestRule.runOnUiThread {
      navController?.navigate(Screen.FRIENDS) // Force the navigation programmatically
    }

    // Step 7: Navigate back to Home from Friends
    composeTestRule.onNodeWithTag("Home").performClick() // Simulate clicking Home button

    composeTestRule.runOnUiThread {
      navController?.navigate(Screen.HOME) // Force the navigation programmatically
    }
  }
}
