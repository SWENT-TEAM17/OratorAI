package com.github.se.orator.ui.overview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppDimensionsObject
import com.github.se.orator.ui.theme.AppTypography
import com.github.se.orator.ui.theme.createAppDimensions
import kotlin.math.roundToInt

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
          Divider()
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
                  if (input.isDropdown) {
                    // Handle dropdown input
                    FocusAreaDropdown(
                        selectedFocusArea = input.value,
                        onFocusAreaSelected = input.onValueChange,
                        question = input.question,
                        placeholder = input.placeholder,
                        testTag = input.testTag,
                        dropdownItems = input.dropdownItems)
                  } else {
                    // Display question as Text
                    Text(
                        text = input.question,
                        style = AppTypography.smallTitleStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                            Modifier.padding(vertical = AppDimensions.paddingSmall)
                                .testTag("${input.testTag}-Question"))

                    if (input.isScrollable) {
                      // Use a scrollable TextField
                      val scrollState = rememberScrollState()
                      var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                      val density = LocalDensity.current
                      val heightPx = with(density) { input.height.toPx() }

                      LaunchedEffect(input.value, textLayoutResult) {
                        val layoutResult = textLayoutResult ?: return@LaunchedEffect

                        // Get the cursor position
                        val cursorRect = layoutResult.getCursorRect(input.value.length)
                        val cursorOffset = cursorRect.topLeft.y

                        val visibleAreaStart = scrollState.value.toFloat()
                        val visibleAreaEnd = scrollState.value.toFloat() + heightPx

                        if (cursorOffset < visibleAreaStart) {
                          // Cursor is above visible area, scroll up
                          scrollState.scrollTo(cursorOffset.roundToInt())
                        } else if (cursorOffset + cursorRect.height > visibleAreaEnd) {
                          // Cursor is below visible area, scroll down
                          val scrollTo = (cursorOffset + cursorRect.height - heightPx).roundToInt()
                          scrollState.scrollTo(scrollTo)
                        }
                      }

                      Box(
                          modifier =
                              Modifier.fillMaxWidth()
                                  .height(input.height)
                                  .border(
                                      width = AppDimensions.borderStrokeWidth,
                                      color = MaterialTheme.colorScheme.onSurface,
                                      shape = MaterialTheme.shapes.small)
                                  .background(MaterialTheme.colorScheme.onPrimary)
                                  .verticalScroll(scrollState)
                                  .testTag("${input.testTag}-TextFieldBox")) {
                            BasicTextField(
                                value = input.value,
                                onValueChange = input.onValueChange,
                                modifier =
                                    Modifier.fillMaxSize().padding(AppDimensions.paddingSmall),
                                textStyle =
                                    LocalTextStyle.current.copy(
                                        color = MaterialTheme.colorScheme.onSurface),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                                onTextLayout = { layoutResult -> textLayoutResult = layoutResult },
                                decorationBox = { innerTextField ->
                                  Box(modifier = Modifier.fillMaxSize()) {
                                    if (input.value.isEmpty()) {
                                      Text(
                                          text = input.placeholder,
                                          style =
                                              LocalTextStyle.current.copy(
                                                  color =
                                                      MaterialTheme.colorScheme.onSurfaceVariant
                                                          .copy(alpha = 0.5f)),
                                          modifier = Modifier.padding(AppDimensions.paddingSmall))
                                    }
                                    innerTextField()
                                  }
                                },
                                singleLine = false)
                          }
                    } else {
                      // Display regular TextField
                      TextField(
                          value = input.value,
                          onValueChange = input.onValueChange,
                          placeholder = {
                            Text(
                                text = input.placeholder,
                                color = MaterialTheme.colorScheme.onSurface)
                          },
                          modifier =
                              Modifier.fillMaxWidth()
                                  .height(input.height)
                                  .border(
                                      width = AppDimensions.borderStrokeWidth,
                                      color = MaterialTheme.colorScheme.primary,
                                      shape = MaterialTheme.shapes.small)
                                  .testTag("${input.testTag}-TextField"),
                          colors =
                              TextFieldDefaults.textFieldColors(
                                  containerColor = MaterialTheme.colorScheme.surface,
                                  cursorColor = MaterialTheme.colorScheme.primary,
                                  focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                  unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                  focusedPlaceholderColor =
                                      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                  unfocusedPlaceholderColor =
                                      MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                              ),
                          keyboardOptions =
                              KeyboardOptions.Default.copy(imeAction = ImeAction.Next))
                    }
                  }
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

/**
 * The FocusAreaDropdown composable displays a dropdown for selecting a focus area.
 *
 * @param selectedFocusArea The selected focus area.
 * @param onFocusAreaSelected The callback to be invoked when a focus area is selected.
 * @param question The question to be displayed above the dropdown.
 * @param placeholder The placeholder text to be displayed in the dropdown.
 * @param testTag The test tag for the dropdown.
 * @param dropdownItems The items to be displayed in the dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusAreaDropdown(
    selectedFocusArea: String,
    onFocusAreaSelected: (String) -> Unit,
    question: String,
    placeholder: String,
    testTag: String,
    dropdownItems: List<String>
) {
  var expanded by remember { mutableStateOf(false) }

  // Display the question as Text
  Text(
      text = question,
      style = AppTypography.smallTitleStyle.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      modifier =
          Modifier.padding(vertical = AppDimensions.paddingSmall).testTag("$testTag-Question"))

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { expanded = !expanded },
      modifier = Modifier.testTag("$testTag-DropdownBox")) {
        TextField(
            value = selectedFocusArea,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onSurface) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier.menuAnchor()
                    .fillMaxWidth()
                    .border(
                        width = AppDimensions.borderStrokeWidth,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.small)
                    .testTag("$testTag-DropdownField"),
            colors =
                TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ))
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("$testTag-DropdownMenu")) {
              dropdownItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                      onFocusAreaSelected(item)
                      expanded = false
                    })
              }
            }
      }
}
