package com.github.se.orator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

object AppDimensions {

  // Helper functions to get screen dimensions
  @Composable private fun screenWidth() = LocalConfiguration.current.screenWidthDp.dp

  @Composable private fun screenHeight() = LocalConfiguration.current.screenHeightDp.dp

  // Padding
  val paddingSmall: Dp
    @Composable get() = (screenWidth() * 0.02f).coerceIn(4.dp, 8.dp)

  val paddingMedium: Dp
    @Composable get() = (screenWidth() * 0.04f).coerceIn(8.dp, 16.dp)

  val paddingLarge: Dp
    @Composable get() = (screenWidth() * 0.06f).coerceIn(12.dp, 24.dp)

  val paddingExtraLarge: Dp
    @Composable get() = (screenWidth() * 0.08f).coerceIn(16.dp, 32.dp)

  val paddingXXLarge: Dp
    @Composable get() = (screenWidth() * 0.1f).coerceIn(24.dp, 42.dp)

  val paddingXXXLarge: Dp
    @Composable get() = (screenWidth() * 0.15f).coerceIn(32.dp, 64.dp)

  val paddingExtraSmall: Dp
    @Composable get() = (screenWidth() * 0.01f).coerceIn(2.dp, 4.dp)

  val paddingSmallMedium: Dp
    @Composable get() = (screenWidth() * 0.03f).coerceIn(6.dp, 12.dp)

  val paddingMediumSmall: Dp
    @Composable get() = (screenWidth() * 0.05f).coerceIn(10.dp, 20.dp)

  val paddingTopSmall: Dp
    @Composable get() = (screenHeight() * 0.005f).coerceIn(2.dp, 5.dp)

  val statusBarPadding: Dp
    @Composable get() = (screenHeight() * 0.015f).coerceIn(6.dp, 10.dp)

  val smallPadding: Dp
    @Composable get() = paddingExtraSmall

  // Spacer Dimensions
  val largeSpacerHeight: Dp
    @Composable get() = (screenHeight() * 0.1f).coerceIn(50.dp, 100.dp)

  val mediumSpacerHeight: Dp
    @Composable get() = (screenHeight() * 0.07f).coerceIn(35.dp, 70.dp)

  val smallSpacerHeight: Dp
    @Composable get() = (screenHeight() * 0.02f).coerceIn(10.dp, 20.dp)

  val spacerWidthMedium: Dp
    @Composable get() = (screenWidth() * 0.04f).coerceIn(8.dp, 16.dp)

  val spacerHeightLarge: Dp
    @Composable get() = (screenHeight() * 0.03f).coerceIn(12.dp, 24.dp)

  val spacerHeightMedium: Dp
    @Composable get() = (screenHeight() * 0.015f).coerceIn(6.dp, 12.dp)

  val spacerHeightDefault: Dp
    @Composable get() = (screenHeight() * 0.04f).coerceIn(16.dp, 32.dp)

  // Button Heights and Sizes
  val buttonHeight: Dp
    @Composable get() = (screenHeight() * 0.06f).coerceIn(40.dp, 60.dp)

  val buttonHeightLarge: Dp
    @Composable get() = (screenHeight() * 0.07f).coerceIn(45.dp, 70.dp)

  val buttonHeightRounded: Dp
    @Composable get() = buttonHeightLarge

  val buttonSize: Dp
    @Composable get() = (screenWidth() * 0.2f).coerceIn(60.dp, 100.dp)

  val buttonWidthMin: Dp
    @Composable get() = (screenWidth() * 0.5f).coerceIn(150.dp, 200.dp)

  val buttonWidthMax: Dp
    @Composable get() = (screenWidth() * 0.7f).coerceIn(250.dp, 300.dp)

  // Logo Dimensions
  val logoSize: Dp
    @Composable get() = (screenWidth() * 0.4f).coerceIn(100.dp, 250.dp)

  val logoTextWidth: Dp
    @Composable get() = (screenWidth() * 0.6f).coerceIn(150.dp, 276.dp)

  val logoTextHeight: Dp
    @Composable get() = (screenHeight() * 0.15f).coerceIn(70.dp, 141.dp)

  val googleLogoSize: Dp
    @Composable get() = (screenWidth() * 0.08f).coerceIn(24.dp, 30.dp)

  val imageLargeXXL: Dp
    @Composable get() = (screenWidth() * 0.8f).coerceIn(200.dp, 350.dp)

  // Fixed Dimensions
  val strokeWidth: Dp = 8.dp
  val borderStrokeWidth: Dp = 1.dp
  val dividerThickness: Dp = 3.dp
  val appBarElevation: Dp = 0.dp
  val elevationSmall: Dp = 4.dp
  val shadowElevation: Dp = 5.dp

  // Loading Indicator
  val loadingIndicatorSize: Dp
    @Composable get() = (screenWidth() * 0.1f).coerceIn(40.dp, 64.dp)

