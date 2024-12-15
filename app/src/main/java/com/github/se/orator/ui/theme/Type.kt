package com.github.se.orator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.github.se.orator.R

object AppFontSizes {
  // Reference dimensions based on your design (e.g., Figma)
  private const val MODEL_WIDTH_DP = 448.0f // Base width in dp from design
  private const val MODEL_HEIGHT_DP = 923.0f // Base height in dp from design

  @Composable
  private fun scaleFactorWidth(): Float {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    return remember(screenWidthDp) { screenWidthDp / MODEL_WIDTH_DP }
  }

  @Composable
  private fun scaleFactorHeight(): Float {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.toFloat()
    return remember(screenHeightDp) { screenHeightDp / MODEL_HEIGHT_DP }
  }

  /**
   * Scales the base font size using the width or height scale factor.
   *
   * @param baseSize The base font size in sp.
   * @param isWidthBased Determines whether to scale based on width or height.
   * @return The scaled font size as a [TextUnit].
   */
  @Composable
  private fun scaleFontSize(baseSize: Float, isWidthBased: Boolean = true): TextUnit {
    val scaleFactor = if (isWidthBased) scaleFactorWidth() else scaleFactorHeight()
    val scaledSize = baseSize * scaleFactor
    // Clamp the scaled size to ensure readability
    val clampedSize = scaledSize.coerceIn(12f, 40f)
    return clampedSize.sp
  }

  // Define scaled font sizes
  val largeTitle: TextUnit
    @Composable get() = scaleFontSize(64f)

  val mediumTitle: TextUnit
    @Composable get() = scaleFontSize(55f)

  val buttonText: TextUnit
    @Composable get() = scaleFontSize(16f)

  val loadingText: TextUnit
    @Composable get() = scaleFontSize(18f)

  val titleMedium: TextUnit
    @Composable get() = scaleFontSize(20f)

  val bodySmall: TextUnit
    @Composable get() = scaleFontSize(14f)

  val bodyLarge: TextUnit
    @Composable get() = scaleFontSize(18f)

  val largeTitleSize: TextUnit
    @Composable get() = scaleFontSize(50f)

  val cardTitle: TextUnit
    @Composable get() = scaleFontSize(20f)

  val titleLarge: TextUnit
    @Composable get() = scaleFontSize(24f)

  val subtitle: TextUnit
    @Composable get() = scaleFontSize(16f)

  val poppinsSizeLarge: TextUnit
    @Composable get() = scaleFontSize(32f)

  val poppinsHeightLarge: TextUnit
    @Composable get() = scaleFontSize(40f, isWidthBased = false)

  val poppinsSizeMedium: TextUnit
    @Composable get() = scaleFontSize(24f)

  val poppinsHeightMedium: TextUnit
    @Composable get() = scaleFontSize(32f, isWidthBased = false)

  val poppinsSizeSmall: TextUnit
    @Composable get() = scaleFontSize(18f)

  val poppinsSizeXSmall: TextUnit
    @Composable get() = scaleFontSize(10f)
}

object AppTypography {
  val manropeFontFamily = FontFamily(Font(R.font.manrope_variablefont_wght))
  val poppinsBlackFontFamily = FontFamily(Font(R.font.poppins_black))
  val poppinsRegularFontFamily = FontFamily(Font(R.font.poppins_regular))

  val mediumTopBarStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.poppinsSizeLarge,
            fontFamily = manropeFontFamily,
            fontWeight = FontWeight.W600,
            lineHeight = AppFontSizes.poppinsHeightLarge,
            color = androidx.compose.ui.graphics.Color.Black,
            textAlign = TextAlign.Center,
        )

  val bigTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.largeTitle, // Scaled large title
            fontFamily = manropeFontFamily,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center)

  val largeTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.poppinsSizeLarge, // Scaled poppins large size
            fontFamily = poppinsBlackFontFamily,
            fontWeight = FontWeight.W600,
            lineHeight = AppFontSizes.poppinsHeightLarge, // Scaled line height
            color = androidx.compose.ui.graphics.Color.Black,
            textAlign = TextAlign.Center)

  val mediumTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.poppinsSizeMedium, // Scaled poppins medium size
            fontFamily = poppinsBlackFontFamily,
            fontWeight = FontWeight.W600,
            lineHeight = AppFontSizes.poppinsHeightMedium, // Scaled line height
            color = androidx.compose.ui.graphics.Color.Black,
            textAlign = TextAlign.Center,
        )

  // Small title style with specified specs
  val smallTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.poppinsSizeSmall, // Scaled poppins small size
            fontFamily = poppinsRegularFontFamily,
            fontWeight = FontWeight.W200,
            lineHeight = AppFontSizes.poppinsSizeMedium, // Optional line height
            color = androidx.compose.ui.graphics.Color.Black,
            textAlign = TextAlign.Center,
        )

  val xSmallTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.poppinsSizeXSmall, // Scaled poppins extra small size
            fontFamily = poppinsRegularFontFamily,
            fontWeight = FontWeight.W200,
            lineHeight = AppFontSizes.poppinsSizeMedium, // Optional line height
            color = androidx.compose.ui.graphics.Color.Black,
            textAlign = TextAlign.Center,
        )

  val mainScreenTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.largeTitle, // Scaled large title
            fontFamily = poppinsBlackFontFamily,
            fontWeight = FontWeight.W600,
            lineHeight = AppFontSizes.poppinsHeightLarge, // Scaled line height
            color = AppColors.textColor,
            textAlign = TextAlign.Start)

  val mainScreenSubtitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.mediumTitle, // Scaled medium title
            fontFamily = poppinsRegularFontFamily,
            fontWeight = FontWeight.W500,
            lineHeight = AppFontSizes.poppinsHeightMedium, // Scaled line height
            color = AppColors.textColor,
            textAlign = TextAlign.Start)

  val buttonTextStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.buttonText,
            fontWeight = FontWeight.Medium,
            color = AppColors.buttonTextColor)

  val loadingTextStyle: TextStyle
    @Composable
    get() = TextStyle(fontSize = AppFontSizes.loadingText, color = AppColors.secondaryTextColor)

  val appBarTitleStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.textColor)

  val titleLargeStyle: TextStyle
    @Composable
    get() =
        TextStyle(
            fontSize = AppFontSizes.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.textColor)

  val subtitleStyle: TextStyle
    @Composable
    get() = TextStyle(fontSize = AppFontSizes.bodyLarge, color = AppColors.secondaryTextColor)

  val bodyLargeStyle: TextStyle
    @Composable get() = TextStyle(fontSize = AppFontSizes.bodyLarge, color = AppColors.textColor)
}

// Define custom typography
val CustomTypography: Typography
  @Composable
  get() =
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
