package com.github.se.orator.ui.overview

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
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.ChatResponse
import com.github.se.orator.ui.network.Choice
import com.github.se.orator.ui.network.Message
import com.github.se.orator.ui.network.Usage
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FeedbackScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testDispatcher = StandardTestDispatcher()

  @Mock private lateinit var chatGPTService: ChatGPTService

  @Mock private lateinit var navigationActions: NavigationActions

  @Mock private lateinit var userProfileRepository: UserProfileRepository

  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var userProfileViewModel: UserProfileViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    MockitoAnnotations.openMocks(this)

    // Initialize the mocked UserProfileRepository
    userProfileRepository = mock(UserProfileRepository::class.java)

    // Create the UserProfileViewModel with the mocked repository
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    // Mock data for UserProfile
    val testUid = "testUid"
    val testUserProfile =
        UserProfile(
            uid = testUid,
            name = "Test User",
            age = 25,
            statistics = UserStatistics(),
            friends = listOf(),
            bio = "Test bio")

    // Mock getCurrentUserUid()
    `when`(userProfileRepository.getCurrentUserUid()).thenReturn(testUid)

    // Mock getUserProfile()
    doAnswer { invocation ->
          val uid = invocation.arguments[0] as String
          val onSuccess = invocation.arguments[1] as (UserProfile?) -> Unit
          val onFailure = invocation.arguments[2] as (Exception) -> Unit

          // Simulate success callback with testUserProfile
          onSuccess(testUserProfile)
          null
        }
        .whenever(userProfileRepository)
        .getUserProfile(any(), any(), any())

    apiLinkViewModel = ApiLinkViewModel()

    val practiceContext =
        InterviewContext(
            interviewType = "Technical",
            role = "Software Engineer",
            company = "Tech Corp",
            focusAreas = listOf("Algorithms", "Data Structures"))
    apiLinkViewModel.updatePracticeContext(practiceContext)
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
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    chatViewModel.generateFeedback()

    advanceUntilIdle()

    composeTestRule.setContent {
      FeedbackScreen(
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel,
          apiLinkViewModel = apiLinkViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("feedbackScreen").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackTopAppBar").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("FeedbackText").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("back_button").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackContent").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackTitle").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackSubtitle").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("feedbackNoMessage").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithTag("retryButton").assertExists().assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("retryButtonText", useUnmergedTree = true)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun clickingTheTryAgainButtonRedirectsToHomeScreen() = runTest {
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
    composeTestRule.setContent {
      FeedbackScreen(
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel,
          apiLinkViewModel = apiLinkViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("retryButton").performClick()

    verify(navigationActions).navigateTo(TopLevelDestinations.HOME)
  }

  @Test
  fun feedbackMessageIsShownWhenThereIsOne() = runTest {
    `when`(chatGPTService.getChatCompletion(any()))
        .thenReturn(
            ChatResponse(
                "id",
                "object",
                0,
                "model",
                listOf(Choice(0, Message("assistant", "This is your feedback."), null)),
                Usage(0, 0, 0)))
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    chatViewModel.generateFeedback()

    advanceUntilIdle()

    composeTestRule.setContent {
      FeedbackScreen(
          chatViewModel = chatViewModel,
          userProfileViewModel = userProfileViewModel,
          apiLinkViewModel = apiLinkViewModel,
          navigationActions = navigationActions)
    }

    composeTestRule.onNodeWithTag("feedbackMessage").assertExists().assertIsDisplayed()
  }
}
