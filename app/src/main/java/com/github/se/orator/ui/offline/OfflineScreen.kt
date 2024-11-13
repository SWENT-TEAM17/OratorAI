package com.github.se.orator.ui.offline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

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
              .testTag("OfflineScreen") // Tag for the entire screen
      ) {
        Column(
            modifier =
                Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("OfflineScreenColumn"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Icon with a circular background
              Box(
                  modifier =
                      Modifier.size(140.dp)
                          .background(colors.primary.copy(alpha = 0.1f), CircleShape)
                          .padding(20.dp)
                          .testTag("OfflineScreenIconContainer")) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_no_wifi2),
                        contentDescription = "No connection",
                        modifier =
                            Modifier.size(100.dp)
                                .align(Alignment.Center)
                                .testTag("NoConnectionIcon"),
                        tint = colors.primary)
                  }

              Spacer(modifier = Modifier.height(24.dp))

              // Main text
              Text(
                  text = "No Internet Connection",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = colors.onBackground,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.testTag("NoConnectionText"))

              Spacer(modifier = Modifier.height(8.dp))

              // Subtext
              Text(
                  text =
                      "It seems like you don't have any WiFi connection... You can still practice offline!",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium,
                  color = colors.onBackground.copy(alpha = 0.7f),
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(horizontal = 16.dp).testTag("SubText"))

              Spacer(modifier = Modifier.height(32.dp))

              // Button
              Button(
                  onClick = {
                    Log.d("offline screen", "Practice Offline button clicked")
                    navigationActions.navigateTo(Screen.PRACTICE_QUESTIONS_SCREEN)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                  shape = RoundedCornerShape(50),
                  modifier =
                      Modifier.height(50.dp)
                          .widthIn(min = 200.dp, max = 300.dp)
                          .shadow(5.dp, RoundedCornerShape(50))
                          .testTag("PracticeOfflineButton")) {
                    Text(
                        text = "Practice Offline",
                        color = colors.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold)
                  }
            }
      }
}