  // Corner Radii
  val roundedCornerRadius: Dp
    @Composable get() = (screenWidth() * 0.03f).coerceIn(8.dp, 16.dp)

  val cornerRadiusSmall: Dp
    @Composable get() = (screenWidth() * 0.02f).coerceIn(4.dp, 8.dp)

  // Icon Sizes
  val iconSize: Dp
    @Composable get() = (screenWidth() * 0.15f).coerceIn(60.dp, 100.dp)

  val iconSizeSmall: Dp
    @Composable get() = (screenWidth() * 0.08f).coerceIn(24.dp, 32.dp)

  val iconSizeMedium: Dp
    @Composable get() = (screenWidth() * 0.06f).coerceIn(20.dp, 24.dp)

  val iconSizeLarge: Dp
    @Composable get() = (screenWidth() * 0.1f).coerceIn(30.dp, 35.dp)

  val iconSizeMic: Dp
    @Composable get() = (screenWidth() * 0.12f).coerceIn(36.dp, 48.dp)

  // Drawer Padding
  val drawerPadding: Dp
    @Composable get() = spacerWidthMedium

  // Card Dimensions
  val cardHorizontalPadding: Dp
    @Composable get() = (screenWidth() * 0.05f).coerceIn(16.dp, 30.dp)

  val cardCornerRadius: Dp
    @Composable get() = roundedCornerRadius

  val cardImageHeight: Dp
    @Composable get() = (screenWidth() * 0.3f).coerceIn(120.dp, 160.dp)

  val cardSectionHeight: Dp
    @Composable get() = (screenHeight() * 0.15f).coerceIn(80.dp, 100.dp)

  // Spacing
  val spacingXLarge: Dp
    @Composable get() = (screenWidth() * 0.1f).coerceIn(32.dp, 40.dp)

  // Navigation
  val bottomNavigationHeight: Dp
    @Composable get() = (screenHeight() * 0.08f).coerceIn(50.dp, 60.dp)

  // Input Fields
  val inputFieldHeight: Dp
    @Composable get() = (screenHeight() * 0.07f).coerceIn(50.dp, 60.dp)

  val bioFieldHeight: Dp
    @Composable get() = (screenHeight() * 0.2f).coerceIn(100.dp, 150.dp)

  // Profile Picture
  val profilePictureDialogSize: Dp
    @Composable get() = (screenWidth() * 0.5f).coerceIn(150.dp, 200.dp)

  val profilePictureSize: Dp
    @Composable get() = (screenWidth() * 0.2f).coerceIn(80.dp, 100.dp)

  val profileBoxHeight: Dp
    @Composable get() = (screenHeight() * 0.25f).coerceIn(150.dp, 200.dp)

  val profileCardHeight: Dp
    @Composable get() = (screenHeight() * 0.18f).coerceIn(100.dp, 140.dp)

  // Visualizer Height
  val visualizerHeight: Dp
    @Composable get() = (screenHeight() * 0.15f).coerceIn(80.dp, 120.dp)

    val jobDescriptionInputFieldHeight: Dp
        @Composable get() = (screenHeight() * 0.15f).coerceIn(180.dp, 240.dp)


    // Heights and Widths
  val mediumHeight: Dp
    @Composable get() = (screenHeight() * 0.08f).coerceIn(50.dp, 64.dp)

  val heightMedium: Dp
    @Composable get() = (screenHeight() * 0.04f).coerceIn(20.dp, 24.dp)

  val smallWidth: Dp
    @Composable get() = (screenWidth() * 0.03f).coerceIn(8.dp, 12.dp)

  // Font Sizes
  val largeTitleFontSize: TextUnit
    @Composable get() = ((LocalConfiguration.current.screenWidthDp * 0.08f).coerceIn(24f, 30f)).sp

  val mediumTitleFontSize: TextUnit
    @Composable get() = ((LocalConfiguration.current.screenWidthDp * 0.06f).coerceIn(20f, 24f)).sp

  val smallTitleFontSize: TextUnit
    @Composable get() = ((LocalConfiguration.current.screenWidthDp * 0.05f).coerceIn(16f, 20f)).sp

  val mediumText: TextUnit
    @Composable get() = ((LocalConfiguration.current.screenWidthDp * 0.04f).coerceIn(14f, 16f)).sp


  // Other Dimensions
  val full: Float = 1f
}

object AppShapes {
  // You may have an AppShapes object for reusable shapes
  val bottomNavigationItemCornerRadius =
      50.dp // Corresponds to the corner radius of BottomNavigationItem
  val bottomNavigationItemShape = RoundedCornerShape(bottomNavigationItemCornerRadius)
  val circleShape = CircleShape // Added for circular buttons
}

private val lightScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )

private val darkScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme =
    ColorFamily(Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified)

@Composable
fun ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable() () -> Unit
) {
  val colorScheme =
      when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
      }
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primary.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = CustomTypography, content = content)
}
