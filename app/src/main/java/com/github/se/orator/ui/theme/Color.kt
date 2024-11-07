// File: Color.kt
package com.github.se.orator.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
  // Original Colors
  val Purple80 = Color(0xFFD0BCFF)
  val PurpleGrey80 = Color(0xFFCCC2DC)
  val Pink80 = Color(0xFFEFB8C8)

  val Purple40 = Color(0xFF6650a4)
  val PurpleGrey40 = Color(0xFF625b71)
  val Pink40 = Color(0xFF7D5260)

  // Additional Theme Colors
  val primaryColor = Color(0xFF442DAA)
  val cardBackgroundColor = Color(0xFFF5F5F5)
  val secondaryColor = Color(0xFF00A6A6)
  val textColor = Color(0xFF000000)
  val buttonTextColor = Color.Gray
  val buttonBackgroundColor = Color.White
  val buttonBorderColor = Color.LightGray
  val loadingIndicatorColor = Color(0xFF442DAA)
  val secondaryTextColor = Color.Gray
  val surfaceColor = Color.White
  val goldColor = Color(0xFFFFD700)
  val silverColor = Color(0xFFC0C0C0)
  val bronzeColor = Color(0xFFCD7F32)
  val errorColor = Color.Red // For error messages

  // Gradients
  val primaryGradient = Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))

  // Message Backgrounds
  val userMessageBackgroundColor = Color(0xFFE8EAF6)
  val assistantMessageBackgroundColor = Color(0xFFE1F5FE)
}
