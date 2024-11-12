package com.github.se.orator.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Define reusable dimensions
object AppDimensions {
  val paddingSmall = 8.dp
  val paddingMedium = 16.dp
  val paddingLarge = 24.dp
  val paddingExtraLarge = 32.dp
  val largeSpacerHeight = 100.dp
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
  val appBarElevation = 4.dp // Added for elevation of AppBar
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
}

object AppShapes {
  // You may have an AppShapes object for reusable shapes
  val bottomNavigationItemCornerRadius =
      50.dp // Corresponds to the corner radius of BottomNavigationItem
  val bottomNavigationItemShape = RoundedCornerShape(bottomNavigationItemCornerRadius)
  val circleShape = CircleShape // Added for circular buttons
}

private val DarkColorScheme =
    darkColorScheme(primary = AppColors.primaryColor, surface = AppColors.surfaceColor)

private val LightColorScheme =
    lightColorScheme(primary = AppColors.primaryColor, surface = AppColors.surfaceColor)

@Composable
fun ProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
  val colorScheme =
      when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
      }

  MaterialTheme(colorScheme = colorScheme, typography = CustomTypography, content = content)
}
