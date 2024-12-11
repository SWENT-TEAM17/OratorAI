package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.theme.AppColors.graphDots
import com.github.se.orator.ui.theme.AppColors.secondaryColor
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppDimensions.graphHeight
import com.github.se.orator.ui.theme.AppDimensions.graphWidth
import com.github.se.orator.ui.theme.AppDimensions.paddingExtraLarge
import com.github.se.orator.ui.theme.AppDimensions.paddingSmall
import com.github.se.orator.ui.theme.AppTypography
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GraphStats(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()

  val xValues = (1..10).toList()

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Statistics",
                  modifier = Modifier.testTag("statTitle"),
                  color = MaterialTheme.colorScheme.onSurface)
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    navigationActions.goBack() // Navigate back
                  },
                  modifier = Modifier.testTag("statBackButton")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.testTag("statBackIcon"),
                        tint = MaterialTheme.colorScheme.onSurface)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer))
      },
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          userProfile?.let { profile ->
            Text(
                modifier =
                    Modifier.padding(start = AppDimensions.paddingXXLarge)
                        .padding(top = AppDimensions.paddingXXLarge)
                        .testTag("graphScreenTitle"),
                text = "Statistics Graph",
                style = AppTypography.mediumTitleStyle, // Apply custom style for title
            )

            Text(
                modifier =
                    Modifier.padding(start = AppDimensions.paddingXXLarge)
                        .padding(top = AppDimensions.paddingExtraLarge)
                        .testTag("talkTimeSecTitle"),
                text = "Talk Time Seconds:",
                style = AppTypography.smallTitleStyle, // Apply custom style for title
            )

            Row() {
              Column(
                  modifier =
                      Modifier.padding(
                          top = paddingExtraLarge,
                          bottom = paddingExtraLarge,
                          start = paddingExtraLarge)) {
                    LineChart(
                        xValues,
                        profileViewModel.ensureListSizeTen(
                            profile.statistics.recentData.toList().map { data ->
                              data.talkTimeSeconds.toFloat()
                            }))
                  }

              Column(
                  modifier =
                      Modifier.padding(
                          top = paddingExtraLarge,
                          bottom = paddingExtraLarge,
                          start = paddingExtraLarge)) {
                    Text(
                        modifier =
                            Modifier.padding(start = AppDimensions.paddingLarge)
                                .padding(top = AppDimensions.paddingExtraLarge)
                                .testTag("talkTimeSecMeanTitle"),
                        text = "Mean: ${profile.statistics.talkTimeSecMean}",
                        style = AppTypography.smallTitleStyle, // Apply custom style for title
                    )
                  }
            }

            Text(
                modifier =
                    Modifier.padding(start = AppDimensions.paddingXXLarge)
                        .padding(top = AppDimensions.paddingExtraLarge)
                        .testTag("talkTimePercTitle"),
                text = "Talk Time Percentage:",
                style = AppTypography.smallTitleStyle, // Apply custom style for title
            )

            Row() {
              Column(
                  modifier =
                      Modifier.padding(
                          top = paddingExtraLarge,
                          bottom = paddingExtraLarge,
                          start = paddingExtraLarge)) {
                    LineChart(
                        xValues,
                        profileViewModel.ensureListSizeTen(
                            profile.statistics.recentData.toList().map { data ->
                              data.talkTimePercentage.toFloat()
                            }))
                  }

              Column(
                  modifier =
                      Modifier.padding(
                          top = paddingExtraLarge,
                          bottom = paddingExtraLarge,
                          start = paddingExtraLarge)) {
                    Text(
                        modifier =
                            Modifier.padding(start = AppDimensions.paddingLarge)
                                .padding(top = AppDimensions.paddingExtraLarge)
                                .testTag("talkTimePercMeanTitle"),
                        text = "Mean: ${profile.statistics.talkTimePercMean}",
                        style = AppTypography.smallTitleStyle, // Apply custom style for title
                    )
                  }
            }
          }
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.HOME)
      })
}

@Composable
fun LineChart(xValues: List<Int>, yValues: List<Float>) {
  require(xValues.size == yValues.size) { "X and Y values must have the same size." }

  Canvas(
      modifier =
          Modifier.width(graphWidth)
              .height(graphHeight)
              .padding(start = paddingExtraLarge, top = paddingSmall)) {
        val maxX = xValues.maxOrNull()?.toFloat() ?: 1f
        val maxY = yValues.maxOrNull() ?: 1f
        val minY = yValues.minOrNull() ?: 0f
        val yRange = maxY - minY

        // Avoid division by zero: Assign a minimal range if all yValues are the same
        val adjustedYRange = if (yRange == 0f) 1f else yRange
        val xStep = size.width / (xValues.size - 1)
        val yScale = size.height / adjustedYRange

        // Number of ticks to show on the Y-axis
        val tickCount = 5
        val tickInterval = adjustedYRange / tickCount

        // Draw Y-axis ticks and labels
        for (i in 0..tickCount) {
          val tickValue = minY + i * tickInterval
          val tickY = size.height - (tickValue - minY) * yScale

          // Draw tick line
          drawLine(
              color = Color.Gray,
              start = Offset(-20f, tickY),
              end = Offset(size.width, tickY),
              strokeWidth = 1f)

          // Draw tick label
          drawContext.canvas.nativeCanvas.apply {
            drawText(
                String.format("%.1f", tickValue),
                -50f,
                tickY + 10f,
                android.graphics.Paint().apply {
                  color = android.graphics.Color.BLACK
                  textSize = 20f
                })
          }
        }

        // Draw Axes
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 10f)
        drawLine(
            color = Color.Black,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 10f)

        // Plot Points and Lines
        for (i in 0 until xValues.size - 1) {
          val startX = i * xStep
          val startY = size.height - (yValues[i] - minY) * yScale
          val endX = (i + 1) * xStep
          val endY = size.height - (yValues[i + 1] - minY) * yScale

          drawLine(
              color = secondaryColor,
              start = Offset(startX, startY),
              end = Offset(endX, endY),
              strokeWidth = 5f)
        }

        // Draw Points
        for (i in xValues.indices) {
          val x = i * xStep
          val y = size.height - (yValues[i] - minY) * yScale
          drawCircle(
              color = graphDots, center = Offset(x, y), radius = 7f // Smaller point size
              )
        }
      }
}
