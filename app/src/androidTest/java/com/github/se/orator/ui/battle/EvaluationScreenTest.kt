package com.github.se.orator.ui.battle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.*
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.Message
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

class EvaluationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockNavigationActions: NavigationActions

  @Mock private lateinit var mockBattleRepository: BattleRepository

  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository

  @Mock private lateinit var mockChatGPTService: ChatGPTService

  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var battleViewModel: BattleViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  // Constants for testing
  private val currentUserId = "testUserId"
  private val testFriendUid = "friendUid"
  private val testBattleId = "testBattleId"

  // Variable to capture the battle update callback
  private var battleUpdateCallback: ((SpeechBattle?) -> Unit)? = null

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock getCurrentUserUid to return "testUserId"
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(currentUserId)

    // Initialize ViewModels
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(mockChatGPTService, apiLinkViewModel)
    battleViewModel =
        BattleViewModel(
            battleRepository = mockBattleRepository,
            userProfileViewModel = userProfileViewModel,
            navigationActions = mockNavigationActions,
            apiLinkViewModel = apiLinkViewModel,
            chatViewModel = chatViewModel)

    // Mock listenToBattleUpdates to capture the callback and return mock ListenerRegistration
    `when`(mockBattleRepository.listenToBattleUpdates(eq(testBattleId), any())).thenAnswer {
        invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      battleUpdateCallback = callback
      mockListenerRegistration
    }
  }

  @After
  fun tearDown() {
    mockListenerRegistration.remove()
  }

  /** Helper function to create a mock SpeechBattle. */
  private fun createMockBattle(
      status: BattleStatus,
      evaluationResult: EvaluationResult? = null,
      userId: String,
      challengerUid: String = "challengerUid",
      opponentUid: String = "opponentUid"
  ): SpeechBattle {
    return SpeechBattle(
        battleId = testBattleId,
        challenger = challengerUid,
        opponent = opponentUid,
        status = status,
        context = getMockInterviewContext(),
        evaluationResult = evaluationResult,
        challengerCompleted = status != BattleStatus.EVALUATING,
        opponentCompleted = status != BattleStatus.EVALUATING)
  }

  /** Helper function to create a mock InterviewContext. */
  private fun getMockInterviewContext(): InterviewContext {
    return InterviewContext(
        targetPosition = "Software Engineer",
        companyName = "Tech Corp",
        interviewType = "Phone Interview",
        experienceLevel = "Mid-Level",
        jobDescription = "Develop and maintain software applications.",
        focusArea = "Technical Questions")
  }

  /** Test that the EvaluationScreen shows loading when battle is null. */
  @Test
  fun evaluationScreen_ShowsLoadingState_WhenBattleIsNull() {
    // Arrange
    // No battle is emitted, initial state is null

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = currentUserId,
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Verify that listenToBattleUpdates was called with the correct battleId
    verify(mockBattleRepository).listenToBattleUpdates(eq(testBattleId), any())

    // Assert
    composeTestRule.onNodeWithText("Loading battle data...").assertIsDisplayed()
  }

  /** Test that the EvaluationScreen shows evaluating state correctly. */
  @Test
  fun evaluationScreen_ShowsEvaluatingState_WhenBattleIsEvaluating() {
    // Arrange
    val evaluatingBattle =
        createMockBattle(
            status = BattleStatus.EVALUATING,
            evaluationResult = null,
            userId = currentUserId,
            challengerUid = currentUserId,
            opponentUid = testFriendUid)

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = currentUserId,
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Emit the evaluating battle via battleUpdateCallback
    battleUpdateCallback?.invoke(evaluatingBattle)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Evaluating performance and determining the winner")
        .assertIsDisplayed()
  }

  /** Test that the EvaluationScreen shows completed state for winner correctly. */
  @Test
  fun evaluationScreen_ShowsCompletedState_Winner() {
    // Arrange
    val evaluationResult =
        EvaluationResult(
            winnerUid = currentUserId,
            winnerMessage = Message(content = "Great job!", role = "assistant"),
            loserMessage = Message(content = "Better luck next time.", role = "assistant"))
    val completedBattle =
        createMockBattle(
            status = BattleStatus.COMPLETED,
            evaluationResult = evaluationResult,
            userId = currentUserId,
            challengerUid = "challengerUid",
            opponentUid = currentUserId)

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = currentUserId,
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Emit the completed battle via battleUpdateCallback
    battleUpdateCallback?.invoke(completedBattle)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithText("You Won!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Great job!").assertIsDisplayed()

    // Verify that action buttons are displayed
    composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    composeTestRule.onNodeWithText("Go to Practice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Return to Home").assertIsDisplayed()
  }

  /** Test that the EvaluationScreen shows completed state for loser correctly. */
  @Test
  fun evaluationScreen_ShowsCompletedState_Loser() {
    // Arrange
    val evaluationResult =
        EvaluationResult(
            winnerUid = "challengerUid",
            winnerMessage = Message(content = "Great job!", role = "assistant"),
            loserMessage = Message(content = "Better luck next time.", role = "assistant"))
    val completedBattle =
        createMockBattle(
            status = BattleStatus.COMPLETED,
            evaluationResult = evaluationResult,
            userId = "testUserId",
            challengerUid = "testUserId",
            opponentUid = "opponentUid")

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = "testUserId",
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Emit the completed battle via battleUpdateCallback
    battleUpdateCallback?.invoke(completedBattle)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithText("You Lost.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Better luck next time.").assertIsDisplayed()

    // Verify that action buttons are displayed
    composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    composeTestRule.onNodeWithText("Go to Practice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Return to Home").assertIsDisplayed()
  }

  /**
   * Test that the EvaluationScreen shows processing state when evaluationResult is null but status
   * is COMPLETED.
   */
  @Test
  fun evaluationScreen_ShowsProcessingState_WhenEvaluationResultIsNull() {
    // Arrange
    val completedBattle =
        createMockBattle(
            status = BattleStatus.COMPLETED,
            evaluationResult = null,
            userId = currentUserId,
            challengerUid = "challengerUid",
            opponentUid = currentUserId)

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = currentUserId,
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Emit the completed battle via battleUpdateCallback
    battleUpdateCallback?.invoke(completedBattle)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithText("Processing results...").assertIsDisplayed()
  }

  /** Test clicking the "Go to Practice" button navigates correctly and calls reset functions. */
  @Test
  fun evaluationScreen_ClickGoToPractice_NavigatesCorrectly() {
    // Arrange
    val evaluationResult =
        EvaluationResult(
            winnerUid = currentUserId,
            winnerMessage = Message(content = "Great job!", role = "assistant"),
            loserMessage = Message(content = "Better luck next time.", role = "assistant"))
    val completedBattle =
        createMockBattle(
            status = BattleStatus.COMPLETED,
            evaluationResult = evaluationResult,
            userId = currentUserId,
            challengerUid = "challengerUid",
            opponentUid = currentUserId)

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = currentUserId,
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Emit the completed battle via battleUpdateCallback
    battleUpdateCallback?.invoke(completedBattle)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert buttons are displayed
    composeTestRule.onNodeWithText("Go to Practice").assertIsDisplayed()

    // Perform click on "Go to Practice" button
    composeTestRule.onNodeWithText("Go to Practice").performClick()

    // Verify navigation to Speaking Job Interview
    verify(mockNavigationActions).navigateTo(Screen.SPEAKING_JOB_INTERVIEW)
  }

  /** Test clicking the "Return to Home" button navigates correctly and calls reset functions. */
  @Test
  fun evaluationScreen_ClickReturnToHome_NavigatesCorrectly() {
    // Arrange
    val evaluationResult =
        EvaluationResult(
            winnerUid = currentUserId,
            winnerMessage = Message(content = "Great job!", role = "assistant"),
            loserMessage = Message(content = "Better luck next time.", role = "assistant"))
    val completedBattle =
        createMockBattle(
            status = BattleStatus.COMPLETED,
            evaluationResult = evaluationResult,
            userId = currentUserId,
            challengerUid = "challengerUid",
            opponentUid = currentUserId)

    // Act
    composeTestRule.setContent {
      EvaluationScreen(
          userId = currentUserId,
          battleId = testBattleId,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel)
    }

    // Emit the completed battle via battleUpdateCallback
    battleUpdateCallback?.invoke(completedBattle)

    // Wait for the UI to update
    composeTestRule.waitForIdle()

    // Assert buttons are displayed
    composeTestRule.onNodeWithText("Return to Home").assertIsDisplayed()

    // Perform click on "Return to Home" button
    composeTestRule.onNodeWithText("Return to Home").performClick()

    // Verify navigation to Home
    verify(mockNavigationActions).navigateTo(Screen.HOME)
  }
}
