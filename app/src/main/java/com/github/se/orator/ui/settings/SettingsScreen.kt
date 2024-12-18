package com.github.se.orator.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.theme.AppThemeValue
import com.github.se.orator.model.theme.AppThemeViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import java.util.Locale

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
                if (themeViewModel != null) {
                  val themeDialogIsOpen = remember { mutableStateOf(false) }

                  TextButtonFun(listOfSettings[1]) { themeDialogIsOpen.value = true }
                  when {
                    themeDialogIsOpen.value ->
                        ThemeSwitchDialog(
                            onDismissRequest = { themeDialogIsOpen.value = false },
                            appThemeViewModel = themeViewModel)
                  }
                }
              }
            }
      }
}

/**
 * Dialog for switching the app theme
 *
 * @param onDismissRequest: Function called on dialog dismiss
 * @param appThemeViewModel: ViewModel for the app theme
 */
@Composable
private fun ThemeSwitchDialog(onDismissRequest: () -> Unit, appThemeViewModel: AppThemeViewModel) {
  // Radio options for the theme switch dialog
  val radioOptions = AppThemeValue.entries.toTypedArray()
  // Selected option for the theme switch dialog and the setter to update it
  val (selectedOption, onOptionSelected) =
      remember { mutableStateOf(appThemeViewModel.currentTheme.value) }

  AlertDialog(
      modifier = Modifier.testTag("settingsThemeDialog"),
      icon = {
        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = "theme",
            modifier =
                Modifier.size(AppDimensions.iconSizeLarge).testTag("settingsThemeDialogIcon"))
      },
      title = {
        Text(
            text = "Select a theme",
            modifier = Modifier.testTag("settingsThemeDialogTitle"),
            color = MaterialTheme.colorScheme.onSurface)
      },
      // The radio buttons
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          radioOptions.forEach { option ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .selectable(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) })
                        .testTag("settingsThemeDialogRow#$option"),
                verticalAlignment = Alignment.CenterVertically) {
                  RadioButton(
                      selected = (option == selectedOption), onClick = { onOptionSelected(option) })
                  Text(
                      text = formatThemeName(option),
                      modifier = Modifier.testTag("settingsThemeDialogText#$option"),
                      fontSize = AppFontSizes.bodyLarge,
                      color = MaterialTheme.colorScheme.onSurface,
                  )
                }
          }
        }
      },
      onDismissRequest = onDismissRequest,
      // Theme is updated only when the confirm button is clicked
      confirmButton = {
        TextButton(
            onClick = {
              appThemeViewModel.saveTheme(selectedOption)
              onDismissRequest()
            }) {
              Text(
                  "Confirm",
                  modifier = Modifier.testTag("settingsThemeDialogConfirm"),
                  color = MaterialTheme.colorScheme.primary)
            }
      },
      dismissButton = {
        TextButton(onClick = onDismissRequest) {
          Text(
              "Cancel",
              modifier = Modifier.testTag("settingsThemeDialogCancel"),
              color = MaterialTheme.colorScheme.primary)
        }
      })
}

/**
 * Format the theme name to be displayed in the theme switch dialog.
 *
 * @param themeValue: The theme value to format
 * @return The formatted theme name
 */
private fun formatThemeName(themeValue: AppThemeValue): String {
  return themeValue.toString().replace("_", " ").lowercase(Locale.ROOT).replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
  }
}
