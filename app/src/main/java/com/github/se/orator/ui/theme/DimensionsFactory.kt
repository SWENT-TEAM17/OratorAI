package com.github.se.orator.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun createAppDimensions(): AppDimensionsObject {
  val configuration = LocalConfiguration.current
  val screenWidth = configuration.screenWidthDp.dp
  val screenHeight = configuration.screenHeightDp.dp

  return AppDimensionsObject(
      // Padding
      paddingExtraSmall = (screenWidth * 0.01f).coerceIn(2.dp, 4.dp),
      paddingSmall = (screenWidth * 0.02f).coerceIn(4.dp, 8.dp),
      paddingSmallMedium = (screenWidth * 0.03f).coerceIn(6.dp, 12.dp),
      paddingMedium = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp),
      paddingLarge = (screenWidth * 0.06f).coerceIn(12.dp, 24.dp),
      paddingExtraLarge = (screenWidth * 0.08f).coerceIn(16.dp, 32.dp),
      paddingXXLarge = (screenWidth * 0.1f).coerceIn(24.dp, 42.dp),
      paddingXXXLarge = (screenWidth * 0.15f).coerceIn(32.dp, 64.dp),
      paddingTopSmall = (screenHeight * 0.01f).coerceIn(4.dp, 5.dp),
      statusBarPadding = (screenHeight * 0.015f).coerceIn(6.dp, 10.dp),

      // Spacer Dimensions
      spacerWidthMedium = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp),
      spacerHeightLarge = (screenWidth * 0.06f).coerceIn(12.dp, 24.dp),
      largeSpacerHeight = (screenHeight * 0.1f).coerceIn(50.dp, 200.dp),

      // Button Heights
      buttonHeight = (screenHeight * 0.06f).coerceIn(40.dp, 60.dp),
      buttonHeightLarge = (screenHeight * 0.07f).coerceIn(45.dp, 60.dp),

      // Logo Dimensions
      logoSize = (screenWidth * 0.4f).coerceIn(100.dp, 250.dp),
      logoTextWidth = (screenWidth * 0.6f).coerceIn(150.dp, 276.dp),
      logoTextHeight = (screenHeight * 0.15f).coerceIn(70.dp, 141.dp),
      googleLogoSize = (screenWidth * 0.08f).coerceIn(24.dp, 30.dp),

      // Fixed Dimensions
      strokeWidth = 8.dp,
      borderStrokeWidth = 1.dp,
      dividerThickness = 3.dp,
      appBarElevation = 4.dp,
      elevationSmall = 4.dp,

      // Loading Indicator
      loadingIndicatorSize = (screenWidth * 0.1f).coerceIn(40.dp, 64.dp),

      // Corner Radii
      roundedCornerRadius = (screenWidth * 0.025f).coerceIn(8.dp, 16.dp),
      cornerRadiusSmall = (screenWidth * 0.015f).coerceIn(4.dp, 8.dp),

      // Icon Sizes
      iconSize = (screenWidth * 0.15f).coerceIn(60.dp, 100.dp),
      iconSizeSmall = (screenWidth * 0.08f).coerceIn(24.dp, 32.dp),
      iconSizeMedium = (screenWidth * 0.06f).coerceIn(20.dp, 24.dp),
      iconSizeLarge = (screenWidth * 0.1f).coerceIn(30.dp, 35.dp),

      // Drawer Padding
      drawerPadding = (screenWidth * 0.04f).coerceIn(8.dp, 16.dp),

      // Card Dimensions
      cardHorizontalPadding = (screenWidth * 0.05f).coerceIn(16.dp, 30.dp),
      cardCornerRadius = (screenWidth * 0.025f).coerceIn(8.dp, 16.dp),
      cardImageHeight = (screenWidth * 0.3f).coerceIn(120.dp, 160.dp),
      cardSectionHeight = (screenHeight * 0.15f).coerceIn(80.dp, 100.dp),

      // Spacing
      spacingXLarge = (screenWidth * 0.1f).coerceIn(32.dp, 40.dp),

      // Navigation
      bottomNavigationHeight = (screenHeight * 0.08f).coerceIn(50.dp, 60.dp),

      // Input Fields
      inputFieldHeight = (screenHeight * 0.07f).coerceIn(50.dp, 60.dp),
      bioFieldHeight = (screenHeight * 0.2f).coerceIn(100.dp, 150.dp),

      // Profile Picture
      profilePictureDialogSize = (screenWidth * 0.5f).coerceIn(150.dp, 200.dp),
      profilePictureSize = (screenWidth * 0.2f).coerceIn(80.dp, 100.dp))
}
