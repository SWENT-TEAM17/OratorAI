// File: ThemeExtensions.kt
package com.github.se.orator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val MaterialTheme.dimensions: AppDimensionsObject
  @Composable get() = LocalAppDimensions.current

val LocalAppDimensions =
    staticCompositionLocalOf<AppDimensionsObject> { error("No AppDimensionsObject provided") }
