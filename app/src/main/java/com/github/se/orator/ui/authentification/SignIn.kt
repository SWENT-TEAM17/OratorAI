// File: SignInScreen.kt
package com.github.se.orator.ui.authentification

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensionsObject
import com.github.se.orator.ui.theme.AppTypography
import com.github.se.orator.ui.theme.createAppDimensions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Composable function that displays the Sign-In screen. It includes Google Sign-In functionality,
 * which triggers navigation based on profile completeness.
 *
 * @param navigationActions The actions to handle navigation events.
 * @param viewModel The UserProfileViewModel used for managing user profile data.
 */
@Composable
fun SignInScreen(navigationActions: NavigationActions, viewModel: UserProfileViewModel) {
  val context = LocalContext.current
  var isLoading by remember { mutableStateOf(false) } // To handle loading state
  var redirectToProfile by remember {
    mutableStateOf(false)
  } // To handle redirection after profile fetch

  // Obtain responsive dimensions for layout styling
  val dimensions: AppDimensionsObject = createAppDimensions()

  // Google Sign-In launcher that manages the sign-in process and handles authentication results
  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
            Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
            isLoading = true // Show loading screen
            val uid = result.user?.uid
            uid?.let { u ->
              viewModel.getUserProfile(u) // Fetch user profile
              redirectToProfile = true
            }
          },
          onAuthError = {
            Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          })

  // Token required for Google Sign-In
  val token = stringResource(R.string.default_web_client_id)

  // Observe profile loading state and navigate based on profile completeness
  LaunchedEffect(viewModel.isLoading.collectAsState().value, redirectToProfile) {
    if (!viewModel.isLoading.value && redirectToProfile) {
      isLoading = false
      if (!viewModel.isProfileIncomplete()) {
        navigationActions.navigateTo(TopLevelDestinations.HOME)
      } else {
        navigationActions.navigateTo(Screen.CREATE_PROFILE)
      }
    }
  }

  // Main UI for Sign-In Screen
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        if (isLoading) {
          LoadingScreen() // Display loading screen when fetching profile
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(padding)
                      .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                      .padding(horizontal = dimensions.paddingMedium),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Spacer(modifier = Modifier.weight(1f)) // Spacer to center content

            // Centered Content: Logo, Title, and Welcome Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)) {
                  Image(
                      painter = painterResource(id = R.drawable.app_logo),
                      contentDescription = "App Logo",
                      modifier = Modifier.size(dimensions.logoSize).testTag("appLogo"))

                  Text(
                      text = "OratorAI",
                      style = AppTypography.largeTitleStyle.copy(brush = AppColors.primaryGradient),
                      modifier =
                          Modifier.width(dimensions.logoTextWidth)
                              .height(dimensions.logoTextHeight)
                              .testTag("appTitle"))

                  Text(
                      text = "Welcome !",
                      style = AppTypography.mediumTitleStyle,
                      modifier = Modifier.testTag("welcomeText"))
                }

            Spacer(modifier = Modifier.weight(1f)) // Spacer to center content

            // Google Sign-In Button at the Bottom
            GoogleSignInButton(
                onSignInClick = {
                  val gso =
                      GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                          .requestIdToken(token)
                          .requestEmail()
                          .build()
                  val googleSignInClient = GoogleSignIn.getClient(context, gso)
                  launcher.launch(googleSignInClient.signInIntent)
                },
                dimensions = dimensions)
          }
        }
      })
}

/**
 * Composable function that displays a loading screen with a circular progress indicator and a
 * "Loading..." text, used during user profile loading.
 */
@Composable
fun LoadingScreen() {
    // Centering content and making it responsive to screen size
    val dimensions: AppDimensionsObject = createAppDimensions()
    Column(
        modifier =
        Modifier.fillMaxSize()
            .padding(top = dimensions.paddingXXLarge)
            .wrapContentSize(Alignment.Center) // Centers the content on screen
            .testTag("loadingScreen"),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.loading_screen), // Your image resource
            contentDescription = "Loading Screen Image",
            modifier =
            Modifier.fillMaxWidth(0.8f) // Limits the image width to 80% of the screen width
                .aspectRatio(1f) // Maintains aspect ratio for better fit
                .wrapContentHeight() // Wraps height to content size
                .testTag("loadingImage"))

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        Text(
            text = "Reach your goals",
            style = AppTypography.mediumTitleStyle,
            modifier = Modifier.testTag("loadingText"))

        Spacer(modifier = Modifier.height(dimensions.paddingSmall))

        Text(
            text = "Become the best speaker",
            style = AppTypography.smallTitleStyle,
            modifier = Modifier.testTag("loadingText"))

        Spacer(modifier = Modifier.height(dimensions.paddingLarge))

        CircularProgressIndicator(
            color = AppColors.loadingIndicatorColor,
            strokeWidth = dimensions.strokeWidth,
            modifier = Modifier.size(dimensions.loadingIndicatorSize).testTag("loadingIndicator"))

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        Text(
            text = "Loading...",
            style = AppTypography.loadingTextStyle,
            modifier = Modifier.testTag("loadingText"))
    }
}

/**
 * Composable function for the Google Sign-In button. Provides a customizable button with Google
 * logo and sign-in text.
 *
 * @param onSignInClick Lambda to execute when the button is clicked.
 * @param dimensions The dimensions for styling the button.
 */
@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit, dimensions: AppDimensionsObject) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = AppColors.buttonBackgroundColor),
      shape = RoundedCornerShape(50),
      border = BorderStroke(dimensions.borderStrokeWidth, AppColors.buttonBorderColor),
      modifier =
          Modifier.fillMaxWidth()
              .padding(dimensions.paddingSmall)
              .height(dimensions.buttonHeight)
              .testTag("loginButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(dimensions.googleLogoSize)
                          .padding(end = dimensions.paddingSmall)
                          .testTag("googleLogo"))
              Text(
                  text = "Sign in with Google",
                  style = AppTypography.buttonTextStyle,
                  modifier = Modifier.testTag("signInWithGoogleText"))
            }
      }
}

/**
 * Utility function that sets up and returns a ManagedActivityResultLauncher for Google Sign-In.
 * This launcher handles the sign-in intent and processes the result.
 *
 * @param onAuthComplete Callback for successful authentication.
 * @param onAuthError Callback for authentication errors.
 * @return ManagedActivityResultLauncher that launches the Google Sign-In intent.
 */
@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()
  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
      scope.launch {
        val authResult = Firebase.auth.signInWithCredential(credential).await()
        onAuthComplete(authResult)
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}
