package com.github.se.orator.ui.overview

/**
 * The InputFieldData data class represents the data required to render an input field.
 *
 * @param value The value of the input field.
 * @param onValueChange The lambda that is called when the value of the input field changes.
 * @param label The label of the input field.
 * @param placeholder The placeholder text of the input field.
 * @param testTag The test tag of the input field.
 * @param height The height of the input field.
 */
data class InputFieldData(
    val value: String,
    val onValueChange: (String) -> Unit, // Explicitly specify that the lambda takes a String parameter
    val label: String,
    val placeholder: String,
    val testTag: String,
    val height: Int = 60 // Default height for input fields
)
