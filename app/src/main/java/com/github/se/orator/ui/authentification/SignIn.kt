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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.navigation.TopLevelDestinations
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography
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
          LoadingScreen()
        } else {
          // Show sign-in UI when not loading
          Column(
              modifier = Modifier.fillMaxSize().padding(padding),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
          ) {
            // App Logo Image
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(AppDimensions.logoSize).testTag("appLogo") // Added testTag
                )

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

            Text(
                text = "OratorAI",
                style = AppTypography.largeTitleStyle.copy(brush = AppColors.primaryGradient),
                modifier =
                    Modifier.width(AppDimensions.logoTextWidth)
                        .height(AppDimensions.logoTextHeight)
                        .testTag("appTitle") // Added testTag
                )

            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

            Text(
                text = "Welcome !",
                style = AppTypography.mediumTitleStyle,
                modifier = Modifier.testTag("welcomeText") // Added testTag
                )

            Spacer(modifier = Modifier.height(AppDimensions.largeSpacerHeight))

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
                })
          }
        }
      })
}

@Composable
fun LoadingScreen() {
  // Show a loading indicator in the center of the screen
  Column(
      modifier =
          Modifier.fillMaxSize().padding(top = AppDimensions.paddingXXLarge)
              .testTag("loadingScreen"), // Optional: Add a testTag for the entire loading screen
      horizontalAlignment = Alignment.CenterHorizontally) {

      Image(
          painter = painterResource(id = R.drawable.loading_screen), // Replace with your actual image name
          contentDescription = "Loading Screen Image",
          modifier = Modifier
              .fillMaxWidth()
              .width(412.dp)
              .height(487.dp)
              .testTag("loadingImage")
      )

      Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))

      Text(
          text = "Reach your goals",
          style = AppTypography.mediumTitleStyle,
          modifier = Modifier.testTag("loadingText") // Added testTag
          )

      Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))

      Text(
          text = "Become the best speaker",
          style = AppTypography.smallTitleStyle,
          modifier = Modifier.testTag("loadingText") // Added testTag
          )

        Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

        CircularProgressIndicator(
            color = AppColors.loadingIndicatorColor,
            strokeWidth = AppDimensions.strokeWidth,
            modifier =
                Modifier.size(AppDimensions.loadingIndicatorSize)
                    .testTag("loadingIndicator") // Added testTag
            )
        Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
        Text(
            text = "Loading...",
            style = AppTypography.loadingTextStyle,
            modifier = Modifier.testTag("loadingText") // Added testTag
            )
      }
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = AppColors.buttonBackgroundColor),
      shape = RoundedCornerShape(50), // Circular edges for the button
      border = BorderStroke(AppDimensions.borderStrokeWidth, AppColors.buttonBorderColor),
      modifier =
          Modifier.padding(AppDimensions.paddingSmall)
              .height(AppDimensions.buttonHeight)
              .testTag("loginButton") // Existing testTag
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter =
                      painterResource(id = R.drawable.google_logo), // Ensure this drawable exists
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(AppDimensions.googleLogoSize)
                          .padding(end = AppDimensions.paddingSmall)
                          .testTag("googleLogo") // Added testTag
                  )

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  style = AppTypography.buttonTextStyle,
                  modifier = Modifier.testTag("signInWithGoogleText") // Added testTag
                  )
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
