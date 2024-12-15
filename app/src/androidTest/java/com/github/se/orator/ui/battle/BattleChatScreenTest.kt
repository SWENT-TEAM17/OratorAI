package com.github.se.orator.ui.battle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.BattleRepository
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatResponse
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.network.Usage
import com.github.se.orator.ui.overview.ChatButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class BattleChatScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testDispatcher = StandardTestDispatcher()

  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockBattleRepository: BattleRepository
  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository
  @Mock private lateinit var chatGPTService: ChatGPTService

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var battleViewModel: BattleViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockitoAnnotations.openMocks(this)

    // Mock the necessary dependencies
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn("testUser")
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)

    apiLinkViewModel = ApiLinkViewModel()

    // Initialize `chatViewModel`
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    // Mock `getBattleById`
    `when`(mockBattleRepository.getBattleById(eq("testBattle"), any())).thenAnswer { invocation ->
      val callback = invocation.getArgument<(SpeechBattle?) -> Unit>(1)
      callback.invoke(
          SpeechBattle(
              battleId = "testBattle",
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
    }

    // Initialize `battleViewModel` after all dependencies are mocked
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
    Dispatchers.resetMain()
    testDispatcher.cancel()
  }

  @Test
  fun screenIsDisplayed() = runTest {
    `when`(chatGPTService.getChatCompletion(any()))
        .thenReturn(ChatResponse("id", "object", 0, "model", emptyList(), Usage(0, 0, 0)))

    chatViewModel.initializeConversation()
    advanceUntilIdle()

    composeTestRule.setContent {
      BattleChatScreen(
          battleId = "testBattle",
          userId = "testUser",
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel)
    }

    composeTestRule.onNodeWithTag("top_app_bar").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_screen_column").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_messages_list").assertExists().assertIsDisplayed()
  }

  @Test
  fun finishBattleButtonTriggersViewModelMethod() = runTest {
    val chatMessages = emptyList<Message>()

    composeTestRule.setContent {
      BattleChatScreen(
          battleId = "testBattle",
          userId = "testUser",
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel)
    }

    composeTestRule.onNodeWithTag("finish_battle_button").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("finish_battle_button").performClick()

    // Verify that the repository method was called with the correct parameters
    verify(mockBattleRepository)
        .updateUserBattleData(eq("testBattle"), eq("testUser"), eq(chatMessages), any())
  }

  @Test
  fun backButtonNavigatesBack() = runTest {
    composeTestRule.setContent {
      BattleChatScreen(
          battleId = "testBattle",
          userId = "testUser",
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel)
    }

    composeTestRule.onNodeWithTag("back_button").performClick()

    verify(mockNavigationActions).goBack()
  }

  @Test
  fun loadingIndicatorIsNotDisplayedAndFinishBattleButtonIsEnabled() = runTest {
    `when`(chatGPTService.getChatCompletion(any()))
        .thenReturn(ChatResponse("id", "object", 0, "model", emptyList(), Usage(0, 0, 0)))

    composeTestRule.setContent {
      BattleChatScreen(
          battleId = "testBattle",
          userId = "testUser",
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel,
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel)
    }

    composeTestRule.onNodeWithTag("loading_indicator").assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(
            ChatButtonType.FINISH_BATTLE_BUTTON.buttonTextTestTag, useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(ChatButtonType.FINISH_BATTLE_BUTTON.buttonTestTag)
        .assertIsEnabled()
  }
}
