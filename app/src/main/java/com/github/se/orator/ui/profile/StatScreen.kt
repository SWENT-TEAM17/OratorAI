package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.friends.TitleAppBar
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.theme.AppColors.axisColor
import com.github.se.orator.ui.theme.AppColors.graphDots
import com.github.se.orator.ui.theme.AppColors.secondaryColor
import com.github.se.orator.ui.theme.AppColors.tickLabelColor
import com.github.se.orator.ui.theme.AppColors.tickLineColor
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppDimensions.AXIS_STROKE_WIDTH
import com.github.se.orator.ui.theme.AppDimensions.DRAW_TEXT_TICK_LABEL_OFFSET_VALUE_FOR_Y
import com.github.se.orator.ui.theme.AppDimensions.DRAW_TEXT_TICK_LABEL_X
import com.github.se.orator.ui.theme.AppDimensions.FULL
import com.github.se.orator.ui.theme.AppDimensions.MIN_Y_RANGE
import com.github.se.orator.ui.theme.AppDimensions.PLOT_LINE_STROKE_WIDTH
import com.github.se.orator.ui.theme.AppDimensions.POINTS_RADIUS
import com.github.se.orator.ui.theme.AppDimensions.TICK_LABEL_TEXT_SIZE
import com.github.se.orator.ui.theme.AppDimensions.X_VALUE_FOR_OFFSET
import com.github.se.orator.ui.theme.AppDimensions.ZERO
import com.github.se.orator.ui.theme.AppDimensions.graphHeight
import com.github.se.orator.ui.theme.AppDimensions.graphWidth
import com.github.se.orator.ui.theme.AppDimensions.paddingExtraLarge
import com.github.se.orator.ui.theme.AppDimensions.paddingSmall
import com.github.se.orator.ui.theme.AppDimensions.paddingXXLarge
import com.github.se.orator.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
// Number of ticks to show on the Y-axis
const val TICK_COUNT = 5

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GraphStats(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()

  val xValues = (1..10).toList()

  Scaffold(
      topBar = {
        TitleAppBar(navigationActions, "Statistics", "statTitle", "statBackButton", "statBackIcon")
      },
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
              userProfile?.let { profile ->
                Text(
                    modifier =
                        Modifier.padding(start = AppDimensions.paddingXXLarge)
                            .padding(top = AppDimensions.paddingXXLarge)
                            .testTag("graphScreenTitle"),
                    text = "Statistics Graph",
                    style = AppTypography.mediumTitleStyle, // Apply custom style for title
                    color = MaterialTheme.colorScheme.primary)

                Text(
                    modifier =
                        Modifier.padding(start = AppDimensions.paddingXXLarge)
                            .padding(top = AppDimensions.paddingExtraLarge)
                            .testTag("talkTimeSecTitle"),
                    text = "Talk Time Seconds:",
                    style = AppTypography.smallTitleStyle, // Apply custom style for title
                    color = MaterialTheme.colorScheme.onSurface)

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
                                profile.statistics.recentData.map { data ->
                                  data.talkTimeSeconds.toFloat()
                                }),
                            "talkTimeSecGraph",
                            yMin = 0f,
                            yMax = 10f)
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
                            color = MaterialTheme.colorScheme.onSurface)
                      }
                }

                Text(
                    modifier =
                        Modifier.padding(start = AppDimensions.paddingXXLarge)
                            .padding(top = AppDimensions.paddingExtraLarge)
                            .testTag("talkTimePercTitle"),
                    text = "Talk Time Percentage:",
                    style = AppTypography.smallTitleStyle, // Apply custom style for title
                    color = MaterialTheme.colorScheme.onSurface)

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
                                profile.statistics.recentData.map { data ->
                                  data.talkTimePercentage.toFloat()
                                }),
                            "talkTimePercGraph",
                            yMin = 0f,
                            yMax = 100f)
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
                            color = MaterialTheme.colorScheme.onSurface)
                      }
                }
                TitleAndStatsRow(profile)
              }
            }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      })
}

