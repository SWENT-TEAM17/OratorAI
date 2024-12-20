package com.github.se.orator.endtoend

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.offlinePrompts.OfflinePromptsFunctionsInterface
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.symblAi.AudioPlayer
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.model.theme.AppThemeViewModel
import com.github.se.orator.ui.friends.AddFriendsScreen
import com.github.se.orator.ui.friends.LeaderboardScreen
import com.github.se.orator.ui.friends.ViewFriendsScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.offline.OfflineRecordingScreen
import com.github.se.orator.ui.offline.OfflineScreen
import com.github.se.orator.ui.offline.RecordingReviewScreen
import com.github.se.orator.ui.overview.OfflineInterviewModule
import com.github.se.orator.ui.profile.CreateAccountScreen
import com.github.se.orator.ui.profile.EditProfileScreen
import com.github.se.orator.ui.profile.PreviousRecordingsFeedbackScreen
import com.github.se.orator.ui.profile.ProfileScreen
import com.github.se.orator.ui.settings.SettingsScreen
import com.github.se.orator.ui.speaking.SpeakingScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class OfflineEndToEndAppTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

  // repository, viewmodel, and data mocks that will be used in the tests
  private lateinit var navigationActions: NavigationActions
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var mockThemeContext: Context
  private lateinit var appThemeViewModel: AppThemeViewModel
  private lateinit var mockSharedPreferences: SharedPreferences
  private lateinit var mockEditor: SharedPreferences.Editor
  private lateinit var data: AnalysisData
  private lateinit var speech: String
  private lateinit var speakingRepository: SpeakingRepository
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatGPTService: ChatGPTService
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var mockPlayer: AudioPlayer
  private lateinit var offlinePromptFunctions: OfflinePromptsFunctionsInterface

  // hardcoded values used in tests
  private val bio = "Test bio"
  private val testUserProfile =
      UserProfile(uid = "testUid", name = "", age = 25, statistics = UserStatistics(), bio = bio)

  private val profile1 = UserProfile("1", "John Doe", 99, statistics = UserStatistics())
  private val profile2 = UserProfile("2", "Jane Doe", 100, statistics = UserStatistics())

  // list of screens needed in the end 2 end tests : useful to see what screens have been tested
  // this far!
  private val screenList =
      listOf(
          Screen.HOME,
          Screen.SETTINGS,
          Screen.EDIT_PROFILE,
          Screen.ADD_FRIENDS,
          Screen.FRIENDS,
          Screen.LEADERBOARD,
          Screen.CREATE_PROFILE,
          Screen.SPEAKING,
          Screen.OFFLINE,
          Screen.OFFLINE_INTERVIEW_MODULE,
          Screen.PRACTICE_QUESTIONS_SCREEN,
          Screen.OFFLINE_RECORDING_SCREEN,
          Screen.OFFLINE_RECORDING_REVIEW_SCREEN,
          Screen.FEEDBACK_SCREEN,
          Screen.OFFLINE_RECORDING_REVIEW_SCREEN)

  @Before
  fun setUp() {
    // mocking all the aforementioned variables
    navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    mockSharedPreferences = mock(SharedPreferences::class.java)
    mockThemeContext = mock(Context::class.java)
    mockEditor = mock(SharedPreferences.Editor::class.java)
    appThemeViewModel = AppThemeViewModel(mockThemeContext)
    apiLinkViewModel = ApiLinkViewModel()
    speakingRepository = mock(SpeakingRepository::class.java)
    chatGPTService = mock(ChatGPTService::class.java)
    offlinePromptFunctions = mock(OfflinePromptsFunctionsInterface::class.java)
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.IDLE))
    `when`(
            offlinePromptFunctions.getPromptMapElement(
                org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .thenReturn("Apple")
    speakingViewModel =
        SpeakingViewModel(speakingRepository, apiLinkViewModel, userProfileViewModel)
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

    speech = "Hello! My name is John. I am an entrepreneur"
    data = AnalysisData(speech, 5, 2.0, 1.0)

    // hardcoded repository function returns to allow more dynamic testing
    `when`(
            speakingRepository.setupAnalysisResultsUsage(
                org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .thenAnswer { invocation ->
          // Extract the onSuccess callback and invoke it
          val onSuccessCallback = invocation.getArgument<(AnalysisData) -> Unit>(0)
          onSuccessCallback.invoke(data)
          null
        }

    `when`(
            mockThemeContext.getSharedPreferences(
                org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .thenReturn(mockSharedPreferences)
    `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
    `when`(mockEditor.putString(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .thenReturn(mockEditor)

    // mocking the request for the user who is using the app
    `when`(
            userProfileRepository.getUserProfile(
                org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .then { it.getArgument<(UserProfile) -> Unit>(1)(testUserProfile) }

    `when`(
            userProfileRepository.getFriendsProfiles(
                org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .then { it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2)) }

    `when`(
            userProfileRepository.getAllUserProfiles(
                org.mockito.kotlin.any(), org.mockito.kotlin.any()))
        .then { it.getArgument<(List<UserProfile>) -> Unit>(0)(listOf(profile1, profile2)) }

    // mocking navigation functions
    `when`(navigationActions.currentRoute()).thenReturn(Screen.FRIENDS)
    `when`(navigationActions.goBack()).then { navController?.popBackStack() ?: {} }
    for (screen in screenList) {
      `when`(navigationActions.navigateTo(screen)).then { navController?.navigate(screen) }
    }

    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel.getUserProfile(testUserProfile.uid)
  }

  var navController: NavHostController? = null

  @Test
  fun testEndToEndNavigationAndUI() {
    // Set up NavHost for navigation and initialize different screens within one setContent
    composeTestRule.setContent {
      navController = rememberNavController()

      NavHost(navController = navController!!, startDestination = Screen.HOME) {
        composable(Screen.HOME) { ProfileScreen(navigationActions, userProfileViewModel) }
        composable(Screen.SETTINGS) {
          SettingsScreen(navigationActions, userProfileViewModel, appThemeViewModel)
        }
        composable(Screen.FRIENDS) { ViewFriendsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.ADD_FRIENDS) { AddFriendsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.EDIT_PROFILE) {
          EditProfileScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.CREATE_PROFILE) {
          CreateAccountScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.PROFILE) { ProfileScreen(navigationActions, userProfileViewModel) }
        composable(Screen.LEADERBOARD) {
          LeaderboardScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.SPEAKING) {
          SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
        }

        composable(Screen.OFFLINE) { OfflineScreen(navigationActions = navigationActions) }

        composable(Screen.OFFLINE_INTERVIEW_MODULE) {
          SpeakingScreen(navigationActions = navigationActions, speakingViewModel, apiLinkViewModel)
        }

        composable(Screen.OFFLINE_INTERVIEW_MODULE) {
          OfflineInterviewModule(navigationActions, speakingViewModel, offlinePromptFunctions)
        }
        composable(Screen.OFFLINE_RECORDING_SCREEN) {
          OfflineRecordingScreen(
              navigationActions = navigationActions,
              question = "How do you handle conflict in a team?",
              viewModel = speakingViewModel,
              offlinePromptsFunctions = offlinePromptFunctions)
        }

        composable(Screen.OFFLINE_RECORDING_REVIEW_SCREEN) {
          RecordingReviewScreen(navigationActions, speakingViewModel)
        }

        composable(Screen.FEEDBACK_SCREEN) {
          PreviousRecordingsFeedbackScreen(
              navigationActions = navigationActions,
              viewModel = chatViewModel,
              speakingViewModel = speakingViewModel,
              player = mockPlayer,
              offlinePromptsFunctions = offlinePromptFunctions)
        }
      }
    }
    composeTestRule.runOnUiThread {
      navController?.navigate(
          Screen.CREATE_PROFILE) // this here forces us to navigate to the create_profile screen
    }
    composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("save_profile_button")
        .assertIsNotEnabled() // clicking save profile without a username to see if the button is
    // enabled (it shouldn't be)

    composeTestRule
        .onNodeWithTag("username_input")
        .performTextInput("TestUser") // add a username then the button should be enabled
    composeTestRule
        .onNodeWithTag("username_input")
        .assertTextContains("TestUser") // making sure username is shown correctly
    composeTestRule
        .onNodeWithTag("save_profile_button")
        .assertIsEnabled() // now the save profile button should be enabled
    composeTestRule.runOnUiThread { navController?.navigate(Screen.HOME) }
    // entering offline mode
    composeTestRule.runOnUiThread {
      navController?.navigate(
          Screen.OFFLINE) // this here forces us to navigate to the create_profile screen
    }
    // Verify "No Internet Connection" title is displayed
    composeTestRule.onNodeWithText("No Internet Connection").assertIsDisplayed()

    // Verify subtext message is displayed
    composeTestRule
        .onNodeWithText(
            "It seems like you don't have any WiFi connection... You can still practice offline!")
        .assertIsDisplayed()

    // Verify "Practice Offline" button is displayed
    composeTestRule.onNodeWithText("Practice Offline").assertIsDisplayed()

    composeTestRule.onNodeWithText("Practice Offline").performClick()
    verify(navigationActions).navigateTo(Screen.OFFLINE_INTERVIEW_MODULE)

    composeTestRule.runOnUiThread {
      navController?.navigate(
          Screen.OFFLINE_INTERVIEW_MODULE) // this here forces us to navigate to the create_profile
      // screen
    }
    composeTestRule.onNodeWithTag("company_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("job_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("question_field").assertIsDisplayed()
    composeTestRule.onNodeWithTag("doneButton").assertIsDisplayed()
    composeTestRule.onNodeWithText("What company are you applying to?").assertIsDisplayed()
    composeTestRule.onNodeWithText("What job are you applying to?").assertIsDisplayed()
    composeTestRule.onNodeWithText("Go to recording screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("company_field", useUnmergedTree = true).performTextInput("Apple")
    composeTestRule
        .onNodeWithTag("company_field", useUnmergedTree = true)
        .assertTextContains("Apple")

    // Input Job Position
    composeTestRule.onNodeWithTag("job_field", useUnmergedTree = true).performTextInput("Engineer")
    composeTestRule
        .onNodeWithTag("job_field", useUnmergedTree = true)
        .assertTextContains("Engineer")

    // Input necessary fields
    composeTestRule.onNodeWithTag("company_field").performTextInput("Apple")
    composeTestRule.onNodeWithTag("job_field").performTextInput("Engineer")

    // Click Done button

    composeTestRule.onNodeWithTag("question_field").performClick()
    composeTestRule
        .onNodeWithTag("dropdown_item_What are your strengths?", useUnmergedTree = true)
        .performClick()
    // Verify navigation to OfflineRecordingScreen with the correct question
    composeTestRule.onNodeWithTag("doneButton").performClick()
    verify(navigationActions).goToOfflineRecording(any())

    composeTestRule.runOnUiThread { navController?.navigate(Screen.OFFLINE_RECORDING_SCREEN) }

    composeTestRule.onNodeWithTag("OfflineRecordingScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BackButtonRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RecordingColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("MicIconContainer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DoneButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("mic_text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("targetCompany").assertIsDisplayed()
    composeTestRule.onNodeWithTag("QuestionText").assertIsDisplayed()

    // user goes back online to see his recordings
    composeTestRule.runOnUiThread { navController?.navigate(Screen.PROFILE) }
    composeTestRule.onNodeWithTag("offline_recordings_section").assertIsDisplayed()
    composeTestRule.onNodeWithTag("offline_recordings_section").performClick()
  }
}
