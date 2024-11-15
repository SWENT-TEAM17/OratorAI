package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class ProfileScreenTest {
  private lateinit var userProfileViewModel: UserProfileViewModel
  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var userProfileRepository: UserProfileRepository

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
    MockitoAnnotations.openMocks(this)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    `when`(userProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<(UserProfile) -> Unit>(1)(testUserProfile)
    }

    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)

    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel.getUserProfile(testUserProfile.uid)

    // `when`(userProfileViewModel.userProfile).thenReturn(MutableStateFlow(testUserProfile))
  }

  @Test
  fun profileScreenComponentsAreDisplayed() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userProfileViewModel) }

    // Verify the settings button is displayed
    composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()

    // Verify the sign-out button is displayed
    composeTestRule.onNodeWithContentDescription("Sign out").assertIsDisplayed()

    // Verify the Edit Profile button is displayed
    composeTestRule.onNodeWithTag("edit_button").assertExists()

    // Verify the Achievements section is displayed
    composeTestRule.onNodeWithTag("statistics_section").assertIsDisplayed()

    // Verify the Previous Sessions section is displayed
    composeTestRule.onNodeWithTag("previous_sessions_section").assertIsDisplayed()
  }

  @Test
  fun settingsButtonNavigatesToSettings() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userProfileViewModel) }

    // Perform click on settings button
    composeTestRule.onNodeWithContentDescription("Settings").performClick()

    // Verify navigation to SETTINGS
    Mockito.verify(navigationActions).navigateTo(Screen.SETTINGS)
  }

  @Test
  fun signOutButtonIsFunctional() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userProfileViewModel) }

    // Perform click on sign-out button
    composeTestRule.onNodeWithContentDescription("Sign out").performClick()
  }

  @Test
  fun editProfileButtonNavigatesToEditProfile() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userProfileViewModel) }

    // Perform click on Edit Profile button
    composeTestRule.onNodeWithTag("edit_button", useUnmergedTree = true).performClick()

    // Verify navigation to EDIT_PROFILE
    Mockito.verify(navigationActions).navigateTo(Screen.EDIT_PROFILE)
  }

  @Test
  fun editDisplays() {
    composeTestRule.setContent { ProfileScreen(navigationActions, userProfileViewModel) }

    // Perform click on Edit Profile button
    composeTestRule.onNodeWithTag("edit_button", useUnmergedTree = true).performClick()

    composeTestRule.onNodeWithTag("achievements_cardsection").isDisplayed()
    composeTestRule.onNodeWithTag("previous_sessions_cardsection").isDisplayed()
  }
}
