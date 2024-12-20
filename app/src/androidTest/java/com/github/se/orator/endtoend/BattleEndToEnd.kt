package com.github.se.orator.endtoend

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.*
import com.github.se.orator.ui.battle.*
import com.github.se.orator.ui.friends.ViewFriendsScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.Message
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class EndToEndFullBattleFlowTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository

  @Mock private lateinit var mockBattleRepository: BattleRepository

  @Mock private lateinit var chatGPTService: ChatGPTService

  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var battleViewModel: BattleViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var navigationActions: NavigationActions

  private val friendUid = "friendUid"
  private val friendName = "Friend Name"
  private val testBattleId = "testBattleId"
  private val currentUserId = "testUser"

  // Callback for simulating Firestore updates
  private var battleUpdateCallback: ((SpeechBattle?) -> Unit)? = null

  // NavHostController for managing navigation
  private lateinit var navController: NavHostController

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock user profiles
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn(currentUserId)

    `when`(mockUserProfileRepository.getUserProfile(eq(friendUid), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      // Mark each other as friends so that this friend will show up
      onSuccess(
          UserProfile(
              uid = friendUid,
              name = friendName,
              age = 100,
              statistics = UserStatistics(),
              friends = listOf(currentUserId)))
      null
    }

    `when`(mockUserProfileRepository.getUserProfile(eq(currentUserId), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      // Current user has the friendUid in its friends list
      onSuccess(
          UserProfile(
              uid = currentUserId,
              name = "Test User",
              age = 100,
              statistics = UserStatistics(),
              friends = listOf(friendUid)))
      null
    }

    `when`(mockUserProfileRepository.getAllUserProfiles(any(), any())).thenAnswer { invocation ->
      val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(0)
      onSuccess(
          listOf(
              UserProfile(friendUid, friendName, 100, statistics = UserStatistics()),
              UserProfile(currentUserId, "Test User", 100, statistics = UserStatistics())))
      null
    }

    // Mock getFriendsProfiles to return our friend
    `when`(mockUserProfileRepository.getFriendsProfiles(eq(listOf(friendUid)), any(), any()))
        .thenAnswer { invocation ->
          val onSuccess = invocation.getArgument<(List<UserProfile>) -> Unit>(1)
          onSuccess(listOf(UserProfile(friendUid, friendName, 100, statistics = UserStatistics())))
          null
        }

    // Mock battle repository
    `when`(mockBattleRepository.generateUniqueBattleId()).thenReturn(testBattleId)
    doAnswer { invocation ->
          val callback = invocation.getArgument<(Boolean) -> Unit>(1)
          callback(true)
          null
        }
        .`when`(mockBattleRepository)
        .storeBattleRequest(any(), any())

    // Simulate battle updates
    `when`(mockBattleRepository.listenToBattleUpdates(eq(testBattleId), any())).thenAnswer {
        invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      battleUpdateCallback = callback
      // Initial: PENDING after request
      callback(
          SpeechBattle(
              battleId = testBattleId,
              challenger = currentUserId,
              opponent = friendUid,
              status = BattleStatus.PENDING,
              context =
                  InterviewContext(
                      interviewType = "Phone Interview",
                      targetPosition = "Software Engineer",
                      companyName = "Tech Corp",
                      jobDescription = "Develop and maintain software applications.",
                      experienceLevel = "Mid-Level",
                      focusArea = "Technical Questions")))
      mockListenerRegistration
    }

    doAnswer { invocation ->
          val status = invocation.getArgument<BattleStatus>(1)
          val callback = invocation.getArgument<(Boolean) -> Unit>(2)
          callback(true)
          // Simulate status change
          battleUpdateCallback?.invoke(
              SpeechBattle(
                  battleId = testBattleId,
                  challenger = currentUserId,
                  opponent = friendUid,
                  status = status,
                  context =
                      InterviewContext(
                          interviewType = "Phone Interview",
                          targetPosition = "Software Engineer",
                          companyName = "Tech Corp",
                          jobDescription = "Develop and maintain software applications.",
                          experienceLevel = "Mid-Level",
                          focusArea = "Technical Questions"),
                  challengerCompleted =
                      (status == BattleStatus.EVALUATING || status == BattleStatus.COMPLETED),
                  opponentCompleted =
                      (status == BattleStatus.EVALUATING || status == BattleStatus.COMPLETED)))
          null
        }
        .`when`(mockBattleRepository)
        .updateBattleStatus(eq(testBattleId), any(), any())

    doAnswer { invocation ->
          val callback = invocation.getArgument<(Boolean) -> Unit>(3)
          callback(true)
          null
        }
        .`when`(mockBattleRepository)
        .updateUserBattleData(eq(testBattleId), eq(currentUserId), any<List<Message>>(), any())

    `when`(mockBattleRepository.getBattleById(eq(testBattleId), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      callback(
          SpeechBattle(
              battleId = testBattleId,
              challenger = currentUserId,
              opponent = friendUid,
              status = BattleStatus.IN_PROGRESS,
              context =
                  InterviewContext(
                      interviewType = "Phone Interview",
                      targetPosition = "Software Engineer",
                      companyName = "Tech Corp",
                      jobDescription = "Develop and maintain software applications.",
                      experienceLevel = "Mid-Level",
                      focusArea = "Technical Questions")))
      null
    }

    // Initialize ViewModels
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
    // Initialize battleViewModel with a temporary mock for NavigationActions
    battleViewModel =
        BattleViewModel(
            battleRepository = mockBattleRepository,
            userProfileViewModel = userProfileViewModel,
            navigationActions =
                mock(NavigationActions::class.java), // Will replace after setContent
            apiLinkViewModel = apiLinkViewModel,
            chatViewModel = chatViewModel)
  }

  @After
  fun tearDown() {
    mockListenerRegistration.remove()
  }

  @Test
  fun testFullEndToEndBattleFlow() {
    composeTestRule.setContent {
      navController = rememberNavController()
      // Create a real NavigationActions linked to navController
      navigationActions = NavigationActions(navController)

      // Re-create battleViewModel with the real navigationActions
      battleViewModel =
          BattleViewModel(
              battleRepository = mockBattleRepository,
              userProfileViewModel = userProfileViewModel,
              navigationActions = navigationActions,
              apiLinkViewModel = apiLinkViewModel,
              chatViewModel = chatViewModel)

      // Set up your NavHost with all the routes
      NavHost(navController = navController, startDestination = Route.FRIENDS) {
        // Friends Screen
        composable(Route.FRIENDS) {
          ViewFriendsScreen(
              navigationActions = navigationActions,
              userProfileViewModel = userProfileViewModel,
              battleViewModel = battleViewModel)
        }

        // Battle Send Screen
        composable(
            route = "${Route.BATTLE_SEND}/{friendUid}",
            arguments = listOf(navArgument("friendUid") { type = NavType.StringType })) {
              val friendUidArg = it.arguments?.getString("friendUid") ?: ""
              BattleScreen(
                  friendUid = friendUidArg,
                  userProfileViewModel = userProfileViewModel,
                  navigationActions = navigationActions,
                  battleViewModel = battleViewModel)
            }

        // Battle Request Sent Screen
        composable(
            route = "${Route.BATTLE_REQUEST_SENT}/{battleId}/{friendUid}",
            arguments =
                listOf(
                    navArgument("battleId") { type = NavType.StringType },
                    navArgument("friendUid") { type = NavType.StringType })) { backStackEntry ->
              val battleId = backStackEntry.arguments?.getString("battleId") ?: ""
              val friendUidArg = backStackEntry.arguments?.getString("friendUid") ?: ""
              BattleRequestSentScreen(
                  friendUid = friendUidArg,
                  battleId = battleId,
                  userProfileViewModel = userProfileViewModel,
                  navigationActions = navigationActions,
                  battleViewModel = battleViewModel)
            }

        // Battle Chat Screen
        composable(
            route = "${Route.BATTLE_CHAT}/{battleId}/{userId}",
            arguments =
                listOf(
                    navArgument("battleId") { type = NavType.StringType },
                    navArgument("userId") { type = NavType.StringType })) { backStackEntry ->
              val battleId = backStackEntry.arguments?.getString("battleId") ?: ""
              val userId = backStackEntry.arguments?.getString("userId") ?: ""
              BattleChatScreen(
                  battleId = battleId,
                  userId = userId,
                  navigationActions = navigationActions,
                  battleViewModel = battleViewModel,
                  chatViewModel = chatViewModel,
                  userProfileViewModel = userProfileViewModel)
            }

        // Waiting for Completion Screen
        composable(
            route = "${Route.WAITING_FOR_COMPLETION}/{battleId}/{friendUid}",
            arguments =
                listOf(
                    navArgument("battleId") { type = NavType.StringType },
                    navArgument("friendUid") { type = NavType.StringType })) { backStackEntry ->
              val battleId = backStackEntry.arguments?.getString("battleId") ?: ""
              val friendUidArg = backStackEntry.arguments?.getString("friendUid") ?: ""
              WaitingForCompletionScreen(
                  friendUid = friendUidArg,
                  battleId = battleId,
                  navigationActions = navigationActions,
                  battleViewModel = battleViewModel,
                  userProfileViewModel = userProfileViewModel)
            }

        // Evaluation Screen
        composable(
            route = "${Route.EVALUATION}/{battleId}",
            arguments = listOf(navArgument("battleId") { type = NavType.StringType })) {
                backStackEntry ->
              val battleId = backStackEntry.arguments?.getString("battleId") ?: ""
              val userId = userProfileViewModel.userProfile.value?.uid ?: ""
              EvaluationScreen(
                  userId = userId,
                  battleId = battleId,
                  navigationActions = navigationActions,
                  battleViewModel = battleViewModel,
                  chatViewModel = chatViewModel)
            }

        // Home Screen
        composable(Screen.HOME) {
          Box(modifier = Modifier.testTag("homeScreen")) { Text(text = "Home Screen") }
        }
      }
    }

    composeTestRule.waitForIdle()

    // **1. On Friends screen, find a friend and click to navigate to BattleSendScreen**
    composeTestRule
        .onNodeWithTag("viewFriendsItem#$friendUid", useUnmergedTree = true)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()

    // **2. Fill in fields on BattleScreen and send request**
    inputText("targetPositionInput-TextField", "Software Engineer")
    inputText("companyNameInput-TextField", "Tech Corp")
    selectDropdownItem("interviewTypeInput-DropdownBox", "Phone Interview")
    selectDropdownItem("experienceLevelInput-DropdownBox", "Mid-Level")

    // **Use text-based selector for job description input**
    composeTestRule
        .onNodeWithText("Paste the job description here", substring = true)
        .performTextInput("Develop and maintain software applications.")
    composeTestRule.waitForIdle()

    selectDropdownItem("focusAreaInput-DropdownBox", "Technical Questions")

    // **Send battle request**
    composeTestRule.onNodeWithTag("getStartedButton").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // **Verify navigation to BattleRequestSentScreen**
    composeTestRule.onNodeWithTag("battleRequestSentScreen").assertIsDisplayed()

    // **3. Simulate friend accepting: update status to IN_PROGRESS**
    runBlocking {
      battleUpdateCallback?.invoke(
          SpeechBattle(
              battleId = testBattleId,
              challenger = currentUserId,
              opponent = friendUid,
              status = BattleStatus.IN_PROGRESS,
              context =
                  InterviewContext(
                      "Software Engineer",
                      "Tech Corp",
                      "Phone Interview",
                      "Mid-Level",
                      "Develop and maintain software applications.",
                      "Technical Questions")))
    }
    composeTestRule.waitForIdle()

    // **4. Finish battle on chat screen**
    composeTestRule.onNodeWithTag("finish_battle_button").performClick()
    composeTestRule.waitForIdle()

    // **Verify navigation to WaitingForCompletionScreen**
    composeTestRule
        .onNodeWithTag("waitingForCompletionScreen", useUnmergedTree = true)
        .assertIsDisplayed()

    // **5. Simulate both users completed: update status to EVALUATING**
    runBlocking {
      battleUpdateCallback?.invoke(
          SpeechBattle(
              battleId = testBattleId,
              challenger = currentUserId,
              opponent = friendUid,
              status = BattleStatus.EVALUATING,
              context =
                  InterviewContext(
                      "Software Engineer",
                      "Tech Corp",
                      "Phone Interview",
                      "Mid-Level",
                      "Develop and maintain software applications.",
                      "Technical Questions"),
              challengerCompleted = true,
              opponentCompleted = true))
    }
    composeTestRule.waitForIdle()

    // **Verify navigation to EvaluationScreen**
    composeTestRule.onNodeWithTag("battleEvaluation", useUnmergedTree = true).assertIsDisplayed()

    // **6. Simulate completed evaluation with result**
    runBlocking {
      battleUpdateCallback?.invoke(
          SpeechBattle(
              battleId = testBattleId,
              challenger = currentUserId,
              opponent = friendUid,
              status = BattleStatus.COMPLETED,
              context =
                  InterviewContext(
                      "Software Engineer",
                      "Tech Corp",
                      "Phone Interview",
                      "Mid-Level",
                      "Develop and maintain software applications.",
                      "Technical Questions"),
              challengerCompleted = true,
              opponentCompleted = true,
              evaluationResult =
                  EvaluationResult(
                      winnerUid = currentUserId,
                      winnerMessage = Message("Great job!", "assistant"),
                      loserMessage = Message("Better luck next time.", "assistant"))))
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("waitingForEvaluation").assertIsDisplayed()
  }

  // **Helper functions**
  private fun inputText(testTag: String, text: String) {
    composeTestRule
        .onNodeWithTag(testTag)
        .assertExists("No TextField found with tag: $testTag")
        .performTextInput(text)
    composeTestRule.onNodeWithTag(testTag).assertTextContains(text)
  }

  /** Helper function to select an item from a dropdown with the given dropdownBoxTag. */
  private fun selectDropdownItem(dropdownBoxTag: String, itemText: String) {
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag(dropdownBoxTag))
    composeTestRule.onNodeWithTag(dropdownBoxTag).performClick()
    composeTestRule.onNodeWithText(itemText).performClick()
    // After selection, verify that the chosen text is visible in the field
    composeTestRule
        .onNodeWithTag(dropdownBoxTag.replace("DropdownBox", "DropdownField"))
        .assertTextContains(itemText)
  }
}
