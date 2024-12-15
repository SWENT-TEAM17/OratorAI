package com.github.se.orator.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.theme.AppThemeViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.TopNavigationMenu
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.AppTypography

// class for all that is needed about a section for settings
data class SettingBar(
    val text: String,
    val testTag: String,
    val icon: ImageVector,
    val iconDescription: String
)

// creating a list of all settings to implement them with a simple loop
val listOfSettings =
    listOf(
        // Redirects to the app's page in the device settings.
        SettingBar("Permissions", "permissions", Icons.Outlined.Lock, "lock icon"),
        // Switches the app's theme between light and dark mode.
        SettingBar("Theme", "theme", Icons.Outlined.DarkMode, "theme"))

// reusable function that is called to add a section to settings
@Composable
fun TextButtonFun(settingBar: SettingBar, onClick: () -> Unit = {}) {
  TextButton(
      onClick = { onClick() },
      modifier = Modifier.fillMaxWidth().testTag(settingBar.testTag),
      contentPadding = PaddingValues(AppDimensions.nullPadding) // Remove default padding
      ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppDimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start) {
              Icon(
                  imageVector = settingBar.icon,
                  contentDescription = settingBar.iconDescription,
                  modifier = Modifier.size(AppDimensions.iconSizeLarge).testTag("icon"))

              Text(
                  modifier =
                      Modifier.padding(
                          start = AppDimensions.paddingSmallMedium,
                          top = AppDimensions.paddingTopSmall),
                  text = settingBar.text,
                  color = MaterialTheme.colorScheme.onBackground,
                  fontSize = AppFontSizes.titleLarge)
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel,
    themeViewModel: AppThemeViewModel? = null
) {
  val context = LocalContext.current
  Scaffold(
      topBar = {
          TopNavigationMenu(
              title = {
                  Text(
                      "Settings",
                      modifier = Modifier.testTag("SettingsText"),
                      color = MaterialTheme.colorScheme.onSurface,
                      style = AppTypography.mediumTopBarStyle
                  )
              },
              navigationIcon = {
                  IconButton(
                      onClick = { navigationActions.goBack() },
                      modifier = Modifier.testTag("back_button")) {
                      androidx.compose.material.Icon(
                          Icons.Outlined.ArrowBackIosNew,
                          contentDescription = "Back button",
                          modifier = Modifier.size(AppDimensions.iconSizeMedium),
                          tint = MaterialTheme.colorScheme.onSurface)
                  }
              },
          )
      }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("settingsScreen"),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.spacerWidthMedium)) {

              // Permissions
              item {
                TextButtonFun(listOfSettings[0]) {
                  context.startActivity(
                      Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                      })
                }
              }
              item {
                TextButtonFun(listOfSettings[1]) {
                  themeViewModel?.switchTheme()
                  Log.d("SettingsScreen", "Theme switch")
                }
              }
            }
      }
}
