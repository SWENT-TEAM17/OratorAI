package com.github.se.orator.ui.friends

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
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
import com.github.se.orator.utils.formatDate
import com.github.se.orator.utils.getCurrentDate
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
  private val profile1 =
      UserProfile(
          "1",
          "John Doe",
          99,
          statistics = UserStatistics(),
          currentStreak = 1,
          lastLoginDate = formatDate(getCurrentDate()))
  private val profile2 = UserProfile("2", "Jane Doe", 100, statistics = UserStatistics())

  val profile3 =
      UserProfile(
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
          bio = "Bio of Friend Three")

  // Define incomingProfile at the class level
  private val incomingProfile =
      UserProfile(
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
          bio = "Bio of Friend Four")

  private val rejectProfile =
      UserProfile(
          uid = "5",
          name = "Friend Five",
          age = 24,
          statistics = UserStatistics(),
          friends = emptyList(),
          recReq = emptyList(),
          sentReq = listOf("testUser"),
          profilePic = null,
          currentStreak = 0,
          lastLoginDate = null,
          bio = "Bio of Friend Five")

  val sentProfile =
      UserProfile(
          uid = "profile6",
          name = "Friend Six",
          age = 26,
          statistics = UserStatistics(),
          friends = emptyList(),
          recReq = listOf("testUser"),
          sentReq = emptyList(),
          profilePic = null,
          currentStreak = 0,
          lastLoginDate = null,
          bio = "Bio of Friend Six")

  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel

  // Adjust testProfile to have recReq = listOf("profile4")
  private val testProfile =
      UserProfile(
          uid = "testUser",
          name = "Test User",
          age = 28,
          statistics = UserStatistics(),
          friends = emptyList(),
          recReq = listOf("profile4", "5"),
          sentReq = listOf("profile6"),
          profilePic = null,
          currentStreak = 0,
          lastLoginDate = null,
          bio = "Test User Bio")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    // Mock getUserProfile to return testProfile
    `when`(mockUserProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<(UserProfile?) -> Unit>(1)(testProfile)
    }

    // Mock getFriendsProfiles
    `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2))
    }

    // Mock getAllUserProfiles
    `when`(mockUserProfileRepository.getAllUserProfiles(any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(0)(listOf(profile1, profile2, profile3))
    }

    // Mock getRecReqProfiles to return the desired profiles
    `when`(mockUserProfileRepository.getRecReqProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(incomingProfile, rejectProfile))
    }

    // Mock getSentReqProfiles to return the desired profiles
    `when`(mockUserProfileRepository.getSentReqProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(sentProfile))
    }

    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    userProfileViewModel.getUserProfile(testProfile.uid)
  }

  /** Function used to setup a testing environment for the ViewFriendsScreen tests */
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

  @Test
  fun testFriendStreakIsDisplayed() {
    // Set up the test environment
    viewFriendsTestsSetup()

    // Ensure that profile1 is part of the friends list
    // This is already handled in the setUp() method where profile1 and profile2 are returned as
    // friends

    // Wait for the UI to render the friends list
    composeTestRule.waitForIdle()

    // Define the expected streak text for profile1
    val expectedStreakText = "1 day streak"

    // Locate the streak text for profile1 using the unique test tag
    composeTestRule
        .onNodeWithTag("friendStreak", useUnmergedTree = true)
        .assertExists("Streak text for profile1 does not exist")
        .assertIsDisplayed()
        .assertTextEquals(expectedStreakText)
  }

  /** Tests that navigation to Add Friend and Leaderboard screens works correctly. */
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

  /** Tests that the Add Friends screen elements are displayed correctly. */
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

  /** Tests the Practice Mode Selector functionality. */
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
   * Tests that deleting a friend calls the repository's deleteFriend method and updates the UI
   * accordingly.
   */
  @Test
  fun deleteFriendButtonCallsRepositoryUpdateProfileMethod() {
    // Define test profiles
    val profile1 =
        UserProfile(
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
            bio = "Bio of Friend One")

    val profile2 =
        UserProfile(
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
            bio = "Bio of Friend Two")

    val testProfile =
        UserProfile(
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
            bio = "Test User Bio")

    // Mock getCurrentUserUid to return testProfile.uid
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testProfile.uid)

    // Mock getUserProfile to return testProfile
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(testProfile)
        }

    // Mock getFriendsProfiles to return profile1 and profile2
    `when`(
            mockUserProfileRepository.getFriendsProfiles(
                eq(listOf(profile1.uid, profile2.uid)), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
          onSuccess(listOf(profile1, profile2))
        }

    // Mock deleteFriend to simulate successful deletion
    `when`(
            mockUserProfileRepository.deleteFriend(
                eq(testProfile.uid), eq(profile1.uid), any(), any()))
        .thenAnswer { invocation ->
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
    verify(mockUserProfileRepository)
        .deleteFriend(eq(testProfile.uid), eq(profile1.uid), any(), any())

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

  /** Tests sending a friend request. */
  @Test
  fun testSendFriendRequest() {
    // Mock getUserProfile to return testProfile initially with no sent requests
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(testProfile)
          null
        }
        .`when`(mockUserProfileRepository)
        .getUserProfile(eq(testProfile.uid), any(), any())

    // Mock getFriendsProfiles to return an empty list as there are no friends initially
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
          onSuccess(emptyList())
          null
        }
        .`when`(mockUserProfileRepository)
        .getFriendsProfiles(eq(emptyList()), any(), any())

    // Mock getAllUserProfiles to include profile3
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(0)
          onSuccess(listOf(profile3)) // Include profile3 in allProfiles
          null
        }
        .`when`(mockUserProfileRepository)
        .getAllUserProfiles(any(), any())

    // Mock sendFriendRequest to simulate successful request
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess()
          null
        }
        .`when`(mockUserProfileRepository)
        .sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

    // After sending, sent requests include profile3
    val updatedTestProfile = testProfile.copy(sentReq = listOf(profile3.uid))
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(updatedTestProfile)
          null
        }
        .`when`(mockUserProfileRepository)
        .getUserProfile(eq(testProfile.uid), any(), any())

    // Set up the AddFriendsScreen composable
    composeTestRule.setContent {
      AddFriendsScreen(
          navigationActions = mockNavigationActions, userProfileViewModel = userProfileViewModel)
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
    verify(mockUserProfileRepository)
        .sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())
      // Step 6: Check if the Sent Requests list is already expanded
      val sentRequestsList = composeTestRule.onAllNodesWithTag("sentFriendRequestsList", useUnmergedTree = true)
      if (sentRequestsList.fetchSemanticsNodes().isEmpty()) {
          // If the list is not displayed, expand the Sent Friend Requests section
          composeTestRule
              .onNodeWithTag("toggleSentRequestsButton", useUnmergedTree = true)
              .assertExists("Toggle Sent Requests button does not exist")
              .assertIsEnabled()
              .performClick()

          // Wait for the UI to render the expanded list
          composeTestRule.waitForIdle()
      }


      // **Assert that profile3 now appears in the "Sent Requests" list**
    composeTestRule
        .onNodeWithTag("sentFriendRequestItem#3", useUnmergedTree = true)
        .assertExists("Sent Request item for 3 does not exist")
        .assertIsDisplayed()

    // **Optionally, assert that the send button is disabled or shows "Request Sent"**
    composeTestRule
        .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  /** Tests accepting a friend request. */
  @Test
  fun testAcceptFriendRequest() {
    // No need to redefine testProfile or incomingProfile here

    // Mock acceptFriendRequest to simulate successful acceptance
    `when`(
            mockUserProfileRepository.acceptFriendRequest(
                eq(testProfile.uid), eq(incomingProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess()
        }

    // After accepting, update testProfile
    val updatedTestProfile =
        testProfile.copy(friends = listOf(incomingProfile.uid), recReq = emptyList())
    // Update incomingProfile
    val updatedIncomingProfile = incomingProfile.copy(friends = listOf(testProfile.uid))

    // Mock getUserProfile to return updated profiles after acceptance
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(updatedTestProfile)
        }
    `when`(mockUserProfileRepository.getUserProfile(eq(incomingProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(updatedIncomingProfile)
        }

    // Set up the ViewFriendsScreen
    viewFriendsTestsSetup()

    // Update ViewModel state after accepting
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Assert that incomingProfile is displayed in the "Received Requests" list
    composeTestRule
        .onNodeWithTag("friendRequestItem#${incomingProfile.uid}", useUnmergedTree = true)
        .assertExists("Friend Request item for ${incomingProfile.uid} does not exist")
        .assertIsDisplayed()

    // Click the accept button
    composeTestRule
        .onNodeWithTag("acceptFriendButton#${incomingProfile.uid}", useUnmergedTree = true)
        .assertExists("Accept Friend Request button does not exist for ${incomingProfile.uid}")
        .assertIsEnabled()
        .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that acceptFriendRequest was called with correct parameters
    verify(mockUserProfileRepository)
        .acceptFriendRequest(eq(testProfile.uid), eq(incomingProfile.uid), any(), any())

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
  /** Tests rejecting (declining) a friend request. */
  @Test
  fun testRejectFriendRequest() {
    // Mock declineFriendRequest to simulate successful rejection
    `when`(
            mockUserProfileRepository.declineFriendRequest(
                eq(testProfile.uid), eq(rejectProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess()
        }

    // After declining, update testProfile to remove the rejected UID from recReq
    val updatedTestProfile = testProfile.copy(recReq = testProfile.recReq - rejectProfile.uid)

    // Mock getUserProfile to return the updated profile after rejection
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(updatedTestProfile)
        }

    // Set up the ViewFriendsScreen
    viewFriendsTestsSetup()

    // Update ViewModel state after rejection
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Assert that rejectProfile is displayed in the "Received Requests" list
    composeTestRule
        .onNodeWithTag("friendRequestItem#${rejectProfile.uid}", useUnmergedTree = true)
        .assertExists("Friend Request item for ${rejectProfile.uid} does not exist")
        .assertIsDisplayed()

    // Click the reject button
    composeTestRule
        .onNodeWithTag("declineFriendButton#${rejectProfile.uid}", useUnmergedTree = true)
        .assertExists("Reject Friend Request button does not exist for ${rejectProfile.uid}")
        .assertIsEnabled()
        .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that declineFriendRequest was called with correct parameters
    verify(mockUserProfileRepository)
        .declineFriendRequest(eq(testProfile.uid), eq(rejectProfile.uid), any(), any())

    // Assert that rejectProfile no longer appears in the "Received Requests" list
    composeTestRule
        .onNodeWithTag("friendRequestItem#${rejectProfile.uid}", useUnmergedTree = true)
        .assertDoesNotExist()
  }

  /** Tests cancelling a sent friend request. */
  /** Tests cancelling a sent friend request. */
  @Test
  fun testCancelSentFriendRequest() {
    // Step 1: Mock getUserProfile to return testProfile with sentFriendRequest (sentProfile)
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(testProfile.copy(sentReq = listOf(sentProfile.uid)))
        }

    // Step 2: Mock cancelFriendRequest to simulate successful cancellation
    `when`(
            mockUserProfileRepository.cancelFriendRequest(
                eq(testProfile.uid), eq(sentProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess()
        }

    // Step 3: Mock getUserProfile to return updatedTestProfile after cancellation (sentReq is
    // empty)
    val updatedTestProfile = testProfile.copy(sentReq = emptyList())
    `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(updatedTestProfile)
        }

    // Step 4: Set up the AddFriendsScreen composable
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
    composeTestRule.setContent {
      AddFriendsScreen(
          navigationActions = mockNavigationActions, userProfileViewModel = userProfileViewModel)
    }

    // Step 5: Trigger the initial profile fetch with sentFriendRequest
    userProfileViewModel.getUserProfile(testProfile.uid)

      // Step 6: Check if the Sent Requests list is already expanded
      val sentRequestsList = composeTestRule.onAllNodesWithTag("sentFriendRequestsList", useUnmergedTree = true)
      if (sentRequestsList.fetchSemanticsNodes().isEmpty()) {
          // If the list is not displayed, expand the Sent Friend Requests section
          composeTestRule
              .onNodeWithTag("toggleSentRequestsButton", useUnmergedTree = true)
              .assertExists("Toggle Sent Requests button does not exist")
              .assertIsEnabled()
              .performClick()

          // Wait for the UI to render the expanded list
          composeTestRule.waitForIdle()
      }

    composeTestRule.waitForIdle()

    // Step 7: Assert that sentProfile is displayed in the "Sent Requests" list
    composeTestRule
        .onNodeWithTag("sentFriendRequestItem#${sentProfile.uid}", useUnmergedTree = true)
        .assertExists("Sent Friend Request item for ${sentProfile.uid} should be displayed")
        .assertIsDisplayed()

    // Step 8: Click the cancel friend request button
    composeTestRule
        .onNodeWithTag("cancelFriendRequestButton#${sentProfile.uid}", useUnmergedTree = true)
        .assertExists("Cancel Friend Request button does not exist for ${sentProfile.uid}")
        .assertIsEnabled()
        .performClick()

    // Wait for the UI to process the cancellation
    composeTestRule.waitForIdle()

    // Step 9: Verify that cancelFriendRequest was called with correct parameters
    verify(mockUserProfileRepository)
        .cancelFriendRequest(eq(testProfile.uid), eq(sentProfile.uid), any(), any())

    composeTestRule.waitForIdle()

    // Step 11: Assert that sentProfile no longer appears in the "Sent Requests" list
    composeTestRule
        .onNodeWithTag("sentFriendRequestItem#${sentProfile.uid}", useUnmergedTree = true)
        .assertDoesNotExist()
  }
  /**
   * ============================
   * Added Tests End Here
   * ============================
   */
}
