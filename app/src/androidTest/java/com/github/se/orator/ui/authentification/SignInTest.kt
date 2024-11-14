package com.github.se.orator.ui.authentification

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var userProfileViewModel: UserProfileViewModel
  @Mock private lateinit var userProfileRepository: UserProfileRepository

  @Test
  fun signInScreen_displaysLogoTitleAndButton() {

    // Mock
    val navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = userProfileViewModel)
    }

    // Check if elements are well displayed
    composeTestRule.onNodeWithContentDescription("App Logo").assertIsDisplayed()
    composeTestRule.onNodeWithText("OratorAI").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome !").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
  }

  @Test
  fun signInScreen_onClickLoginButton_triggersSignInFlow() {

    // Mock
    val navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = userProfileViewModel)
    }

    // Check click on login button
    composeTestRule.onNodeWithTag("loginButton").performClick()
  }

  @Test
  fun checkLoadingScreenIsDisplayed() {
    composeTestRule.setContent { LoadingScreen() }

    // Check if the LoadingScreen displays the loading indicator
    composeTestRule.onNodeWithText("Loading...").assertExists()
    composeTestRule.onNodeWithTag("loadingIndicator").assertExists()
  }
}
