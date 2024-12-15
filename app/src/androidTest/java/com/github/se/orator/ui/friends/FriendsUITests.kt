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
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.BattleRepository
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
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

  // ============================
  // Mock User Profiles
  // ============================

  // Profile of John Doe with a current streak and last login date
  private val profile1 =
      UserProfile(
          "1",
          "John Doe",
          99,
          statistics = UserStatistics(),
          currentStreak = 1,
          lastLoginDate = formatDate(getCurrentDate()))

  // Profile of Jane Doe with maximum statistics
  private val profile2 = UserProfile("2", "Jane Doe", 100, statistics = UserStatistics())

  // Profile of Friend Three with detailed information
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

  // Profile of Friend Four who has sent a friend request
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

  // Profile of Friend Five who is to be rejected
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

  // Profile of Friend Six who has received a friend request
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

  // Profile of Friend Seven who is already a friend
  val profile7 =
      UserProfile(
          uid = "7",
          name = "Friend Seven",
          age = 25,
          statistics = UserStatistics(),
          friends = listOf("testUser"),
          recReq = emptyList(),
          sentReq = emptyList(),
          profilePic = null,
          currentStreak = 0,
          lastLoginDate = null,
          bio = "Bio of Friend Seven")

  // ============================
  // Mock Dependencies
  // ============================

  @Mock private lateinit var mockNavigationActions: NavigationActions

  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository

  @Mock private lateinit var mockBattleRepository: BattleRepository

  @Mock private lateinit var chatGPTService: ChatGPTService

  // ============================
  // ViewModels
  // ============================

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var battleViewModel: BattleViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  // ============================
  // Test User Profile
  // ============================

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

    // Mock getFriendsProfiles to return profile1 and profile2 as friends
    `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2))
    }

    // Mock getAllUserProfiles to include profile1, profile2, and profile3
    `when`(mockUserProfileRepository.getAllUserProfiles(any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(0)(listOf(profile1, profile2, profile3))
    }

    // Mock getRecReqProfiles to return incomingProfile and rejectProfile
    `when`(mockUserProfileRepository.getRecReqProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(incomingProfile, rejectProfile))
    }

    // Mock getSentReqProfiles to return sentProfile
    `when`(mockUserProfileRepository.getSentReqProfiles(any(), any(), any())).then {
      it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(sentProfile))
    }

    // Initialize UserProfileViewModel with the mocked repository
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    userProfileViewModel.getUserProfile(testProfile.uid)

    // Initialize other ViewModels
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    // Initialize BattleViewModel with the mocked dependencies
    battleViewModel =
        BattleViewModel(
            battleRepository = mockBattleRepository,
            userProfileViewModel = userProfileViewModel,
            navigationActions = mockNavigationActions,
            apiLinkViewModel = apiLinkViewModel,
            chatViewModel = chatViewModel)
  }

  /**
   * Function used to set up a testing environment for the ViewFriendsScreen tests. It sets the
   * current route to FRIENDS and initializes the ViewFriendsScreen composable.
   */
  private fun viewFriendsTestsSetup() {
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.FRIENDS)
    composeTestRule.setContent {
      ViewFriendsScreen(mockNavigationActions, userProfileViewModel, battleViewModel)
    }
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
   * Tests that the friend's streak is displayed correctly. It ensures that the streak text for
   * profile1 is present and matches the expected text.
   */
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

  /**
   * Tests that navigation to Add Friend and Leaderboard screens works correctly. It verifies that
   * clicking the respective buttons navigates to the correct screens.
   */
  @Test
  fun testCanGoToAddFriendAndLeaderboardScreens() {
    viewFriendsTestsSetup()

    // Click the menu button to open the drawer
    composeTestRule.onNodeWithTag("viewFriendsMenuButton").performClick()

    // Click the Add Friend button and verify navigation
    composeTestRule
        .onNodeWithTag("viewFriendsAddFriendButton", useUnmergedTree = true)
        .performClick()
    composeTestRule.waitForIdle()
    verify(mockNavigationActions).navigateTo(eq(Screen.ADD_FRIENDS))

    // Click the Leaderboard button and verify navigation
    composeTestRule
        .onNodeWithTag("viewFriendsLeaderboardButton", useUnmergedTree = true)
        .performClick()
    composeTestRule.waitForIdle()
    verify(mockNavigationActions).navigateTo(eq(Screen.LEADERBOARD))
  }

  /**
   * Tests that the search functionality works correctly on the View Friends screen. It verifies
   * that the search field exists and is displayed, can be clicked, and text can be inputted. After
   * inputting text, it checks that the first friend item is displayed and the second is not.
   */
  @Test
  fun testCanSearchForFriends() {
    viewFriendsTestsSetup()

    // Perform a click on the search field and input "John"
    composeTestRule.onNodeWithTag("viewFriendsSearch").performClick()
    composeTestRule.onNodeWithTag("viewFriendsSearch").performTextInput("John")

    // Verify that the first friend item is displayed and the second is not
    composeTestRule.onNodeWithTag("viewFriendsItem#1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("viewFriendsItem#2", useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  /**
   * Tests that the Add Friends screen elements are displayed correctly. It verifies that the title
   * and search field are present on the Add Friends screen.
   */
  @Test
  fun testAddFriendsScreenElementsAreDisplayed() {
    // Set the current route to ADD_FRIENDS and render the AddFriendsScreen composable
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
    composeTestRule.setContent { AddFriendsScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the add friend screen elements are displayed
    composeTestRule.onNodeWithTag("addFriendTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("addFriendSearchField").assertIsDisplayed()
  }

  /**
   * Tests that the search functionality works correctly on the Add Friends screen. After inputting
   * text, it checks that the first and second friend items are not displayed because the user
   * searching should not be displayed and the second is not because it's not the right name.
   */
  @Test
  fun testAddFriendSearch() {
    // Set the current route to ADD_FRIENDS and render the AddFriendsScreen composable
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
    composeTestRule.setContent { AddFriendsScreen(mockNavigationActions, userProfileViewModel) }

    // Log the current profiles for debugging
    Log.d("AddFriendSearch", userProfileViewModel.allProfiles.value.toString())

    // Perform a text input search for "John"
    composeTestRule.onNodeWithTag("addFriendSearchField").performTextInput("John")

    // Verify that no matching user items are displayed
    composeTestRule.onNodeWithTag("addFriendUserItem#1").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("addFriendUserItem#2").assertIsNotDisplayed()
  }

  /**
   * Tests that the leaderboard screen elements are displayed correctly. It verifies that the title
   * and leaderboard list are present on the Leaderboard screen.
   */
  @Test
  fun testLeaderboardScreenElementsAreDisplayed() {
    // Set the current route to LEADERBOARD and render the LeaderboardScreen composable
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the leaderboard screen elements are displayed
    composeTestRule.onNodeWithTag("leaderboardTitle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaderboardList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("buttonRowLeaderboard")
  }

  /**
   * Tests that the leaderboard item elements are displayed correctly. It verifies that individual
   * leaderboard items are present and displayed.
   */
  @Test
  fun testLeaderboardItemElementsAreDisplayed() {
    // Set the current route to LEADERBOARD and render the LeaderboardScreen composable
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

    // Check if the leaderboard item elements are displayed
    composeTestRule.onNodeWithTag("leaderboardItem#1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("leaderboardItem#2", useUnmergedTree = true).assertIsDisplayed()
  }

  /**
   * Tests the Practice Mode Selector functionality. It verifies that the dropdown menu opens,
   * displays options, and allows selecting an option.
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
    composeTestRule.onNodeWithTag("practiceModeOption3").assertIsDisplayed()

    // Select "Practice mode 2"
    composeTestRule.onNodeWithTag("practiceModeOption2").performClick()

    // Verify that the prefix text is displayed
    composeTestRule.onNodeWithTag("practiceModeSelector").assertTextEquals("Mode : Speech")
  }

  /** Tests the Rank Metric Selector functionality. */
  @Test
  fun testRankMetricSelector() {
    // Set up the content with the PracticeModeSelector composable
    composeTestRule.setContent { RankMetricSelector() }

    // Open the dropdown menu
    composeTestRule.onNodeWithTag("rankMetricSelector").performClick()

    // Verify the dropdown menu options are displayed
    composeTestRule.onNodeWithTag("rankMetricOption1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rankMetricOption2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rankMetricOption3").assertIsDisplayed()

    // Select "Practice mode 2"
    composeTestRule.onNodeWithTag("rankMetricOption2").performClick()

    // Verify that the prefix text is displayed
    composeTestRule.onNodeWithTag("rankMetricSelector").assertTextEquals("Metric : Success")
  }

  /**
   * Tests sending a friend request. It verifies that sending a friend request updates the UI
   * accordingly and the repository is called.
   */
  @Test
  fun testSendFriendRequest() {
    // Mock getUserProfile to return testProfile initially with the current sent requests
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

    // Mock sendFriendRequest to simulate a successful request
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess()
          null
        }
        .`when`(mockUserProfileRepository)
        .sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

    // After sending, update the testProfile to include profile3 in sent requests
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

    // Input a search query that matches profile3's name
    composeTestRule
        .onNodeWithTag("addFriendSearchField")
        .assertExists("No search bar is being displayed.")
        .performTextInput("Friend Three") // Adjust the query to match profile3.name

    // Wait for the UI to process the search
    composeTestRule.waitForIdle()

    // Log available testTags to verify presence
    composeTestRule.onAllNodesWithTag("*").printToLog("AvailableTestTagsAfterSearch")

    // Locate and click the "Send Friend Request" button with the correct testTag
    composeTestRule
        .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
        .assertExists("Send Friend Request button does not exist for 3")
        .assertIsEnabled()
        .performClick()

    // Wait for the UI to process the state change
    composeTestRule.waitForIdle()

    // Verify that sendFriendRequest was called with correct parameters
    verify(mockUserProfileRepository)
        .sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

    // Check if the Sent Requests list is already expanded
    val sentRequestsList =
        composeTestRule.onAllNodesWithTag("sentFriendRequestsList", useUnmergedTree = true)
    if (sentRequestsList.fetchSemanticsNodes().isEmpty()) {
      // If the list is not displayed, expand the Sent Friend Requests section
      composeTestRule
          .onNodeWithTag("toggleSentRequestsButton", useUnmergedTree = true)
          .assertExists("Toggle Sent Requests button does not exist")
          .assertIsEnabled()
          .performClick()
      composeTestRule.waitForIdle()
    }

    // Assert that profile3 now appears in the "Sent Requests" list
    composeTestRule
        .onNodeWithTag("sentFriendRequestItem#3", useUnmergedTree = true)
        .assertExists("Sent Request item for 3 does not exist")
        .assertIsDisplayed()

    // Optionally, assert that the send button is disabled or shows "Request Sent"
    composeTestRule
        .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
        .assertIsNotDisplayed()
  }

  /**
   * Tests accepting a friend request. It verifies that accepting a request updates the UI and
   * repository correctly.
   */
  @Test
  fun testAcceptFriendRequest() {
    // Mock acceptFriendRequest to simulate successful acceptance
    `when`(
            mockUserProfileRepository.acceptFriendRequest(
                eq(testProfile.uid), eq(incomingProfile.uid), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(2)
          onSuccess()
        }

    // After accepting, update testProfile and incomingProfile
    val updatedTestProfile =
        testProfile.copy(friends = listOf(incomingProfile.uid), recReq = emptyList())
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

    // Set up the ViewFriendsScreen composable
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
  }

  /**
   * Tests rejecting (declining) a friend request. It verifies that rejecting a request updates the
   * UI and repository correctly.
   */
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

    // Set up the ViewFriendsScreen composable
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

  /**
   * Tests cancelling a sent friend request. It verifies that cancelling a request updates the UI
   * and repository correctly.
   */
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
    val sentRequestsList =
        composeTestRule.onAllNodesWithTag("sentFriendRequestsList", useUnmergedTree = true)
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

    // Wait for the UI to update
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

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Step 11: Assert that sentProfile no longer appears in the "Sent Requests" list
    composeTestRule
        .onNodeWithTag("sentFriendRequestItem#${sentProfile.uid}", useUnmergedTree = true)
        .assertDoesNotExist()
  }

  /**
   * Tests that no pending battles icon is displayed when there are no pending battles. It verifies
   * that the pending battle icon does not exist in the UI.
   */
  @Test
  fun testNoPendingBattlesIconNotDisplayed() {
    // Set up the mocks to return no pending battles
    doAnswer { invocation ->
          val callback = invocation.getArgument<(List<SpeechBattle>) -> Unit>(1)
          callback(emptyList())
          null
        }
        .`when`(mockBattleRepository)
        .getPendingBattlesForUser(any(), any(), any())

    // Render the screen with no pending battles
    composeTestRule.setContent {
      ViewFriendsScreen(
          navigationActions = mockNavigationActions,
          userProfileViewModel = userProfileViewModel,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to render
    composeTestRule.waitForIdle()

    // Since there are no pending battles, we expect no pending battle icons
    composeTestRule
        .onNodeWithTag("pendingBattleIcon#3", useUnmergedTree = true)
        .assertDoesNotExist()
  }

  /**
   * Tests accepting a pending battle. It verifies that accepting a battle updates the battle status
   * and repository correctly.
   */
  @Test
  fun testAcceptPendingBattle() {
    // Create a mock pending battle
    val mockBattles =
        listOf(
            SpeechBattle(
                battleId = "battle1",
                challenger = "1",
                opponent = "testUser",
                status = BattleStatus.PENDING,
                context =
                    InterviewContext(
                        "testPosition",
                        "testCompany",
                        "testType",
                        "testExperience",
                        "testDescription",
                        "testFocusArea")))

    // Mock getPendingBattlesForUser to return the mockBattles
    doAnswer { invocation ->
          val callback = invocation.getArgument<(List<SpeechBattle>) -> Unit>(1)
          callback(mockBattles)
          null
        }
        .`when`(mockBattleRepository)
        .getPendingBattlesForUser(any(), any(), any())

    // Mock getBattleById to return the specific battle
    `when`(mockBattleRepository.getBattleById(eq("battle1"), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      callback.invoke(mockBattles.first())
      null
    }

    // Now render the screen
    composeTestRule.setContent {
      ViewFriendsScreen(
          navigationActions = mockNavigationActions,
          userProfileViewModel = userProfileViewModel,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to render
    composeTestRule.waitForIdle()

    // Verify that the pending battle icon is displayed
    composeTestRule
        .onNodeWithTag("pendingBattleIcon#1", useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()

    // Verify that updateBattleStatus was called to change the status to IN_PROGRESS
    verify(mockBattleRepository)
        .updateBattleStatus(eq("battle1"), eq(BattleStatus.IN_PROGRESS), any())
  }

  /**
   * Tests that clicking on a friend opens the chat screen. It verifies that clicking on a friend
   * navigates to the send battle screen with the correct friend UID.
   */
  @Test
  fun clickingOnFriendOpensChatScreen() {
    // Render the ViewFriendsScreen composable
    composeTestRule.setContent {
      ViewFriendsScreen(
          navigationActions = mockNavigationActions,
          userProfileViewModel = userProfileViewModel,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to render
    composeTestRule.waitForIdle()

    // Click on the friend profile with UID "2"
    composeTestRule.onNodeWithTag("viewFriendsItem#2").performClick()

    // Verify that we navigate to the send battle screen with the correct UID
    verify(mockNavigationActions).navigateToSendBattleScreen(eq("2"))
  }
}
