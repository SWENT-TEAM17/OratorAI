package com.github.se.orator.ui.overview

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

/**
 * The SpeakingPracticeModule composable displays the speaking practice module screen.
 *
 * @param navigationActions The navigation actions that can be performed.
 * @param screenTitle The title of the screen.
 * @param headerText The header text.
 * @param inputs The input fields.
 * @param onGetStarted The action to perform when the Get Started button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingPracticeModule(
    navigationActions: NavigationActions,
    screenTitle: String,
    headerText: String,
    inputs: List<InputFieldData>,
    onGetStarted: () -> Unit
) {
  val context = LocalContext.current

  // Obtain responsive AppDimensions using the factory

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("speakingPracticeScreen"),
      topBar = {
        // Use CenterAlignedTopAppBar for consistency
        TopAppBar(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = AppDimensions.statusBarPadding)
                    .testTag("topAppBar"),
            title = {
              Text(
                  text = screenTitle,
                  style = AppTypography.appBarTitleStyle.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag("screenTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(AppDimensions.iconSizeSmall),
                        tint = MaterialTheme.colorScheme.onSurface)
                  }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
          )
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(horizontal = AppDimensions.paddingMedium)
                      .padding(top = AppDimensions.paddingSmall)
                      .verticalScroll(rememberScrollState())
                      .testTag("content"),
              verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall)) {
                // Header text with consistent styling
                Text(
                    text = headerText,
                    style =
                        AppTypography.mediumTitleStyle.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary),
                    modifier =
                        Modifier.padding(vertical = AppDimensions.paddingMedium)
                            .testTag("titleText"))

                // Dynamically generated input fields based on the provided data
                inputs.forEach { input ->
                  OutlinedTextField(
                      value = input.value,
                      onValueChange = input.onValueChange,
                      label = { Text(input.label) },
                      placeholder = { Text(input.placeholder) },
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(
                                  input.height.dp) // Assuming 'height' is defined in InputFieldData
                              .testTag(input.testTag),
                      colors =
                          TextFieldDefaults.outlinedTextFieldColors(
                              backgroundColor = MaterialTheme.colorScheme.surface,
                              textColor = MaterialTheme.colorScheme.onSurface,
                              focusedBorderColor = MaterialTheme.colorScheme.outline,
                              unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                              cursorColor = MaterialTheme.colorScheme.primary,
                              focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                              unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                              placeholderColor =
                                  MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                          ))
                }

                // Spacer to add space before the button
                Spacer(modifier = Modifier.height(AppDimensions.paddingLarge))

                // Get Started Button with consistent styling
                Button(
                    onClick = {
                      // Custom action, can be customized for different modules
                      if (inputs.all { it.value.isNotEmpty() }) {
                        onGetStarted()
                      } else {
                        Toast.makeText(context, "Please fill all the fields!", Toast.LENGTH_SHORT)
                            .show()
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(top = AppDimensions.paddingMedium)
                            .border(
                                width = AppDimensions.borderStrokeWidth,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.medium)
                            .testTag("getStartedButton"),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                      Text(
                          "Get Started",
                          modifier = Modifier.testTag("getStartedText"),
                          color = MaterialTheme.colorScheme.primary)
                    }
              }
        }
      })
}
