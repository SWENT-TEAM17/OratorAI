package com.github.se.orator.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppDimensionsObject
import com.github.se.orator.ui.theme.createAppDimensions

/**
 * Data class representing an input field in the Speaking Job Interview module.
 *
 * @param value The value of the input field.
 * @param onValueChange The callback to be invoked when the value of the input field changes.
 * @param question The question to be displayed above the input field.
 * @param placeholder The placeholder text to be displayed in the input field.
 * @param testTag The test tag for the input field.
 * @param isDropdown Whether the input field is a dropdown.
 * @param dropdownItems The items to be displayed in the dropdown.
 * @param isScrollable Whether the input field is scrollable.
 * @param height The height of the input field.
 */
data class InputFieldData(
    val value: String,
    val onValueChange: (String) -> Unit,
    val question: String,
    val placeholder: String,
    val testTag: String,
    val isDropdown: Boolean = false,
    val dropdownItems: List<String> = emptyList(),
    val isScrollable: Boolean = false,
    val height: Dp = AppDimensions.inputFieldHeight
)