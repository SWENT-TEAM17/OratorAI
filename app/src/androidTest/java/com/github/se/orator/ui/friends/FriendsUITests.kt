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
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
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
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.BattleRepository
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.ui.network.ChatGPTService
import kotlinx.coroutines.flow.flowOf

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

    @Mock private lateinit var mockNavigationActions: NavigationActions
    @Mock private lateinit var mockUserProfileRepository: UserProfileRepository
    @Mock private lateinit var mockBattleRepository: BattleRepository
    @Mock private lateinit var chatGPTService: ChatGPTService

    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var battleViewModel: BattleViewModel
    private lateinit var apiLinkViewModel: ApiLinkViewModel
    private lateinit var chatViewModel: ChatViewModel

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

        // Mock getRecReqProfiles
        `when`(mockUserProfileRepository.getRecReqProfiles(any(), any(), any())).then {
            it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(incomingProfile, rejectProfile))
        }

        // Mock getSentReqProfiles
        `when`(mockUserProfileRepository.getSentReqProfiles(any(), any(), any())).then {
            it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(sentProfile))
        }

        userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
        userProfileViewModel.getUserProfile(testProfile.uid)

        apiLinkViewModel = ApiLinkViewModel()
        chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

        battleViewModel = BattleViewModel(
            battleRepository = mockBattleRepository,
            userProfileViewModel = userProfileViewModel,
            navigationActions = mockNavigationActions,
            apiLinkViewModel = apiLinkViewModel,
            chatViewModel = chatViewModel
        )
    }

    /** Function used to setup a testing environment for the ViewFriendsScreen tests */
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

    @Test
    fun testFriendsListIsDisplayed() {
        viewFriendsTestsSetup()

        composeTestRule.onNodeWithTag("viewFriendsList", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewFriendsItem#1", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testFriendStreakIsDisplayed() {
        viewFriendsTestsSetup()

        composeTestRule.waitForIdle()
        val expectedStreakText = "1 day streak"
        composeTestRule
            .onNodeWithTag("friendStreak", useUnmergedTree = true)
            .assertExists("Streak text for profile1 does not exist")
            .assertIsDisplayed()
            .assertTextEquals(expectedStreakText)
    }

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

    @Test
    fun testAddFriendsScreenElementsAreDisplayed() {
        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
        composeTestRule.setContent { AddFriendsScreen(mockNavigationActions, userProfileViewModel) }

        composeTestRule.onNodeWithTag("addFriendTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("addFriendSearchField").assertIsDisplayed()
    }

    @Test
    fun testAddFriendSearch() {
        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
        composeTestRule.setContent { AddFriendsScreen(mockNavigationActions, userProfileViewModel) }

        Log.d("AddFriendSearch", userProfileViewModel.allProfiles.value.toString())
        composeTestRule.onNodeWithTag("addFriendSearchField").performTextInput("John")

        composeTestRule.onNodeWithTag("addFriendUserItem#1").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("addFriendUserItem#2").assertIsNotDisplayed()
    }

    @Test
    fun testLeaderboardScreenElementsAreDisplayed() {
        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
        composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

        composeTestRule.onNodeWithTag("leaderboardTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("leaderboardList").assertIsDisplayed()
    }

    @Test
    fun testLeaderboardItemElementsAreDisplayed() {
        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LEADERBOARD)
        composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }

        composeTestRule.onNodeWithTag("leaderboardItem#1", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("leaderboardItem#2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testPracticeModeSelector() {
        composeTestRule.setContent { PracticeModeSelector() }

        composeTestRule.onNodeWithTag("practiceModeSelector").performClick()
        composeTestRule.onNodeWithTag("practiceModeOption1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("practiceModeOption2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("practiceModeOption2").performClick()
        composeTestRule.onNodeWithTag("practiceModeSelector").assertTextEquals("Practice mode 2")
    }

    @Test
    fun testSendFriendRequest() {
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
            onSuccess(testProfile)
            null
        }.`when`(mockUserProfileRepository).getUserProfile(eq(testProfile.uid), any(), any())

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
            onSuccess(emptyList())
            null
        }.`when`(mockUserProfileRepository).getFriendsProfiles(eq(emptyList()), any(), any())

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(0)
            onSuccess(listOf(profile3))
            null
        }.`when`(mockUserProfileRepository).getAllUserProfiles(any(), any())

        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<() -> Unit>(2)
            onSuccess()
            null
        }.`when`(mockUserProfileRepository)
            .sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

        val updatedTestProfile = testProfile.copy(sentReq = listOf(profile3.uid))
        doAnswer { invocation ->
            val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
            onSuccess(updatedTestProfile)
            null
        }.`when`(mockUserProfileRepository).getUserProfile(eq(testProfile.uid), any(), any())

        composeTestRule.setContent {
            AddFriendsScreen(
                navigationActions = mockNavigationActions, userProfileViewModel = userProfileViewModel)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("addFriendSearchField").performTextInput("Friend Three")
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("*").printToLog("AvailableTestTagsAfterSearch")
        composeTestRule
            .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
            .assertExists("Send Friend Request button does not exist for 3")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        verify(mockUserProfileRepository)
            .sendFriendRequest(eq(testProfile.uid), eq(profile3.uid), any(), any())

        val sentRequestsList =
            composeTestRule.onAllNodesWithTag("sentFriendRequestsList", useUnmergedTree = true)
        if (sentRequestsList.fetchSemanticsNodes().isEmpty()) {
            composeTestRule
                .onNodeWithTag("toggleSentRequestsButton", useUnmergedTree = true)
                .assertExists("Toggle Sent Requests button does not exist")
                .assertIsEnabled()
                .performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule
            .onNodeWithTag("sentFriendRequestItem#3", useUnmergedTree = true)
            .assertExists("Sent Request item for 3 does not exist")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("sendFriendRequestButton#3", useUnmergedTree = true)
            .assertIsNotDisplayed()
    }

    @Test
    fun testAcceptFriendRequest() {
        `when`(
            mockUserProfileRepository.acceptFriendRequest(
                eq(testProfile.uid), eq(incomingProfile.uid), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<() -> Unit>(2)
                onSuccess()
            }

        val updatedTestProfile =
            testProfile.copy(friends = listOf(incomingProfile.uid), recReq = emptyList())
        val updatedIncomingProfile = incomingProfile.copy(friends = listOf(testProfile.uid))

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

        viewFriendsTestsSetup()
        userProfileViewModel.getUserProfile(testProfile.uid)

        composeTestRule
            .onNodeWithTag("friendRequestItem#${incomingProfile.uid}", useUnmergedTree = true)
            .assertExists("Friend Request item for ${incomingProfile.uid} does not exist")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("acceptFriendButton#${incomingProfile.uid}", useUnmergedTree = true)
            .assertExists("Accept Friend Request button does not exist for ${incomingProfile.uid}")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        verify(mockUserProfileRepository)
            .acceptFriendRequest(eq(testProfile.uid), eq(incomingProfile.uid), any(), any())

        composeTestRule
            .onNodeWithTag("friendRequestItem#${incomingProfile.uid}", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testRejectFriendRequest() {
        `when`(
            mockUserProfileRepository.declineFriendRequest(
                eq(testProfile.uid), eq(rejectProfile.uid), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<() -> Unit>(2)
                onSuccess()
            }

        val updatedTestProfile = testProfile.copy(recReq = testProfile.recReq - rejectProfile.uid)
        `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
                onSuccess(updatedTestProfile)
            }

        viewFriendsTestsSetup()
        userProfileViewModel.getUserProfile(testProfile.uid)

        composeTestRule
            .onNodeWithTag("friendRequestItem#${rejectProfile.uid}", useUnmergedTree = true)
            .assertExists("Friend Request item for ${rejectProfile.uid} does not exist")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("declineFriendButton#${rejectProfile.uid}", useUnmergedTree = true)
            .assertExists("Reject Friend Request button does not exist for ${rejectProfile.uid}")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()

        verify(mockUserProfileRepository)
            .declineFriendRequest(eq(testProfile.uid), eq(rejectProfile.uid), any(), any())

        composeTestRule
            .onNodeWithTag("friendRequestItem#${rejectProfile.uid}", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testCancelSentFriendRequest() {
        `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
                onSuccess(testProfile.copy(sentReq = listOf(sentProfile.uid)))
            }

        `when`(
            mockUserProfileRepository.cancelFriendRequest(
                eq(testProfile.uid), eq(sentProfile.uid), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<() -> Unit>(2)
                onSuccess()
            }

        val updatedTestProfile = testProfile.copy(sentReq = emptyList())
        `when`(mockUserProfileRepository.getUserProfile(eq(testProfile.uid), any(), any()))
            .thenAnswer { invocation ->
                val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
                onSuccess(updatedTestProfile)
            }

        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.ADD_FRIENDS)
        composeTestRule.setContent {
            AddFriendsScreen(
                navigationActions = mockNavigationActions, userProfileViewModel = userProfileViewModel)
        }

        userProfileViewModel.getUserProfile(testProfile.uid)

        val sentRequestsList =
            composeTestRule.onAllNodesWithTag("sentFriendRequestsList", useUnmergedTree = true)
        if (sentRequestsList.fetchSemanticsNodes().isEmpty()) {
            composeTestRule
                .onNodeWithTag("toggleSentRequestsButton", useUnmergedTree = true)
                .assertExists("Toggle Sent Requests button does not exist")
                .assertIsEnabled()
                .performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("sentFriendRequestItem#${sentProfile.uid}", useUnmergedTree = true)
            .assertExists("Sent Friend Request item for ${sentProfile.uid} should be displayed")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("cancelFriendRequestButton#${sentProfile.uid}", useUnmergedTree = true)
            .assertExists("Cancel Friend Request button does not exist for ${sentProfile.uid}")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()

        verify(mockUserProfileRepository)
            .cancelFriendRequest(eq(testProfile.uid), eq(sentProfile.uid), any(), any())

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("sentFriendRequestItem#${sentProfile.uid}", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testNoPendingBattlesIconNotDisplayed() {
        // Set up the mocks to return no pending battles
        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<SpeechBattle>) -> Unit>(1)
            callback(emptyList())
            null
        }.`when`(mockBattleRepository).getPendingBattlesForUser(any(), any(), any())

        // Render the screen with no pending battles
        composeTestRule.setContent {
            ViewFriendsScreen(
                navigationActions = mockNavigationActions,
                userProfileViewModel = userProfileViewModel,
                battleViewModel = battleViewModel
            )
        }

        composeTestRule.waitForIdle()

        // Since there are no pending battles, we expect no pending battle icons
        composeTestRule.onNodeWithTag("pendingBattleIcon#3", useUnmergedTree = true)
            .assertDoesNotExist()

    }

    @Test
    fun testAcceptPendingBattle() {
        val mockBattles = listOf(
            SpeechBattle(
                battleId = "battle1",
                challenger = "1",
                opponent = "testUser",
                status = BattleStatus.PENDING,
                context = InterviewContext("testPosition", "testCompany", "testType", "testExperience", "testDescription", "testFocusArea")
            )
        )

        // Mock getPendingBattlesForUser
        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<SpeechBattle>) -> Unit>(1)
            callback(mockBattles)
            null
        }.`when`(mockBattleRepository).getPendingBattlesForUser(any(), any(), any())

        // Mock getBattleById to return the battle so that friendUid is not null
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
                battleViewModel = battleViewModel
            )
        }

        composeTestRule.waitForIdle()

        // Verify that the pending battle icon is displayed
        composeTestRule.onNodeWithTag("pendingBattleIcon#1", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        // Verify that updateBattleStatus was called
        verify(mockBattleRepository)
            .updateBattleStatus(eq("battle1"), eq(BattleStatus.IN_PROGRESS), any())
    }

    @Test
    fun clickingOnFriendOpensChatScreen() {

        composeTestRule.setContent {
            ViewFriendsScreen(
                navigationActions = mockNavigationActions,
                userProfileViewModel = userProfileViewModel,
                battleViewModel = battleViewModel
            )
        }

        composeTestRule.waitForIdle()

        // Click on friend profile
        composeTestRule.onNodeWithTag("viewFriendsItem#2").performClick()

        // Verify that we navigate to the send battle screen
        verify(mockNavigationActions).navigateToSendBattleScreen(eq("2"))
    }

}
