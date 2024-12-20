package com.github.se.orator.endtoend

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.model.theme.AppThemeViewModel
import com.github.se.orator.ui.friends.AddFriendsScreen
import com.github.se.orator.ui.friends.LeaderboardScreen
import com.github.se.orator.ui.friends.ViewFriendsScreen
import com.github.se.orator.ui.mainScreen.MainScreen
import com.github.se.orator.ui.mainScreen.OnlineScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.overview.ChatScreen
import com.github.se.orator.ui.overview.FeedbackScreen
import com.github.se.orator.ui.overview.SpeakingJobInterviewModule
import com.github.se.orator.ui.overview.SpeakingPublicSpeakingModule
import com.github.se.orator.ui.overview.SpeakingSalesPitchModule
import com.github.se.orator.ui.profile.EditProfileScreen
import com.github.se.orator.ui.profile.GraphStats
import com.github.se.orator.ui.profile.ProfileScreen
import com.github.se.orator.ui.settings.SettingsScreen
import com.github.se.orator.ui.speaking.SpeakingScreen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class LastEndToEndTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

  @Mock private lateinit var navigationActions: NavigationActions
  @Mock private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel
  @Mock private lateinit var mockThemeContext: Context
  private lateinit var appThemeViewModel: AppThemeViewModel
  @Mock private lateinit var mockSharedPreferences: SharedPreferences
  @Mock private lateinit var mockEditor: SharedPreferences.Editor
  private lateinit var data: AnalysisData
  private lateinit var speech: String
  @Mock private lateinit var speakingRepository: SpeakingRepository
  private lateinit var speakingViewModel: SpeakingViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  @Mock private lateinit var chatGPTService: ChatGPTService
  private lateinit var chatViewModel: ChatViewModel
  @Mock private lateinit var offlinePromptFunctions: OfflinePromptsFunctionsInterface

  private var navController: NavHostController? = null

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
          Screen.SPEAKING)

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    appThemeViewModel = AppThemeViewModel(mockThemeContext)
    apiLinkViewModel = ApiLinkViewModel()
    `when`(speakingRepository.analysisState)
        .thenReturn(MutableStateFlow(SpeakingRepository.AnalysisState.IDLE))

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

    `when`(speakingRepository.fileSaved).thenReturn(MutableStateFlow(true))

    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel.getUserProfile(testUserProfile.uid)
  }

  @Test
  fun lastEndToEndTest() {
    composeTestRule.setContent {
      navController = rememberNavController()

      NavHost(navController = navController!!, startDestination = Route.HOME) {

        // Main/home flow
        navigation(startDestination = Screen.HOME, route = Route.HOME) {
          composable(Screen.HOME) { MainScreen(navigationActions) }
          composable(Screen.ONLINE_SCREEN) { OnlineScreen(navigationActions) }
          composable(Screen.SPEAKING_JOB_INTERVIEW) {
            SpeakingJobInterviewModule(navigationActions, chatViewModel, apiLinkViewModel)
          }
          composable(Screen.SPEAKING_PUBLIC_SPEAKING) {
            SpeakingPublicSpeakingModule(navigationActions, chatViewModel, apiLinkViewModel)
          }
          composable(Screen.SPEAKING_SALES_PITCH) {
            SpeakingSalesPitchModule(navigationActions, chatViewModel, apiLinkViewModel)
          }
          composable(Screen.SPEAKING) {
            SpeakingScreen(navigationActions, speakingViewModel, apiLinkViewModel)
          }
          composable(Screen.CHAT_SCREEN) {
            ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
          }
          composable(Screen.FEEDBACK) {
            FeedbackScreen(
                chatViewModel = chatViewModel,
                userProfileViewModel = userProfileViewModel,
                apiLinkViewModel = apiLinkViewModel,
                navigationActions = navigationActions)
          }
        }

        // Friends flow
        navigation(startDestination = Screen.FRIENDS, route = Route.FRIENDS) {
          composable(Screen.FRIENDS) { ViewFriendsScreen(navigationActions, userProfileViewModel) }
        }

        // Profile flow
        navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
          composable(Screen.PROFILE) { ProfileScreen(navigationActions, userProfileViewModel) }

          composable(Screen.EDIT_PROFILE) {
            EditProfileScreen(navigationActions, userProfileViewModel)
          }
          composable(Screen.STAT) { GraphStats(navigationActions, userProfileViewModel) }
          composable(Screen.LEADERBOARD) {
            LeaderboardScreen(navigationActions, userProfileViewModel)
          }
          composable(Screen.ADD_FRIENDS) {
            AddFriendsScreen(navigationActions, userProfileViewModel)
          }
          composable(Screen.SETTINGS) {
            SettingsScreen(navigationActions, userProfileViewModel, appThemeViewModel)
          }
        }
      }
    }

    // Test elements of the main screen
    composeTestRule.onNodeWithText("Find your").assertExists()
    composeTestRule.onNodeWithText("practice mode").assertExists()

    // Test the practice modules
    composeTestRule
        .onNodeWithText("Prepare for an interview")
        .assertExists()
        .assertHasClickAction()
        .performClick()
    verify(navigationActions).navigateTo(Screen.SPEAKING_JOB_INTERVIEW)

    composeTestRule
        .onNodeWithText("Improve public speaking")
        .assertExists()
        .assertHasClickAction()
        .performClick()
    verify(navigationActions).navigateTo(Screen.SPEAKING_PUBLIC_SPEAKING)

    composeTestRule
        .onNodeWithText("Master sales pitches")
        .assertExists()
        .assertHasClickAction()
        .performClick()
    verify(navigationActions).navigateTo(Screen.SPEAKING_SALES_PITCH)

    // Test elements of the toolbar
    composeTestRule.onNodeWithText("Popular").assertExists().assertHasClickAction().performClick()
    verify(navigationActions, never()).navigateTo(Screen.HOME)

    composeTestRule.onNodeWithText("Online").assertExists().assertHasClickAction().performClick()
    verify(navigationActions).navigateTo(Screen.ONLINE_SCREEN)

    // Go to the speaking screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.SPEAKING_JOB_INTERVIEW) }
    composeTestRule.waitForIdle()

    // Test the speaking screen
    composeTestRule.onNodeWithText("Ace your next job interview").assertExists()

    composeTestRule.onNodeWithText("What is your target job position?").assertExists()
    composeTestRule.onNodeWithTag("targetPositionInput-Question").performClick()

    composeTestRule.onNodeWithText("Which company are you applying to?").assertExists()
    composeTestRule.onNodeWithTag("companyNameInput-Question").performClick()

    composeTestRule.onNodeWithText("What type of interview are you preparing for?").assertExists()
    composeTestRule.onNodeWithTag("companyNameInput-Question").performClick()

    composeTestRule.onNodeWithText("What is your experience level in this field?").assertExists()
    composeTestRule.onNodeWithTag("experienceLevelInput-Question").performClick()

    composeTestRule.onNodeWithText("Please provide the job description:").assertExists()
    composeTestRule.onNodeWithTag("jobDescriptionInput-Question").performClick()

    composeTestRule.onNodeWithText("What do you want to focus on the most?").assertExists()
    composeTestRule.onNodeWithTag("focusAreaInput-Question").performClick()

    composeTestRule.onNodeWithTag("getStartedButton").performClick()
    // The fields are not filled so no transition to the speaking screen
    verify(navigationActions, never()).navigateTo(Screen.SPEAKING)

    // Test the feedback screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.FEEDBACK) }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithText("Here's what you did well and where you can improve:")
        .assertExists()
    composeTestRule
        .onNodeWithText("You have successfully completed 0 sessions so far!")
        .assertExists()
    composeTestRule.onNodeWithText("Your Performance Metrics:").assertExists()
    composeTestRule.onNodeWithText("Talk Time (Seconds)").assertExists()
    composeTestRule.onNodeWithText("Pace").assertExists()

    composeTestRule.onNodeWithText("Try Again").assertExists().assertHasClickAction().performClick()
    verify(navigationActions).navigateTo(Screen.ONLINE_SCREEN)

    // Test the friends screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.FRIENDS) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("➕ Add a friend").assertExists().assertIsNotDisplayed()
    composeTestRule.onNodeWithText("⭐ Leaderboard").assertExists().assertIsNotDisplayed()

    composeTestRule.onNodeWithText("John Doe").assertExists().assertHasClickAction()
    composeTestRule.onNodeWithText("Jane Doe").assertExists().assertHasClickAction()

    composeTestRule.onNodeWithTag("viewFriendsMenuButton").assertExists().performClick()

    composeTestRule.onNodeWithText("My Friends").assertExists()

    // Test the add friends screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.ADD_FRIENDS) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Add a friend").assertExists()
    composeTestRule.onNodeWithText("Username").assertExists()
    composeTestRule.onNodeWithTag("addFriendSearchField").performTextInput("John Doe")

    // Test the leaderboard screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.LEADERBOARD) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Leaderboard").assertExists()
    composeTestRule.onNodeWithText("John Doe").assertExists()
    composeTestRule.onNodeWithText("Jane Doe").assertExists()
    composeTestRule.onNodeWithText("Mode : Speech").assertExists()
    composeTestRule.onNodeWithText("Metric : Ratio").assertExists()

    composeTestRule
        .onNodeWithTag("practiceModeSelector")
        .assertExists()
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithText("Interview").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("Negotiation").assertExists().assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("rankMetricSelector")
        .assertExists()
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    composeTestRule.onNodeWithText("Improvement").assertExists().assertIsDisplayed()
    composeTestRule.onNodeWithText("Success").assertExists().assertIsDisplayed()

    // Test the profile screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.PROFILE) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("sign_out_button").assertExists().performClick()
    verify(navigationActions).navigateTo(Screen.AUTH)

    composeTestRule.onNodeWithTag("edit_button").assertExists().performClick()
    verify(navigationActions).navigateTo(Screen.EDIT_PROFILE)

    composeTestRule.onNodeWithTag("settings_button").assertExists().performClick()
    verify(navigationActions).navigateTo(Screen.SETTINGS)

    composeTestRule.onNodeWithTag("offline_recordings_section").assertExists().performClick()
    verify(navigationActions).navigateTo(Screen.OFFLINE_RECORDING_PROFILE)

    composeTestRule.onNodeWithTag("statistics_section").assertExists().performClick()
    composeTestRule.onNodeWithText("My Stats").assertExists()
    composeTestRule.onNodeWithText("My Offline Recordings").assertExists()

    // Test the settings screen
    composeTestRule.runOnUiThread { navController?.navigate(Screen.SETTINGS) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("theme").assertExists().assertHasClickAction().performClick()
    composeTestRule.onNodeWithTag("settingsThemeDialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("Light").assertExists()
    composeTestRule.onNodeWithText("Dark").assertExists()
    composeTestRule.onNodeWithText("System default").assertExists()

    composeTestRule.onNodeWithText("Confirm").assertExists().assertHasClickAction()
    composeTestRule
        .onNodeWithTag("settingsThemeDialogCancel", useUnmergedTree = true)
        .assertExists()
        .performClick()

    composeTestRule.onNodeWithTag("settingsThemeDialog").assertIsNotDisplayed()

    composeTestRule.onNodeWithTag("permissions").assertExists().assertHasClickAction()
  }
}
