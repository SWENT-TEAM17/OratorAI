package com.github.se.orator.ui.friends

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class ViewFriendsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockFriendsProfiles =
      listOf(
          UserProfile(
              "1",
              "John Doe",
              25,
              "https://someurl.com/johndoe.jpg",
              statistics = null,
              friends = listOf(),
              bio = "Loves coding"),
          UserProfile(
              "2",
              "Jane Smith",
              30,
              "https://someurl.com/janesmith.jpg",
              statistics = null,
              friends = listOf(),
              bio = "UI/UX Designer"))

  @Test
  fun viewFriendsScreen_displaysFriendsList() {
    // Arrange: Create mock ViewModel and NavigationActions
    val mockViewModel =
        mock<UserProfileViewModel> {
          on { friendsProfiles } doReturn MutableStateFlow(mockFriendsProfiles)
        }
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Assert: Check if the friends are displayed
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
  }

  @Test
  fun viewFriendsScreen_searchBarFiltersFriends() {
    // Arrange: Mock ViewModel and NavigationActions
    val mockViewModel =
        mock<UserProfileViewModel> {
          on { friendsProfiles } doReturn MutableStateFlow(mockFriendsProfiles)
        }
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Act: Perform text input into the search bar
    composeTestRule.onNodeWithText("Search for a friend").performTextInput("Jane")

    // Assert: Only Jane Smith should be visible after search
    composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertDoesNotExist()
  }

  @Test
  fun viewFriendsScreen_addFriendButtonNavigatesToAddFriendScreen() {
    // Arrange: Create mock ViewModel and NavigationActions
    val mockViewModel = mock<UserProfileViewModel>()
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Act: Click on the "Add a friend" button
    composeTestRule.onNodeWithText("➕ Add a friend").performClick()

    // Assert: Verify that navigation to the Add Friends screen occurred
    verify(mockNavActions).navigateTo(Screen.ADD_FRIENDS)
  }

  @Test
  fun viewFriendsScreen_noFriendsFoundShowsNoUserMessage() {
    // Arrange: Create mock ViewModel with empty friends list
    val mockViewModel =
        mock<UserProfileViewModel> {
          on { friendsProfiles } doReturn MutableStateFlow(emptyList<UserProfile>())
        }
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Assert: Verify that "No user found" message is displayed
    composeTestRule.onNodeWithText("No user found").assertIsDisplayed()
  }

  @Test
  fun viewFriendsScreen_leaderboardButtonNavigatesToLeaderboardScreen() {
    // Arrange: Create mock ViewModel and NavigationActions
    val mockViewModel = mock<UserProfileViewModel>()
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Act: Click on the "Leaderboard" button
    composeTestRule.onNodeWithText("⭐ Leaderboard").performClick()

    // Assert: Verify that navigation to the Leaderboard screen occurred
    verify(mockNavActions).navigateTo(Screen.LEADERBOARD)
  }

  @Test
  fun viewFriendsScreen_menuOpensDrawer() {
    // Arrange: Mock ViewModel and NavigationActions
    val mockViewModel = mock<UserProfileViewModel>()
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Act: Click on the menu icon
    composeTestRule.onNodeWithContentDescription("Menu").performClick()

    // Assert: Check if drawer content is displayed
    composeTestRule.onNodeWithText("Actions").assertIsDisplayed()
  }

  @Test
  fun viewFriendsScreen_clickOutsideSearchBarRemovesFocus() {
    // Arrange: Mock ViewModel and NavigationActions
    val mockViewModel =
        mock<UserProfileViewModel> {
          on { friendsProfiles } doReturn MutableStateFlow(mockFriendsProfiles)
        }
    val mockNavActions = mock<NavigationActions>()

    // Act: Render the composable
    composeTestRule.setContent {
      ViewFriendsScreen(navigationActions = mockNavActions, userProfileViewModel = mockViewModel)
    }

    // Act: Focus on search bar, then click outside
    composeTestRule.onNodeWithText("Search for a friend").performClick()
    composeTestRule.onNodeWithText("My Friends").performClick()

    // Assert: Verify that focus is removed from search bar
    composeTestRule.onNodeWithText("Search for a friend").assertIsNotFocused()
  }
}
