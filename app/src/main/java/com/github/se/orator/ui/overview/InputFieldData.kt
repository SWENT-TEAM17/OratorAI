package com.github.se.orator.ui.overview

data class InputFieldData(
    val value: String,
    val onValueChange: (String) -> Unit, // Explicitly specify that the lambda takes a String parameter
    val label: String,
    val placeholder: String,
    val testTag: String,
    val height: Int = 60 // Default height for input fields
)
