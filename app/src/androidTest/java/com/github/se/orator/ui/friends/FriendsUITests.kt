package com.github.se.orator.ui.friends

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class FriendsUITests {

  @get:Rule val composeTestRule = createComposeRule()

  // Friends List
  private val profile1 = UserProfile("1", "John Doe", 99, statistics = UserStatistics())
  private val profile2 = UserProfile("2", "Jane Doe", 100, statistics = UserStatistics())

  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel

  private val testProfile = UserProfile("1", "John Doe", 99, statistics = UserStatistics())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    // When getUserProfiles is called on the repository, a custom list of profiles is passed.
    `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2))
    }
    `when`(mockUserProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<(UserProfile) -> Unit>(1)(testProfile)
    }
    `when`(mockUserProfileRepository.getAllUserProfiles(any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(0)(listOf(profile1, profile2))
    }

    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    userProfileViewModel.getUserProfile(testProfile.uid)
  }

  /**
   * Tests that the action button works correctly on the View Friends screen. It verifies that the
   * menu button exists, is displayed, and can be clicked. After clicking, it checks that the drawer
   * menu and its elements are displayed.
   */
  @Test
  fun testActionButtonWorks() {
    viewFriendsTestsSetup()

    composeTestRule
        .onNodeWithTag("viewFriendsMenuButton")
        .assertExists("The menu button on the view friends screen does not exist")
        .assertIsDisplayed()
        .performClick()

    composeTestRule.onNodeWithTag("viewFriendsDrawerMenu").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewFriendsDrawerTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewFriendsAddFriendButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewFriendsLeaderboardButton").assertIsDisplayed()
  }

  /**
   * Tests that the friends list is displayed correctly on the View Friends screen. It verifies that
   * the friends list exists and is displayed. It also checks that the first friend item is
   * displayed.
   */
  @Test
  fun testFriendsListIsDisplayed() {
    viewFriendsTestsSetup()

    composeTestRule.onNodeWithTag("viewFriendsList", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("viewFriendsItem#1", useUnmergedTree = true).assertIsDisplayed()
  }

  /**
   * Tests that the friends list is displayed correctly on the View Friends screen. It verifies that
   * the friends list exists and is displayed. It also checks that the first friend item is
   * displayed.
   */
  @Test
  fun testCanGoToAddFriendAndLeaderboardScreens() {
    viewFriendsTestsSetup()

    composeTestRule.onNodeWithTag("viewFriendsMenuButton").performClick()

    composeTestRule
        .onNodeWithTag("viewFriendsAddFriendButton", useUnmergedTree = true)
        .performClick()
    composeTestRule.waitForIdle()
    verify(mockNavigationActions).navigateTo(eq(Screen.ADD_FRIENDS))

    composeTestRule
        .onNodeWithTag("viewFriendsLeaderboardButton", useUnmergedTree = true)
        .performClick()
    composeTestRule.waitForIdle()
    verify(mockNavigationActions).navigateTo(eq(Screen.LEADERBOARD))
  }

  /**
   * Tests that the search functionality works correctly on the View Friends screen. It verifies
   * that the search field exists and is displayed, can be clicked and text can be inputted. After
   * inputting text, it checks that the first friend item is displayed and the second is not.
   */
  @Test
  fun testCanSearchForFriends() {
    viewFriendsTestsSetup()

    composeTestRule.onNodeWithTag("viewFriendsSearch").performClick()
    composeTestRule.onNodeWithTag("viewFriendsSearch").performTextInput("John")

    composeTestRule.onNodeWithTag("viewFriendsItem#1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("viewFriendsItem#2", useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  /**
   * Tests that the friends list is displayed correctly on the View Friends screen. It also checks
   * that the first friend item is displayed.
   */
  @Test
  fun testAddFriendsScreenElementsAreDisplayed() {

    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
    composeTestRule.setContent { AddFriendsScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the add friend screen elements are displayed
    composeTestRule.onNodeWithTag("addFriendTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFriendSearchField").assertIsDisplayed()
  }

  /**
   * Tests that the search functionality works correctly on the Add Friends screen. After inputting
   * text, it checks that the first friend item is not displayed because the user searching should
   * not be displayed and the second is not because it's not the right name.
   */
  @Test
  fun testAddFriendSearch() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
    composeTestRule.setContent { AddFriendsScreen(mockNavigationActions, userProfileViewModel) }

    Log.d("AddFriendSearch", userProfileViewModel.allProfiles.value.toString())
    composeTestRule.onNodeWithTag("addFriendSearchField").performTextInput("John")

    composeTestRule.onNodeWithTag("addFriendUserItem#1").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addFriendUserItem#2").assertIsNotDisplayed()
  }

  /** Tests that the leaderboard screen elements are displayed correctly. */
  @Test
  fun testLeaderboardScreenElementsAreDisplayed() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the leaderboard screen elements are displayed
    composeTestRule.onNodeWithTag("leaderboardTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaderboardList").assertIsDisplayed()
  }

  /** Tests that the leaderboard item elements are displayed correctly. */
  @Test
  fun testLeaderboardItemElementsAreDisplayed() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the leaderboard item elements are displayed
    composeTestRule.onNodeWithTag("leaderboardItem#1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaderboardItem#2", useUnmergedTree = true).assertIsDisplayed()
  }

  /** Function used to setup a testing environment for the ViewFriendsScreen tests */
  private fun viewFriendsTestsSetup() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.FRIENDS)
    composeTestRule.setContent { ViewFriendsScreen(mockNavigationActions, userProfileViewModel) }
  }

  @Test
  fun testPracticeModeSelector() {
    // Set up the content with the PracticeModeSelector composable
    composeTestRule.setContent { PracticeModeSelector() }

    // Open the dropdown menu
    composeTestRule.onNodeWithTag("practiceModeSelector").performClick()

    // Verify the dropdown menu options are displayed
    composeTestRule.onNodeWithTag("practiceModeOption1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("practiceModeOption2").assertIsDisplayed()

    // Select "Practice mode 2"
    composeTestRule.onNodeWithTag("practiceModeOption2").performClick()
  }

  @Test
  fun deleteFriendButtonCallsRepositoryUpdateProfileMethod() {
    // Define test profiles
    val profile1 = UserProfile(
      uid = "profile1",
      name = "Friend One",
      age = 25,
      statistics = UserStatistics(),
      friends = listOf(testProfile.uid),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Bio of Friend One"
    )

    val profile2 = UserProfile(
      uid = "profile2",
      name = "Friend Two",
      age = 30,
      statistics = UserStatistics(),
      friends = listOf(testProfile.uid),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Bio of Friend Two"
    )

    val testProfile = UserProfile(
      uid = "testUser",
      name = "Test User",
      age = 28,
      statistics = UserStatistics(),
      friends = listOf(profile1.uid, profile2.uid),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Test User Bio"
    )

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    // Mock getUserProfile to return testProfile
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile)
    }

    // Mock getFriendsProfiles to return profile1 and profile2
    `when`(mockUserProfileRepository.getFriendsProfiles(eq(listOf(profile1.uid, profile2.uid)), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
      onSuccess(listOf(profile1, profile2))
    }

    // Mock deleteFriend to simulate successful deletion
    `when`(mockUserProfileRepository.deleteFriend(eq(testProfile.uid), eq(profile1.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    // Initialize ViewModel with mocked repository
    val userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)

    // Trigger the initial profile fetch
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Set up the content with the ViewFriendsScreen composable
    composeTestRule.setContent { ViewFriendsScreen(mockNavigationActions, userProfileViewModel) }

    // Assert that profile2 is displayed
    composeTestRule
      .onNodeWithTag("viewFriendsItem#${profile2.uid}", useUnmergedTree = true)
      .assertExists()
      .assertIsDisplayed()

    // Assert that deleteFriendButton for profile1 is displayed initially
    composeTestRule
      .onNodeWithTag("deleteFriendButton#${profile1.uid}", useUnmergedTree = true)
      .assertExists()
      .assertIsDisplayed()

    // Click the delete friend button for profile1
    composeTestRule
      .onNodeWithTag("deleteFriendButton#${profile1.uid}", useUnmergedTree = true)
      .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that deleteFriend was called with correct parameters
    verify(mockUserProfileRepository).deleteFriend(eq(testProfile.uid), eq(profile1.uid), any(), any())

    // Assert that the deleteFriendButton for profile1 is no longer displayed
    composeTestRule
      .onNodeWithTag("deleteFriendButton#${profile1.uid}", useUnmergedTree = true)
      .assertDoesNotExist()

    // Optionally, assert that profile2 is still displayed
    composeTestRule
      .onNodeWithTag("viewFriendsItem#${profile2.uid}", useUnmergedTree = true)
      .assertIsDisplayed()
  }

}
