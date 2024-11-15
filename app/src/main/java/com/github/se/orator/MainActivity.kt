package com.github.se.orator

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.SpeakingRepositoryRecord
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.network.NetworkConnectivityObserver
import com.github.se.orator.network.OfflineViewModel
import com.github.se.orator.ui.authentification.SignInScreen
import com.github.se.orator.ui.friends.AddFriendsScreen
import com.github.se.orator.ui.friends.LeaderboardScreen
import com.github.se.orator.ui.friends.ViewFriendsScreen
import com.github.se.orator.ui.mainScreen.MainScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.createChatGPTService
import com.github.se.orator.ui.offline.OfflinePracticeQuestionsScreen
import com.github.se.orator.ui.offline.OfflineRecordingScreen
import com.github.se.orator.ui.offline.OfflineScreen
import com.github.se.orator.ui.offline.RecordingReviewScreen
import com.github.se.orator.ui.overview.ChatScreen
import com.github.se.orator.ui.overview.FeedbackScreen
import com.github.se.orator.ui.overview.SpeakingJobInterviewModule
import com.github.se.orator.ui.overview.SpeakingPublicSpeakingModule
import com.github.se.orator.ui.overview.SpeakingSalesPitchModule
import com.github.se.orator.ui.profile.CreateAccountScreen
import com.github.se.orator.ui.profile.ProfileScreen
import com.github.se.orator.ui.settings.SettingsScreen
import com.github.se.orator.ui.speaking.SpeakingScreen
import com.github.se.orator.ui.theme.ProjectTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  private lateinit var networkConnectivityObserver: NetworkConnectivityObserver
  private val offlineViewModel: OfflineViewModel by viewModels() // Initialize the OfflineViewModel

  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let { auth.signOut() }

    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val apiKey = appInfo.metaData.getString("GPT_API_KEY")
    val organizationId = appInfo.metaData.getString("GPT_ORGANIZATION_ID")

    requireNotNull(apiKey) { "GPT API Key is missing in the manifest" }
    requireNotNull(organizationId) { "GPT Organization ID is missing in the manifest" }

    val chatGPTService = createChatGPTService(apiKey, organizationId)

    // Initialize NetworkChangeReceiver and register it
    networkConnectivityObserver = NetworkConnectivityObserver()
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    registerReceiver(networkConnectivityObserver, filter)

    // Observe network status updates and update offline mode in ViewModel
    lifecycleScope.launch {
      NetworkConnectivityObserver.isNetworkAvailable.collect { isConnected ->
        offlineViewModel.setOfflineMode(!isConnected) // Update offline mode status in ViewModel
      }
    }

    enableEdgeToEdge()
    setContent {
      ProjectTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) {
          val isOffline by offlineViewModel.isOffline.observeAsState(false)
          OratorApp(chatGPTService, isOffline)
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(networkConnectivityObserver) // Unregister receiver to prevent leaks
  }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OratorApp(chatGPTService: ChatGPTService, isOffline: Boolean) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  // Initialize required ViewModels
  val userProfileViewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModel.Factory)
  val apiLinkViewModel = ApiLinkViewModel()
  val speakingViewModel =
      SpeakingViewModel(SpeakingRepositoryRecord(LocalContext.current), apiLinkViewModel)
  val chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)

  // Monitor `isOffline` for state changes and navigate accordingly
  LaunchedEffect(isOffline) {
    if (isOffline) {
      // Navigate to the offline screen if the app goes offline
      navController.navigate(Screen.OFFLINE) { popUpTo(Screen.OFFLINE) { inclusive = true } }
    } else {
      // Navigate to the main flow if the app comes back online
      navController.navigate(Route.AUTH) { popUpTo(Route.AUTH) { inclusive = true } }
    }
  }

  Scaffold(modifier = Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = Route.AUTH) {

      // Offline flow
      composable(Screen.OFFLINE) { OfflineScreen(navigationActions) }
      composable(Screen.PRACTICE_QUESTIONS_SCREEN) {
        OfflinePracticeQuestionsScreen(navigationActions)
      }
      composable(Screen.OFFLINE_RECORDING_REVIEW_SCREEN) {
        RecordingReviewScreen(navigationActions, speakingViewModel)
      }
      composable(
          route = "offline_recording/{question}",
          arguments = listOf(navArgument("question") { type = NavType.StringType })) {
              backStackEntry ->
            val question = backStackEntry.arguments?.getString("question") ?: ""
            OfflineRecordingScreen(navigationActions, question, speakingViewModel)
          }

      // Online/auth flow
      navigation(startDestination = Screen.AUTH, route = Route.AUTH) {
        composable(Screen.AUTH) { SignInScreen(navigationActions, userProfileViewModel) }
        composable(Screen.CREATE_PROFILE) {
          CreateAccountScreen(navigationActions, userProfileViewModel)
        }
      }

      // Main/home flow
      navigation(startDestination = Screen.HOME, route = Route.HOME) {
        composable(Screen.HOME) { MainScreen(navigationActions) }
        composable(Screen.SPEAKING_JOB_INTERVIEW) {
          SpeakingJobInterviewModule(navigationActions, apiLinkViewModel)
        }
        composable(Screen.SPEAKING_PUBLIC_SPEAKING) {
          SpeakingPublicSpeakingModule(navigationActions, apiLinkViewModel)
        }
        composable(Screen.SPEAKING_SALES_PITCH) {
          SpeakingSalesPitchModule(navigationActions, apiLinkViewModel)
        }
        composable(Screen.SPEAKING) { SpeakingScreen(navigationActions, speakingViewModel) }
        composable(Screen.CHAT_SCREEN) {
          ChatScreen(navigationActions = navigationActions, chatViewModel = chatViewModel)
        }
        composable(Screen.FEEDBACK) {
          FeedbackScreen(chatViewModel = chatViewModel, navigationActions = navigationActions)
        }
      }

      // Friends flow
      navigation(startDestination = Screen.FRIENDS, route = Route.FRIENDS) {
        composable(Screen.FRIENDS) { ViewFriendsScreen(navigationActions, userProfileViewModel) }
      }

      // Profile flow
      navigation(startDestination = Screen.PROFILE, route = Route.PROFILE) {
        composable(Screen.PROFILE) { ProfileScreen(navigationActions, userProfileViewModel) }
        composable(Screen.LEADERBOARD) {
          LeaderboardScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.ADD_FRIENDS) { AddFriendsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.SETTINGS) { SettingsScreen(navigationActions, userProfileViewModel) }
      }
    }
  }
}
