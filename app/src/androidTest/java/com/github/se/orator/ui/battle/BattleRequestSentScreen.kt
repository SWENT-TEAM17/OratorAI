package com.github.se.orator.ui.battle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.network.ChatGPTService
import com.google.firebase.firestore.ListenerRegistration
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class BattleRequestSentScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockNavigationActions: NavigationActions

  @Mock private lateinit var mockBattleRepository: BattleRepository

  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository

  @Mock private lateinit var chatGPTService: ChatGPTService

  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var battleViewModel: BattleViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  private var battleUpdateCallback: ((SpeechBattle?) -> Unit)? = null

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock getCurrentUserUid to return "testUser"
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn("testUser")

    // Initialize ViewModels
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    // Mock getBattleById to return a PENDING battle initially
    `when`(mockBattleRepository.getBattleById(eq("battleId"), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      val battle =
          SpeechBattle(
              battleId = "battleId",
              challenger = "friendUid",
              opponent = "testUser",
              status = BattleStatus.PENDING,
              context =
                  InterviewContext(
                      "testPosition",
                      "testCompany",
                      "testType",
                      "testExperience",
                      "testDescription",
                      "testFocusArea"))
      callback(battle)
      null
    }

    // Mock listenToBattleUpdates to capture callback and return PENDING initially
    `when`(mockBattleRepository.listenToBattleUpdates(eq("battleId"), any())).thenAnswer {
        invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      battleUpdateCallback = callback
      callback(
          SpeechBattle(
              battleId = "battleId",
              challenger = "friendUid",
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
      mockListenerRegistration
    }

    // Mock updateBattleStatus to simulate success and trigger battle update to IN_PROGRESS
    `when`(
            mockBattleRepository.updateBattleStatus(
                eq("battleId"), eq(BattleStatus.IN_PROGRESS), any()))
        .thenAnswer { invocation ->
          val status = invocation.getArgument<BattleStatus>(1)
          val callback = invocation.getArgument<(Boolean) -> Unit>(2)
          callback(true)
          // Trigger the battle update callback to simulate status change
          battleUpdateCallback?.invoke(
              SpeechBattle(
                  battleId = "battleId",
                  challenger = "friendUid",
                  opponent = "testUser",
                  status = status,
                  context =
                      InterviewContext(
                          "testPosition",
                          "testCompany",
                          "testType",
                          "testExperience",
                          "testDescription",
                          "testFocusArea")))
          null
        }

    // Mock updateBattleStatus to simulate success and trigger battle update to CANCELLED
    `when`(
            mockBattleRepository.updateBattleStatus(
                eq("battleId"), eq(BattleStatus.CANCELLED), any()))
        .thenAnswer { invocation ->
          val status = invocation.getArgument<BattleStatus>(1)
          val callback = invocation.getArgument<(Boolean) -> Unit>(2)
          callback(true)
          // Trigger the battle update callback to simulate status change
          battleUpdateCallback?.invoke(
              SpeechBattle(
                  battleId = "battleId",
                  challenger = "friendUid",
                  opponent = "testUser",
                  status = status,
                  context =
                      InterviewContext(
                          "testPosition",
                          "testCompany",
                          "testType",
                          "testExperience",
                          "testDescription",
                          "testFocusArea")))
          null
        }

    // Mock getUserProfile to return a valid user profile
    `when`(mockUserProfileRepository.getUserProfile(eq("testUser"), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      val userProfile = UserProfile("testUser", "Test User", 100, statistics = UserStatistics())
      onSuccess(userProfile)
      null
    }

    // Mock getAllUserProfiles to return a list of user profiles
    `when`(mockUserProfileRepository.getAllUserProfiles(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(0)
      val profiles =
          listOf(
              UserProfile("friendUid", "Friend Name", 100, statistics = UserStatistics()),
              UserProfile("testUser", "Test User", 100, statistics = UserStatistics()))
      onSuccess(profiles)
      null
    }

    // Initialize UserProfileViewModel
    userProfileViewModel.getUserProfile("testUser")

    // Initialize BattleViewModel
    battleViewModel =
        BattleViewModel(
            battleRepository = mockBattleRepository,
            userProfileViewModel = userProfileViewModel,
            navigationActions = mockNavigationActions,
            apiLinkViewModel = apiLinkViewModel,
            chatViewModel = chatViewModel)
  }

  @After
  fun tearDown() {
    // Remove any listeners to prevent leaks
    mockListenerRegistration.remove()
  }

  /** Test that all UI components are displayed correctly. */
  @Test
  fun testComponentsDisplayed() {
    // Set the content of the Compose UI
    composeTestRule.setContent {
      BattleRequestSentScreen(
          friendUid = "friendUid",
          battleId = "battleId",
          userProfileViewModel = userProfileViewModel,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Verify that all UI elements are displayed
    composeTestRule.onNodeWithTag("topAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("battleRequestSentScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("battleRequestSentText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  /** Test that clicking the back button triggers navigation back. */
  @Test
  fun testBackButtonNavigatesBack() {
    // Set the content of the Compose UI
    composeTestRule.setContent {
      BattleRequestSentScreen(
          friendUid = "friendUid",
          battleId = "battleId",
          userProfileViewModel = userProfileViewModel,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Perform click on the back button
    composeTestRule.onNodeWithTag("backButton", useUnmergedTree = true).performClick()

    // Verify that navigation back was called
    verify(mockNavigationActions).goBack()
  }

  /**
   * Test that updating the battle status to IN_PROGRESS triggers navigation to the battle screen.
   */
  @Test
  fun testNavigationOnBattleAccepted() {
    // Set the content of the Compose UI
    composeTestRule.setContent {
      BattleRequestSentScreen(
          friendUid = "friendUid",
          battleId = "battleId",
          userProfileViewModel = userProfileViewModel,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Simulate updating the battle status to IN_PROGRESS
    battleViewModel.updateBattleStatus("battleId", BattleStatus.IN_PROGRESS) {
      // No action needed here for this simple test
    }

    // Wait for the UI to settle after status update
    composeTestRule.waitForIdle()

    // Verify that navigation to the battle screen was triggered
    verify(mockNavigationActions).navigateToBattleScreen("battleId", "testUser")
  }

  /** Test that updating the battle status to CANCELLED triggers navigation to the home screen. */
  @Test
  fun testNavigationOnBattleDeclined() {
    // Set the content of the Compose UI
    composeTestRule.setContent {
      BattleRequestSentScreen(
          friendUid = "friendUid",
          battleId = "battleId",
          userProfileViewModel = userProfileViewModel,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Simulate updating the battle status to CANCELLED
    battleViewModel.updateBattleStatus("battleId", BattleStatus.CANCELLED) {
      // No action needed here for this simple test
    }

    // Wait for the UI to settle after status update
    composeTestRule.waitForIdle()

    // Verify that navigation to the battle screen was triggered
    verify(mockNavigationActions).navigateTo(eq(TopLevelDestinations.HOME))
  }
}
