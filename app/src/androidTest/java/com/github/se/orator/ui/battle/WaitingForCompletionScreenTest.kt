package com.github.se.orator.ui.battle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

class WaitingForCompletionScreenTest {

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

  private val testBattleId = "testBattleId"
  private val testUserId = "testUser"

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock getCurrentUserUid to return "testUser"
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(testUserId)

    // Initialize ViewModels
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    // Initial battle state: other user not completed yet.
    val initialBattle =
        SpeechBattle(
            battleId = testBattleId,
            challenger = testUserId,
            opponent = "otherUser",
            status = BattleStatus.IN_PROGRESS,
            context =
                InterviewContext(
                    "testPosition",
                    "testCompany",
                    "testType",
                    "testExperience",
                    "testDescription",
                    "testFocusArea"),
            challengerCompleted = true, // Current user has completed
            opponentCompleted = false // Other user has not yet completed
            )

    // Mock listenToBattleUpdates to capture the callback
    `when`(mockBattleRepository.listenToBattleUpdates(eq(testBattleId), any())).thenAnswer {
        invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      battleUpdateCallback = callback
      // Send initial battle state
      callback(initialBattle)
      mockListenerRegistration
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
              UserProfile("otherUser", "Other User", 100, statistics = UserStatistics()),
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
    mockListenerRegistration.remove()
  }

  /** Test that all UI components are displayed correctly initially. */
  @Test
  fun testComponentsDisplayed() {
    composeTestRule.setContent {
      WaitingForCompletionScreen(
          battleId = testBattleId,
          userId = testUserId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    composeTestRule.waitForIdle()

    // Verify that waiting screen and loading indicator are displayed
    composeTestRule.onNodeWithTag("waitingForCompletionScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("waitingText").assertIsDisplayed()
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()

    // No navigation should have happened yet since otherUser not completed
    verify(mockNavigationActions, never()).navigateToEvaluationScreen(anyString())
  }

  /**
   * Test navigation when the other user completes the battle. We simulate a battle update where the
   * opponentCompleted is now true.
   */
  @Test
  fun testNavigationWhenOtherUserCompleted() {
    composeTestRule.setContent {
      WaitingForCompletionScreen(
          battleId = testBattleId,
          userId = testUserId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    composeTestRule.waitForIdle()

    // Simulate update: now the other user has completed
    val updatedBattle =
        SpeechBattle(
            battleId = testBattleId,
            challenger = testUserId,
            opponent = "otherUser",
            status = BattleStatus.IN_PROGRESS,
            context =
                InterviewContext(
                    "testPosition",
                    "testCompany",
                    "testType",
                    "testExperience",
                    "testDescription",
                    "testFocusArea"),
            challengerCompleted = true,
            opponentCompleted = true // now other user completed
            )

    // Trigger the callback to simulate battle update
    battleUpdateCallback?.invoke(updatedBattle)

    composeTestRule.waitForIdle()

    // Now navigation to evaluation screen should have been called
    verify(mockNavigationActions).navigateToEvaluationScreen(eq(testBattleId))
  }
}
