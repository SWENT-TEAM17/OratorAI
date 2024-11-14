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
            isLoading = true
            val uid = result.user?.uid
            uid?.let { u ->
              viewModel.getUserProfile(u)
              redirectToProfile = true
            }
          },
          onAuthError = {
            Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          })

  val token = stringResource(R.string.default_web_client_id)

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

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        if (isLoading) {
          LoadingScreen()
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(padding)
                      .verticalScroll(rememberScrollState())
                      .padding(horizontal = dimensions.paddingMedium),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Spacer(modifier = Modifier.weight(1f)) // Spacer to push content to the center

            // Centered Content (Logo and Texts)
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

            Spacer(modifier = Modifier.weight(1f)) // Spacer to push content to the center

            // Bottom Content (Google Sign-In Button)
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
fun LoadingScreen() {
  val dimensions: AppDimensionsObject = createAppDimensions()
  // Show a loading indicator in the center of the screen
  Column(
      modifier = Modifier.fillMaxSize().testTag("loadingScreen"),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
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
