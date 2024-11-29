package com.github.se.orator.ui.offline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes

@Composable
fun OfflineScreen(navigationActions: NavigationActions) {
  val colors = MaterialTheme.colorScheme

  // Background gradient
  Box(
      modifier =
          Modifier.fillMaxSize()
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              colors.background,
                              colors.primary.copy(alpha = 0.1f),
                              colors.secondary.copy(alpha = 0.1f))))
              .padding(WindowInsets.systemBars.asPaddingValues())
              .testTag("OfflineScreen")) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = AppDimensions.paddingMedium)
                    .testTag("OfflineScreenColumn"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Icon with a circular background
              Box(
                  modifier =
                      Modifier.size(AppDimensions.profilePictureDialogSize)
                          .background(colors.primary.copy(alpha = 0.1f), CircleShape)
                          .padding(AppDimensions.paddingLarge)
                          .testTag("OfflineScreenIconContainer")) {
                  Icon(
                      Icons.Filled.WifiOff,
                      contentDescription = "No connection",
                      modifier =
                      Modifier.size(AppDimensions.iconSize)
                          .align(Alignment.Center)
                          .testTag("NoConnectionIcon"),
                      tint = colors.primary
                  )
                  }

              Spacer(modifier = Modifier.height(AppDimensions.spacerHeightDefault))

              // Main text
              Text(
                  text = "No Internet Connection",
                  fontSize = AppFontSizes.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = colors.onBackground,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("NoConnectionText"))

              Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))

              // Subtext
              Text(
                  text =
                      "It seems like you don't have any WiFi connection... You can still practice offline!",
                  fontSize = AppFontSizes.bodyLarge,
                  fontWeight = FontWeight.Medium,
                  color = colors.onBackground.copy(alpha = 0.7f),
                  textAlign = TextAlign.Center,
                  modifier =
                      Modifier.padding(horizontal = AppDimensions.paddingMedium).testTag("SubText"))

              Spacer(modifier = Modifier.height(AppDimensions.spacerHeightDefault))

              // Button
              Button(
                  onClick = {
                    Log.d("offline screen", "Practice Offline button clicked")
                    navigationActions.navigateTo(Screen.OFFLINE_INTERVIEW_MODULE)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                  shape = RoundedCornerShape(AppDimensions.buttonHeightRounded),
                  modifier =
                      Modifier.height(AppDimensions.buttonHeightRounded)
                          .widthIn(
                              min = AppDimensions.buttonWidthMin,
                              max = AppDimensions.buttonWidthMax)
                          .shadow(
                              AppDimensions.shadowElevation,
                              RoundedCornerShape(AppDimensions.buttonHeightRounded))
                          .testTag("PracticeOfflineButton")) {
                    Text(
                        text = "Practice Offline",
                        color = colors.onPrimary,
                        fontSize = AppFontSizes.subtitle,
                        fontWeight = FontWeight.Bold)
                  }
            }
      }
}