@Composable
fun LineChart(xValues: List<Int>, yValues: List<Float>, testTag: String, yMin: Float, yMax: Float) {
  require(xValues.size == yValues.size) { "X and Y values must have the same size." }

  Canvas(
      modifier =
          Modifier.width(graphWidth)
              .height(graphHeight)
              .padding(start = paddingExtraLarge, top = paddingSmall)
              .testTag(testTag)) {
        // Use the provided yMin and yMax instead of computing from data
        val maxY = yValues.maxOrNull() ?: FULL // full being the const value for 1f
        val minY = ZERO // zero for the value 0f
        var yRange = maxY - minY
        if (yRange < MIN_Y_RANGE) {
          yRange = MIN_Y_RANGE
        }

        val xStep = size.width / (xValues.size - 1)
        val yScale = size.height / yRange

        val tickInterval = yRange / TICK_COUNT

        // Draw Y-axis ticks and labels
        for (i in 0..TICK_COUNT) {
          val tickValue = minY + i * tickInterval
          val tickY = size.height - (tickValue - minY) * yScale

          // Draw tick line
          drawLine(
              color = tickLineColor,
              start = Offset(X_VALUE_FOR_OFFSET, tickY),
              end = Offset(size.width, tickY),
              strokeWidth = FULL)

          // Draw tick label
          drawContext.canvas.nativeCanvas.apply {
            drawText(
                String.format("%.1f", tickValue),
                DRAW_TEXT_TICK_LABEL_X,
                tickY + DRAW_TEXT_TICK_LABEL_OFFSET_VALUE_FOR_Y,
                android.graphics.Paint().apply {
                  color = tickLabelColor
                  textSize = TICK_LABEL_TEXT_SIZE
                })
          }
        }

        // Draw Axes
        drawLine(
            color = axisColor,
            start = Offset(ZERO, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = AXIS_STROKE_WIDTH)
        drawLine(
            color = axisColor,
            start = Offset(ZERO, ZERO),
            end = Offset(ZERO, size.height),
            strokeWidth = AXIS_STROKE_WIDTH)

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
              strokeWidth = PLOT_LINE_STROKE_WIDTH)
        }

        // Draw Points
        for (i in xValues.indices) {
          val x = i * xStep
          val y = size.height - (yValues[i] - minY) * yScale
          drawCircle(color = graphDots, center = Offset(x, y), radius = POINTS_RADIUS)
        }
      }
}

/** Composable for practice mode title displays on the stats screen */
@Composable
fun PracticeModeTitle(modeTitleTestTag: String, mode: String) {
  androidx.compose.material3.Text(
      modifier =
          Modifier.padding(start = AppDimensions.paddingSmall)
              .padding(top = AppDimensions.paddingXXLarge)
              .testTag(modeTitleTestTag),
      text = mode,
      style = AppTypography.smallTitleStyle, // Apply custom style for title
      color = MaterialTheme.colorScheme.primary)
}

/** Composable for stats displays on the stats screen */
@Composable
fun StatDisplay(statTestTag: String, stat: String, statValue: String) {
  androidx.compose.material3.Text(
      modifier =
          Modifier.padding(start = AppDimensions.paddingSmall)
              .padding(top = AppDimensions.paddingSmall)
              .testTag(statTestTag),
      text = stat + statValue,
      style = AppTypography.xSmallTitleStyle, // Apply custom style for title
      color = MaterialTheme.colorScheme.onSurface)
}

/** Composable for a single mode's stats and title */
@Composable
fun ModeStatsColumn(titleTestTag: String, mode: String, profile: UserProfile) {
  val modeKey = mode.uppercase()
  Column {
    PracticeModeTitle(titleTestTag, mode)
    StatDisplay(
        statTestTag = "totalSessions${mode}Title",
        stat = "Sessions: ",
        statValue = profile.statistics.sessionsGiven[modeKey]?.toString() ?: "0")
    StatDisplay(
        statTestTag = "successSessions${mode}Title",
        stat = "Successful: ",
        statValue = profile.statistics.successfulSessions[modeKey]?.toString() ?: "0")
  }
}
/** Row that contains the titles and stats for each mode, side by side */
@Composable
fun TitleAndStatsRow(profile: UserProfile) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.paddingMedium)
              .padding(bottom = paddingXXLarge),
      horizontalArrangement = Arrangement.SpaceEvenly) {
        ModeStatsColumn("InterviewTitle", "Interview", profile)
        ModeStatsColumn("SpeechTitle", "Speech", profile)
        ModeStatsColumn("NegotiationTitle", "Negotiation", profile)
      }
}
