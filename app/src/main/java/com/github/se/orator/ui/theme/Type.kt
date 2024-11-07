// File: Type.kt
package com.github.se.orator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.github.se.orator.R

object AppFontSizes {
  val largeTitle = 64.sp
  val mediumTitle = 55.sp
  val buttonText = 16.sp
  val loadingText = 18.sp
  val titleMedium = 20.sp
  val bodySmall = 14.sp
  val bodyLarge = 18.sp // Updated to 18.sp for consistency
  val largeTitleSize = 50.sp
  val mediumTitleSize = 47.sp
  val buttonTextSize = 20.sp
  val cardTitleSize = 20.sp
  val cardTitle = 20.sp
  val titleLarge = 24.sp // For large titles in composables
  val subtitle = 16.sp
  val titleSmall = 18.sp
}

object AppTypography {
  val manropeFontFamily = FontFamily(Font(R.font.manrope_variablefont_wght))

  val largeTitleStyle =
      TextStyle(
          fontSize = AppFontSizes.largeTitle,
          fontFamily = manropeFontFamily,
          fontWeight = FontWeight.W600,
          textAlign = TextAlign.Center)

  val mediumTitleStyle =
      TextStyle(
          fontSize = AppFontSizes.mediumTitle,
          fontWeight = FontWeight.W500,
          color = AppColors.textColor,
          textAlign = TextAlign.Center,
      )

  val buttonTextStyle =
      TextStyle(
          fontSize = AppFontSizes.buttonText,
          fontWeight = FontWeight.Medium,
          color = AppColors.buttonTextColor)

  val loadingTextStyle =
      TextStyle(fontSize = AppFontSizes.loadingText, color = AppColors.secondaryTextColor)

  val appBarTitleStyle =
      TextStyle(
          fontSize = AppFontSizes.titleMedium,
          fontWeight = FontWeight.Bold,
          color = AppColors.textColor)

  val titleLargeStyle =
      TextStyle(
          fontSize = AppFontSizes.titleLarge,
          fontWeight = FontWeight.Bold,
          color = AppColors.textColor)

  val subtitleStyle =
      TextStyle(fontSize = AppFontSizes.bodyLarge, color = AppColors.secondaryTextColor)

  val bodyLargeStyle = TextStyle(fontSize = AppFontSizes.bodyLarge, color = AppColors.textColor)
}

// Define custom typography
val CustomTypography =
    Typography(
        titleMedium =
            TextStyle(
                fontSize = AppFontSizes.titleMedium,
                fontWeight = FontWeight.W500,
                color = AppColors.textColor),
        bodySmall =
            TextStyle(
                fontSize = AppFontSizes.bodySmall,
                fontWeight = FontWeight.Normal,
                color = AppColors.secondaryTextColor),
        bodyLarge =
            TextStyle(
                fontSize = AppFontSizes.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = AppColors.textColor))
