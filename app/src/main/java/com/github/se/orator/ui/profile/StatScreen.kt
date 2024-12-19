package com.github.se.orator.ui.profile

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.theme.AppColors.axisColor
import com.github.se.orator.ui.theme.AppColors.graphDots
import com.github.se.orator.ui.theme.AppColors.secondaryColor
import com.github.se.orator.ui.theme.AppColors.tickLabelColor
import com.github.se.orator.ui.theme.AppColors.tickLineColor
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
import com.github.se.orator.ui.theme.AppDimensions.paddingLarge
import com.github.se.orator.ui.theme.AppDimensions.paddingMedium
import com.github.se.orator.ui.theme.AppDimensions.paddingSmall
import com.github.se.orator.ui.theme.AppDimensions.paddingXXLarge
import com.github.se.orator.ui.theme.AppTypography

const val TICK_COUNT = 5

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GraphStats(profileViewModel: UserProfileViewModel) {

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()

  val xValues = (1..10).toList()

  Column(
      modifier =
          Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(paddingMedium)) {
        if (userProfile == null) {
          Log.e("GraphStats", "userProfile is null. Cannot render stats.")
        } else {
          userProfile?.let { profile ->
            Text(
                modifier =
                    Modifier.padding(start = paddingXXLarge, top = paddingMedium)
                        .testTag("graphScreenTitle"),
                textAlign = TextAlign.Center,
                text = "Your Stats",
                style = AppTypography.mediumTitleStyle,
                color = MaterialTheme.colorScheme.primary)

            Text(
                modifier =
                    Modifier.padding(start = paddingXXLarge, top = paddingSmall)
                        .testTag("talkTimeSecTitle"),
                text = "Talk Time Seconds:",
                style = AppTypography.smallTitleStyle,
                color = MaterialTheme.colorScheme.onSurface)

            Column(modifier = Modifier.padding(paddingLarge)) {
              LineChart(
                  xValues,
                  profileViewModel.ensureListSizeTen(
                      profile.statistics.recentData.map { data -> data.talkTimeSeconds.toFloat() }),
                  "talkTimeSecGraph")
              Text(
                  modifier = Modifier.padding(top = paddingSmall).testTag("talkTimeSecMeanTitle"),
                  text = "Mean: ${profile.statistics.talkTimeSecMean}",
                  style = AppTypography.smallTitleStyle,
                  color = MaterialTheme.colorScheme.onSurface)
            }

            Text(
                modifier =
                    Modifier.padding(start = paddingXXLarge, top = paddingMedium)
                        .testTag("paceTitle"),
                text = "Pace:",
                style = AppTypography.smallTitleStyle,
                color = MaterialTheme.colorScheme.onSurface)

            Column(modifier = Modifier.padding(paddingSmall)) {
              LineChart(
                  xValues,
                  profileViewModel.ensureListSizeTen(
                      profile.statistics.recentData.map { data -> data.pace.toFloat() }),
                  "paceGraph")
              Text(
                  modifier = Modifier.padding(top = paddingSmall).testTag("paceMeanTitle"),
                  text = "Mean: ${profile.statistics.paceMean}",
                  style = AppTypography.smallTitleStyle,
                  color = MaterialTheme.colorScheme.onSurface)
            }

            Column(modifier = Modifier.fillMaxWidth().padding(paddingSmall)) {
              TitleAndStatsRow(profile)
            }
          }
        }
      }
}

@Composable
fun LineChart(xValues: List<Int>, yValues: List<Float>, testTag: String) {
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
  Text(
      modifier =
          Modifier.padding(start = paddingSmall)
              .padding(top = paddingXXLarge)
              .testTag(modeTitleTestTag),
      text = mode,
      style = AppTypography.smallTitleStyle, // Apply custom style for title
      color = MaterialTheme.colorScheme.primary)
}

/** Composable for stats displays on the stats screen */
@Composable
fun StatDisplay(statTestTag: String, stat: String, statValue: String) {
  Text(
      modifier =
          Modifier.padding(start = paddingSmall).padding(top = paddingSmall).testTag(statTestTag),
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
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = paddingMedium)
              .padding(bottom = paddingXXLarge),
      verticalArrangement = Arrangement.SpaceEvenly) {
        ModeStatsColumn("InterviewTitle", "Interview", profile)
        ModeStatsColumn("SpeechTitle", "Speech", profile)
        ModeStatsColumn("NegotiationTitle", "Negotiation", profile)
      }
}
