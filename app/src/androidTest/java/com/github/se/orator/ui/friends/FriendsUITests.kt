package com.github.se.orator.ui.friends

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
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
import org.mockito.Mockito.doAnswer
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
    // When getFriendsProfiles is called on the repository, a custom list of profiles is passed.
    `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2))
    }
    `when`(mockUserProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<(UserProfile?) -> Unit>(1)(testProfile)
    }
    `when`(mockUserProfileRepository.getAllUserProfiles(any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(0)(listOf(profile1, profile2))
    }

    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    userProfileViewModel.getUserProfile(testProfile.uid)
  }

  /**
   * Function used to setup a testing environment for the ViewFriendsScreen tests
   */
  private fun viewFriendsTestsSetup() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.FRIENDS)
    composeTestRule.setContent { ViewFriendsScreen(mockNavigationActions, userProfileViewModel) }
  }

  /**
   * ============================
   * Existing Tests
   * ============================
   */

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
   * Tests that navigation to Add Friend and Leaderboard screens works correctly.
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
   * Tests that the Add Friends screen elements are displayed correctly.
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

  /**
   * Tests that the leaderboard screen elements are displayed correctly.
   */
  @Test
  fun testLeaderboardScreenElementsAreDisplayed() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the leaderboard screen elements are displayed
    composeTestRule.onNodeWithTag("leaderboardTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaderboardList").assertIsDisplayed()
  }

  /**
   * Tests that the leaderboard item elements are displayed correctly.
   */
  @Test
  fun testLeaderboardItemElementsAreDisplayed() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the leaderboard item elements are displayed
    composeTestRule.onNodeWithTag("leaderboardItem#1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaderboardItem#2", useUnmergedTree = true).assertIsDisplayed()
  }

  /**
   * Tests the Practice Mode Selector functionality.
   */
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

    // Optionally, verify that the selected mode is reflected
    composeTestRule.onNodeWithTag("practiceModeSelector").assertTextEquals("Practice mode 2")
  }

  /**
   * Tests that deleting a friend calls the repository's deleteFriend method and updates the UI accordingly.
   */
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

  /**
   * ============================
   * Added Tests Start Here
   * ============================
   */

  /**
   * Tests sending a friend request.
   */
  @Test
  fun testSendFriendRequest() {
    // Define test profiles
    val testProfile = UserProfile(
      uid = "testUser",
      name = "Test User",
      age = 28,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Test User Bio"
    )

    val profile3 = UserProfile(
      uid = "3",
      name = "Friend Three",
      age = 22,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Bio of Friend Three"
    )

    // Mock repository methods

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    // Mock getUserProfile to return testProfile initially with no sent requests
    doAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile)
      null
    }.`when`(mockUserProfileRepository).getUserProfile(eq(testProfile.uid), any(), any())

    // Mock getFriendsProfiles to return an empty list as there are no friends initially
    doAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
      onSuccess(emptyList())
      null
    }.`when`(mockUserProfileRepository).getFriendsProfiles(eq(emptyList()), any(), any())

    // Mock getAllUserProfiles to include profile3
    doAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(0)
      onSuccess(listOf(profile3)) // Include profile3 in allProfiles
      null
    }.`when`(mockUserProfileRepository).getAllUserProfiles(any(), any())

    // Mock sendFriendRequest to simulate successful request
    doAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
      null
    }.`when`(mockUserProfileRepository).sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

    // After sending, sent requests include profile3
    val updatedTestProfile = testProfile.copy(sentReq = listOf(profile3.uid))
    doAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(updatedTestProfile)
      null
    }.`when`(mockUserProfileRepository).getUserProfile(eq(testProfile.uid), any(), any())

    // Set up the AddFriendsScreen composable
    composeTestRule.setContent {
      AddFriendsScreen(navigationActions = mockNavigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Wait for the initial UI to render
    composeTestRule.waitForIdle()

    // **Input a search query that matches profile3's name**
    composeTestRule
      .onNodeWithTag("addFriendSearchField")
      .assertExists("No search bar is being displayed.")
      .performTextInput("Friend Three") // Adjust the query to match profile3.name

    // Wait for the UI to process the search
    composeTestRule.waitForIdle()

    // **Log available testTags to verify presence**
    composeTestRule.onAllNodesWithTag("*").printToLog("AvailableTestTagsAfterSearch")

    // **Locate and click the "Send Friend Request" button with the correct testTag**
    composeTestRule
      .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
      .assertExists("Send Friend Request button does not exist for 3")
      .assertIsEnabled()
      .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // **Verify that sendFriendRequest was called with correct parameters**
    verify(mockUserProfileRepository).sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

    // **Assert that profile3 now appears in the "Sent Requests" list**
    composeTestRule
      .onNodeWithTag("sentFriendRequestItem#3", useUnmergedTree = true)
      .assertExists("Sent Request item for 3 does not exist")
      .assertIsDisplayed()

    // **Optionally, assert that the send button is disabled or shows "Request Sent"**
    composeTestRule
      .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
      .assertIsNotEnabled()
  }

  /**
   * Tests accepting a friend request.
   */
  @Test
  fun testAcceptFriendRequest() {
    // Define test profiles
    val testProfile = UserProfile(
      uid = "testUser",
      name = "Test User",
      age = 28,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = listOf("profile4"),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Test User Bio"
    )

    val incomingProfile = UserProfile(
      uid = "profile4",
      name = "Friend Four",
      age = 27,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = emptyList(),
      sentReq = listOf("testUser"),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Bio of Friend Four"
    )

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    `when`(mockUserProfileRepository.getRecReqProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(incomingProfile, profile2))
    }


    // Mock getUserProfile to return testProfile with incoming friend request
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile)
    }
    print(testProfile.recReq)

    // Mock getFriendsProfiles to return empty list initially
    `when`(mockUserProfileRepository.getFriendsProfiles(eq(emptyList()), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
      onSuccess(emptyList())
    }

    // Mock acceptFriendRequest to simulate successful acceptance
    `when`(mockUserProfileRepository.acceptFriendRequest(eq(testProfile.uid), eq(incomingProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    // After accepting, friends include incomingProfile and received requests are empty
    val updatedTestProfile = testProfile.copy(
      friends = listOf(incomingProfile.uid),
      recReq = emptyList()
    )
    val updatedIncomingProfile = incomingProfile.copy(
      friends = listOf(testProfile.uid)
    )
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(updatedTestProfile)
    }
    `when`(mockUserProfileRepository.getUserProfile(eq(incomingProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(updatedIncomingProfile)
    }

    // Set up the ViewFriendsScreen with incoming friend request
    viewFriendsTestsSetup()

    // Mocking received requests
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile.copy(recReq = listOf(incomingProfile.uid)))
    }

    // Update ViewModel state after accepting
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Assert that incomingProfile is displayed in the "Received Requests" list
    composeTestRule
      .onNodeWithTag("friendRequestItem#${incomingProfile.uid}", useUnmergedTree = true)
      .assertExists("Friend Request item for ${incomingProfile.uid} does not exist")
      .assertIsDisplayed()

    // Assume there's an accept button with testTag "acceptFriendButton#profile4"
    composeTestRule
      .onNodeWithTag("acceptFriendButton#${incomingProfile.uid}", useUnmergedTree = true)
      .assertExists("Accept Friend Request button does not exist for ${incomingProfile.uid}")
      .assertIsEnabled()
      .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that acceptFriendRequest was called with correct parameters
    verify(mockUserProfileRepository).acceptFriendRequest(eq(testProfile.uid), eq(incomingProfile.uid), any(), any())

    // Assert that incomingProfile no longer appears in the "Received Requests" list
    composeTestRule
      .onNodeWithTag("friendRequestItem#${incomingProfile.uid}", useUnmergedTree = true)
      .assertDoesNotExist()

    // Assert that incomingProfile now appears in the "Friends" list
    composeTestRule
      .onNodeWithTag("viewFriendsItem#${incomingProfile.uid}", useUnmergedTree = true)
      .assertExists("Friend item for ${incomingProfile.uid} does not exist in Friends list")
      .assertIsDisplayed()
  }

  /**
   * Tests rejecting (declining) a friend request.
   */
  @Test
  fun testRejectFriendRequest() {
    // Define test profiles
    val testProfile = UserProfile(
      uid = "testUser",
      name = "Test User",
      age = 28,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = listOf("profile5"),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Test User Bio"
    )

    val incomingProfile = UserProfile(
      uid = "5",
      name = "Friend Five",
      age = 24,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Bio of Friend Five"
    )

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    // Mock getUserProfile to return testProfile with incoming friend request
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile)
    }

    // Mock getFriendsProfiles to return empty list initially
    `when`(mockUserProfileRepository.getFriendsProfiles(eq(emptyList()), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
      onSuccess(emptyList())
    }

    // Mock declineFriendRequest to simulate successful rejection
    `when`(mockUserProfileRepository.declineFriendRequest(eq(testProfile.uid), eq(incomingProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    // After declining, received requests are empty
    val updatedTestProfile = testProfile.copy(recReq = emptyList())
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(updatedTestProfile)
    }

    // Set up the ViewFriendsScreen with incoming friend request
    viewFriendsTestsSetup()

    // Mocking received requests
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile.copy(recReq = listOf(incomingProfile.uid)))
    }

    // Update ViewModel state after rejection
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Assert that incomingProfile is displayed in the "Received Requests" list
    composeTestRule
      .onNodeWithTag("friendRequestItem#5", useUnmergedTree = true)
      .assertExists("Friend Request item for ${incomingProfile.uid} does not exist")
      .assertIsDisplayed()

    // Assume there's a reject button with testTag "rejectFriendButton#profile5"
    composeTestRule
      .onNodeWithTag("rejectFriendButton#${incomingProfile.uid}", useUnmergedTree = true)
      .assertExists("Reject Friend Request button does not exist for ${incomingProfile.uid}")
      .assertIsEnabled()
      .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that declineFriendRequest was called with correct parameters
    verify(mockUserProfileRepository).declineFriendRequest(eq(testProfile.uid), eq(incomingProfile.uid), any(), any())

    // Assert that incomingProfile no longer appears in the "Received Requests" list
    composeTestRule
      .onNodeWithTag("friendRequestItem#${incomingProfile.uid}", useUnmergedTree = true)
      .assertDoesNotExist()
  }

  /**
   * Tests cancelling a sent friend request.
   */
  @Test
  fun testCancelSentFriendRequest() {
    // Define test profiles
    val testProfile = UserProfile(
      uid = "testUser",
      name = "Test User",
      age = 28,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = emptyList(),
      sentReq = listOf("profile6"),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Test User Bio"
    )

    val sentProfile = UserProfile(
      uid = "profile6",
      name = "Friend Six",
      age = 26,
      statistics = UserStatistics(),
      friends = emptyList(),
      recReq = emptyList(),
      sentReq = emptyList(),
      profilePic = null,
      currentStreak = 0,
      lastLoginDate = null,
      bio = "Bio of Friend Six"
    )

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    // Mock getUserProfile to return testProfile with sent friend request
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile)
    }

    // Mock getFriendsProfiles to return empty list as there are no friends
    `when`(mockUserProfileRepository.getFriendsProfiles(eq(emptyList()), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
      onSuccess(emptyList())
    }

    // Mock cancelFriendRequest to simulate successful cancellation
    `when`(mockUserProfileRepository.cancelFriendRequest(eq(testProfile.uid), eq(sentProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<() -> Unit>(2)
      onSuccess()
    }

    // After cancellation, sent requests are empty
    val updatedTestProfile = testProfile.copy(sentReq = emptyList())
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(updatedTestProfile)
    }

    // Set up the ViewFriendsScreen with sent friend request
    viewFriendsTestsSetup()

    // Mocking sent requests
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      onSuccess(testProfile.copy(sentReq = listOf(sentProfile.uid)))
    }

    // Update ViewModel state after cancellation
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Assert that sentProfile is displayed in the "Sent Requests" list
    composeTestRule
      .onNodeWithTag("sentRequestItem#${sentProfile.uid}", useUnmergedTree = true)
      .assertExists("Sent Request item for ${sentProfile.uid} does not exist")
      .assertIsDisplayed()

    // Assume there's a cancel button with testTag "cancelFriendRequestButton#profile6"
    composeTestRule
      .onNodeWithTag("cancelFriendRequestButton#${sentProfile.uid}", useUnmergedTree = true)
      .assertExists("Cancel Friend Request button does not exist for ${sentProfile.uid}")
      .assertIsEnabled()
      .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that cancelFriendRequest was called with correct parameters
    verify(mockUserProfileRepository).cancelFriendRequest(eq(testProfile.uid), eq(sentProfile.uid), any(), any())

    // Assert that sentProfile no longer appears in the "Sent Requests" list
    composeTestRule
      .onNodeWithTag("sentRequestItem#${sentProfile.uid}", useUnmergedTree = true)
      .assertDoesNotExist()
  }

  /**
   * ============================
   * Added Tests End Here
   * ============================
   */
}
