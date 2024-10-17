package com.github.se.orator.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen

/**
 * A composable function that displays the Speaking Practice module.
 *
 * @param navigationActions The navigation actions that can be performed.
 * @param screenTitle The title of the screen.
 * @param headerText The header text of the module.
 * @param inputs The list of input fields to be displayed.
 */
@Composable
fun SpeakingPracticeModule(
    navigationActions: NavigationActions,
    screenTitle: String,
    headerText: String,
    inputs: List<InputFieldData>
) {
  androidx.compose.material.Scaffold(
      modifier = Modifier.fillMaxSize().testTag("speakingPracticeScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = Color.White,
            contentColor = Color.Black,
            elevation = 4.dp,
            title = { Text(screenTitle) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Image(
                    painter = painterResource(id = R.drawable.back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(32.dp).testTag("back_button"))
              }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = headerText,
                  style = MaterialTheme.typography.h6,
                  modifier = Modifier.padding(16.dp).testTag("titleText"))

              Spacer(modifier = Modifier.height(45.dp))

              // Dynamically generated input fields based on the provided data
              inputs.forEach { input ->
                OutlinedTextField(
                    value = input.value,
                    onValueChange =
                        input
                            .onValueChange, // Correctly uses the lambda with an explicitly typed
                                            // String parameter
                    label = { Text(input.label) },
                    placeholder = { Text(input.placeholder) },
                    modifier =
                        Modifier.fillMaxWidth().height(input.height.dp).testTag(input.testTag))
              }

              // Get Started Button
              Button(
                  modifier =
                      Modifier.fillMaxWidth().padding(top = 100.dp).testTag("getStartedButton"),
                  onClick = {
                    // Custom action, can be customized for different modules
                    navigationActions.navigateTo(Screen.SPEAKING_SCREEN)
                  }) {
                    Text("Get Started")
                  }
            }
      })
}
