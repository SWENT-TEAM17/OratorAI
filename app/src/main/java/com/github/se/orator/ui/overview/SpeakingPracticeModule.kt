package com.github.se.orator.ui.overview

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensionsObject
import com.github.se.orator.ui.theme.AppTypography
import com.github.se.orator.ui.theme.createAppDimensions

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

  // Obtain responsive dimensions using the factory
  val dimensions: AppDimensionsObject = createAppDimensions()

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("speakingPracticeScreen"),
      topBar = {
        // Use CenterAlignedTopAppBar for consistency
        TopAppBar(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = dimensions.statusBarPadding)
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
                        modifier = Modifier.size(dimensions.iconSizeSmall),
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
                      .padding(horizontal = dimensions.paddingMedium)
                      .padding(top = dimensions.paddingSmall)
                      .verticalScroll(rememberScrollState())
                      .testTag("content"),
              verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)) {
                // Header text with consistent styling
                Text(
                    text = headerText,
                    style =
                        AppTypography.mediumTitleStyle.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary),
                    modifier =
                        Modifier.padding(vertical = dimensions.paddingMedium).testTag("titleText"))

                // Dynamically generated input fields based on the provided data
                inputs.forEach { input ->
                  androidx.compose.material.OutlinedTextField(
                      value = input.value,
                      onValueChange = input.onValueChange,
                      label = { androidx.compose.material.Text(input.label) },
                      placeholder = { androidx.compose.material.Text(input.placeholder) },
                      modifier =
                          Modifier.fillMaxWidth()
                              .height(
                                  input.height.dp) // Assuming 'height' is defined in InputFieldData
                              .testTag(input.testTag),
                      colors =
                          androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors(
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
                Spacer(modifier = Modifier.height(dimensions.paddingLarge))

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
                            .padding(top = dimensions.paddingMedium)
                            .border(
                                width = dimensions.borderStrokeWidth,
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
