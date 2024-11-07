package com.github.se.orator.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppTypography

/**
 * The SpeakingPracticeModule composable displays the speaking practice module.
 *
 * @param navigationActions The navigation actions that can be performed.
 * @param screenTitle The title of the screen.
 * @param headerText The header text.
 * @param inputs The input fields.
 * @param onGetStarted The action to perform when the Get Started button is clicked.
 */
@Composable
fun SpeakingPracticeModule(
    navigationActions: NavigationActions,
    screenTitle: String,
    headerText: String,
    inputs: List<InputFieldData>,
    onGetStarted: () -> Unit
) {
  androidx.compose.material.Scaffold(
      modifier = Modifier.fillMaxSize().testTag("speakingPracticeScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().testTag("topAppBar"),
            backgroundColor = AppColors.surfaceColor,
            contentColor = AppColors.textColor,
            elevation = AppDimensions.elevationSmall,
            title = {
              Text(
                  screenTitle,
                  style = AppTypography.appBarTitleStyle,
                  modifier = Modifier.testTag("screenTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("back_button")) {
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Back",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeSmall).testTag("back_button"))
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(AppDimensions.paddingMedium)
                    .padding(paddingValues)
                    .testTag("content"),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall)) {
              Text(
                  text = headerText,
                  style = AppTypography.mediumTitleStyle,
                  modifier = Modifier.padding(AppDimensions.paddingMedium).testTag("titleText"))

              Spacer(modifier = Modifier.height(AppDimensions.spacerHeightLarge))

              // Dynamically generated input fields based on the provided data
              inputs.forEach { input ->
                OutlinedTextField(
                    value = input.value,
                    onValueChange = input.onValueChange,
                    label = { Text(input.label) },
                    placeholder = { Text(input.placeholder) },
                    modifier =
                        Modifier.fillMaxWidth().height(input.height.dp).testTag(input.testTag))
              }

              // Get Started Button
              Button(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = AppDimensions.paddingExtraLarge)
                          .testTag("getStartedButton"),
                  onClick = {
                    // Custom action, can be customized for different modules
                    onGetStarted()
                  }) {
                    Text("Get Started", modifier = Modifier.testTag("getStartedText"))
                  }
            }
      })
}
