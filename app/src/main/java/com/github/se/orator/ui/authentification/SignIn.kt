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

@Composable
fun SignInScreen(navigationActions: NavigationActions, viewModel: UserProfileViewModel) {
  val context = LocalContext.current
  var isLoading by remember { mutableStateOf(false) } // To handle loading state
  var redirectToProfile by remember {
    mutableStateOf(false)
  } // To handle redirection after profile fetch

  // Obtain responsive dimensions using your factory
  val dimensions: AppDimensionsObject = createAppDimensions()

  // Launcher for Google Sign-In
  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
            Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()

            // Start loading and fetch the user profile
            isLoading = true
            val uid = result.user?.uid

            // Fetch the user profile and check if it's incomplete
            uid?.let { u ->
              viewModel.getUserProfile(u)
              redirectToProfile = true
            }
          },
          onAuthError = {
            Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          })

  // Token for Google Sign-In
  val token = stringResource(R.string.default_web_client_id)

  // Start observing profile loading state and redirect based on profile completeness
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

  // UI for the sign-in screen
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        if (isLoading) {
          // Show loading spinner while fetching user profile data
          LoadingScreen(dimensions = dimensions)
        } else {
          // Show sign-in UI when not loading
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(padding)
                      .verticalScroll(rememberScrollState()) // Make content scrollable
                      .padding(horizontal = dimensions.paddingMedium),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
          ) {
            // App Logo Image
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(dimensions.logoSize).testTag("appLogo"))

            Text(
                text = "OratorAI",
                style = AppTypography.bigTitleStyle.copy(brush = AppColors.primaryGradient),
                modifier =
                    Modifier.width(dimensions.logoTextWidth)
                        .height(dimensions.logoTextHeight)
                        .testTag("appTitle"))

            Text(
                text = "Welcome !",
                style = AppTypography.smallTitleStyle,
                modifier = Modifier.testTag("welcomeText"))

            Spacer(modifier = Modifier.height(dimensions.paddingExtraLarge))

            // Authenticate With Google Button
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

@Composable
fun LoadingScreen(dimensions: AppDimensionsObject) {
  // Centering content and making it responsive to screen size
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
              // Load the Google logo from resources
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(dimensions.googleLogoSize)
                          .padding(end = dimensions.paddingSmall)
                          .testTag("googleLogo"))

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  style = AppTypography.buttonTextStyle,
                  modifier = Modifier.testTag("signInWithGoogleText"))
            }
      }
}

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
