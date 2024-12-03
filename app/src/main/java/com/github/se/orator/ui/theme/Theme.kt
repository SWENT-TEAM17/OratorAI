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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

data class AppDimensionsObject(
    // Padding
    val paddingExtraSmall: Dp,
    val paddingSmall: Dp,
    val paddingSmallMedium: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val paddingExtraLarge: Dp,
    val paddingXXLarge: Dp,
    val paddingXXXLarge: Dp,
    val paddingTopSmall: Dp,
    val statusBarPadding: Dp,

    // Spacer Dimensions
    val spacerWidthMedium: Dp,
    val spacerHeightLarge: Dp,
    val largeSpacerHeight: Dp,

    // Button Heights
    val buttonHeight: Dp,
    val buttonHeightLarge: Dp,

    // Logo Dimensions
    val logoSize: Dp,
    val logoTextWidth: Dp,
    val logoTextHeight: Dp,
    val googleLogoSize: Dp,

    // Fixed Dimensions
    val strokeWidth: Dp,
    val borderStrokeWidth: Dp,
    val dividerThickness: Dp,
    val appBarElevation: Dp,
    val elevationSmall: Dp,

    // Loading Indicator
    val loadingIndicatorSize: Dp,

    // Corner Radii
    val roundedCornerRadius: Dp,
    val cornerRadiusSmall: Dp,

    // Icon Sizes
    val iconSize: Dp,
    val iconSizeSmall: Dp,
    val iconSizeMedium: Dp,
    val iconSizeLarge: Dp,

    // Drawer Padding
    val drawerPadding: Dp,

    // Card Dimensions
    val cardHorizontalPadding: Dp,
    val cardCornerRadius: Dp,
    val cardImageHeight: Dp,
    val cardSectionHeight: Dp,

    // Spacing
    val spacingXLarge: Dp,

    // Navigation
    val bottomNavigationHeight: Dp,

    // Input Fields
    val inputFieldHeight: Dp,
    val bioFieldHeight: Dp,

    // Profile Picture
    val profilePictureDialogSize: Dp,
    val profilePictureSize: Dp,

    // Font Size
    val largeTitleFontSize: Dp,
    val mediumTitleFontSize: Dp,
    val buttonTextSize: Dp,
)

object AppDimensions {
  val paddingSmall = 8.dp
  val paddingMedium = 16.dp
  val paddingLarge = 24.dp
  val paddingExtraLarge = 32.dp
  val largeSpacerHeight = 100.dp
  val mediumHeight = 64.dp
  val MediumSpacerHeight = 50.dp
  val SmallSpacerHeight = 15.dp
  val buttonHeight = 48.dp
  val logoSize = 250.dp
  val logoTextWidth = 276.dp
  val logoTextHeight = 141.dp
  val googleLogoSize = 30.dp
  val strokeWidth = 8.dp
  val borderStrokeWidth = 1.dp
  val loadingIndicatorSize = 64.dp
  val roundedCornerRadius = 12.dp
  val spacerWidthMedium = 16.dp
  val spacerHeightLarge = 24.dp
  val spacerHeightMedium = 8.dp
  val iconSize = 100.dp
  val drawerPadding = 16.dp
  val cardHorizontalPadding = 30.dp
  val cardCornerRadius = 16.dp
  val cardImageHeight = 160.dp
  val paddingXXLarge = 42.dp // Corresponds to 42.dp
  val paddingXXXLarge = 64.dp // Corresponds to 64.dp
  val spacingXLarge = 40.dp // Corresponds to 40.dp spacing in ButtonRow
  val bottomNavigationHeight = 60.dp // Corresponds to the height of BottomNavigation
  val paddingExtraSmall = 4.dp
  val paddingSmallMedium = 12.dp // Added for padding of 12.dp
  val iconSizeSmall = 32.dp // Added for icon size 32.dp
  val iconSizeMedium = 24.dp // Added for medium-sized icons
  val cornerRadiusSmall = 8.dp // Added for corner radius of 8.dp
  val appBarElevation = 0.dp // Added for elevation of AppBar
  val elevationSmall = 4.dp // For AppBar elevation
  val inputFieldHeight = 56.dp // Existing or newly added for input fields
  val bioFieldHeight = 150.dp // Added for bio input field height
  val buttonHeightLarge = 50.dp // Added for Save changes button height
  val profilePictureDialogSize = 200.dp // Added for ProfilePictureDialog size
  val cardSectionHeight = 100.dp // Added for CardSection height
  val profilePictureSize = 100.dp // Added for profile picture size
  val dividerThickness = 3.dp // Added for HorizontalDivider thickness
  val paddingTopSmall = 5.dp // Added for padding top=5.dp in Text
  val statusBarPadding = 10.dp
  val iconSizeLarge = 35.dp
  val buttonSize = 80.dp
  val visualizerHeight = 100.dp
  val iconSizeMic = 48.dp
  val heightMedium = 24.dp
  val paddingMediumSmall = 20.dp
  val smallWidth = 12.dp
  val smallPadding = 4.dp
  val mediumText = 16.sp
  val full = 1f
  val imageLargeXXL = 350.dp
  val smallTitleFontSize = 20.dp
  val mediumTitleFontSize = 30.dp
  val profileBoxHeight = 200.dp
  val profileCardHeight = 140.dp

  /// Offline Mode layout definition
  val buttonWidthMin = 200.dp // Minimum width for buttons
  val buttonWidthMax = 300.dp // Maximum width for buttons
  val buttonHeightRounded = 50.dp // Height for rounded buttons
  val shadowElevation = 5.dp // Elevation/shadow for buttons
  // Specific Spacing Needs
  val spacerHeightDefault = 32.dp // Spacer height for default spacing in between elements
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
