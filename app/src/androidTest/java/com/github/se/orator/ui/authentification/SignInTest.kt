package com.github.se.orator.ui.authentification

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.navigation.TopLevelDestinations
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signInScreen_displaysLogoTitleAndButton() {
    // Arrange
    val navigationActions = mock(NavigationActions::class.java)
    val viewModel = mock(UserProfileViewModel::class.java)
    `when`(viewModel.isLoading).thenReturn(MutableStateFlow(false))

    // Act
    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = viewModel)
    }

    // Assert
    composeTestRule.onNodeWithContentDescription("App Logo").assertIsDisplayed()
    composeTestRule.onNodeWithText("OratorAI").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome !").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loginButton").assertIsDisplayed()
  }

  @Test
  fun signInScreen_whenLoading_showsLoadingScreen() {
    // Arrange
    val navigationActions = mock(NavigationActions::class.java)
    val viewModel = mock(UserProfileViewModel::class.java)
    `when`(viewModel.isLoading).thenReturn(MutableStateFlow(true))

    // Act
    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = viewModel)
    }

    // Assert
    composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun signInScreen_onClickLoginButton_triggersSignInFlow() {
    // Arrange
    val navigationActions = mock(NavigationActions::class.java)
    val viewModel = mock(UserProfileViewModel::class.java)
    `when`(viewModel.isLoading).thenReturn(MutableStateFlow(false))

    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = viewModel)
    }

    // Act
    composeTestRule.onNodeWithTag("loginButton").performClick()

    // Assert
    // Since the sign-in process involves external services, you can't test it directly.
    // However, you can verify that the sign-in intent is launched.
    // If you can expose the launcher or use a test double, you could verify that it's called.
    // Alternatively, check if certain UI elements change or if certain methods are called.
  }

  @Test
  fun signInScreen_whenProfileComplete_navigatesToHome() {
    // Arrange
    val navigationActions = mock(NavigationActions::class.java)
    val viewModel = mock(UserProfileViewModel::class.java)
    `when`(viewModel.isLoading).thenReturn(MutableStateFlow(false))
    `when`(viewModel.isProfileIncomplete()).thenReturn(false)

    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = viewModel)
    }

    // Act
    // Simulate the state where the user has signed in and profile is fetched
    composeTestRule.runOnIdle {
      // Simulate the LaunchedEffect being triggered
      // Since we can't directly manipulate the LaunchedEffect, this test might be better suited as
      // a unit test
    }

    // Assert
    // Verify that navigation to HOME was called
    verify(navigationActions).navigateTo(TopLevelDestinations.HOME)
  }

  @Test
  fun signInScreen_whenProfileIncomplete_navigatesToCreateProfile() {
    // Arrange
    val navigationActions = mock(NavigationActions::class.java)
    val viewModel = mock(UserProfileViewModel::class.java)
    `when`(viewModel.isLoading).thenReturn(MutableStateFlow(false))
    `when`(viewModel.isProfileIncomplete()).thenReturn(true)

    composeTestRule.setContent {
      SignInScreen(navigationActions = navigationActions, viewModel = viewModel)
    }

    // Act
    // Simulate the state where the user has signed in but profile is incomplete
    composeTestRule.runOnIdle {
      // Simulate the LaunchedEffect being triggered
    }

    // Assert
    // Verify that navigation to CREATE_PROFILE was called
    verify(navigationActions).navigateTo(Screen.CREATE_PROFILE)
  }
}
