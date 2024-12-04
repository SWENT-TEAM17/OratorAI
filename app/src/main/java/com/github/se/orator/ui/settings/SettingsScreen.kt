package com.github.se.orator.ui.settings

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.AppThemeViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes

// class for all that is needed about a section for settings
data class SettingBar(
    val text: String,
    val testTag: String,
    val function: (AppThemeViewModel?) -> Unit,
    val icon: ImageVector,
    val iconDescription: String
)

// creating a list of all settings to implement them with a simple loop
val listOfSettings =
    listOf(
        SettingBar(
            "Account Management",
            "account_management",
            { Log.d("hello", "account management") },
            Icons.Outlined.AccountCircle,
            "account circle icon"),
        SettingBar(
            "Storage Settings",
            "storage_settings",
            { Log.d("hello", "storage settings") },
            Icons.AutoMirrored.Outlined.List,
            "storage settings icon"),
        SettingBar(
            "Permissions",
            "permissions",
            { Log.d("hello", "permissions") },
            Icons.Outlined.Lock,
            "lock icon"),
        SettingBar(
            "Theme (click to switch)",
            "theme",
            { themeVM ->
              themeVM?.switchTheme()
              Log.d("hello", "theme")
            },
            Icons.Outlined.DarkMode,
            "theme"),
        SettingBar(
            "Invite Friends",
            "invite_friends",
            { Log.d("hello", "invite friends") },
            Icons.Outlined.Mail,
            "mail icon"),
        SettingBar(
            "Notifications",
            "notifications",
            { Log.d("hello", "Notifications") },
            Icons.Outlined.Notifications,
            "notifications icon"),
        SettingBar(
            "Rate on the App Store",
            "rate_on_the_app_store",
            { Log.d("hello", "rate on the app store") },
            Icons.Outlined.Star,
            "star icon"),
        SettingBar("About", "about", { Log.d("hello", "about") }, Icons.Outlined.Info, "info icon"))

// reusable function that is called to add a section to settings
@Composable
fun TextButtonFun(settingBar: SettingBar, switchTheme: AppThemeViewModel? = null) {
  TextButton(
      onClick = { settingBar.function(switchTheme) },
      modifier = Modifier.fillMaxWidth().testTag(settingBar.testTag),
      contentPadding = PaddingValues(0.dp) // Remove default padding
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
  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Settings",
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag("SettingsText"))
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
            colors =
                TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ))
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("settingsScreen"),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.spacerWidthMedium)) {
              for (setting in listOfSettings) {
                TextButtonFun(setting, themeViewModel)
                HorizontalDivider(thickness = AppDimensions.dividerThickness)
              }
            }
      })
}
