package com.github.se.orator.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.mainScreen.AnimatedCards
import com.github.se.orator.ui.mainScreen.ButtonRow
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
import com.github.se.orator.ui.theme.AppDimensions.paddingMedium
import com.github.se.orator.ui.theme.AppDimensions.paddingSmall
import com.github.se.orator.ui.theme.AppTypography

@Composable
fun GraphStats(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {
    val xValues = (1..10).toList()
    val yValuesTalkTimeSec = listOf(10f, 20f, 15f, 25f, 30f, 10f, 5f, 40f, 35f, 20f)
    val yValuesTalkTimePerc = listOf(20f, 20f, 15f, 25f, 30f, 10f, 15f, 35f, 30f, 25f)

    //val yValuesTalkTimeSec = profileViewModel.recentData.toList().map {data -> data.talkTimeSeconds}
    //val yValuesTalkTimePerc = profileViewModel.recentData.map {data -> data.talkTimePercentage}

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(
                    modifier =
                    Modifier.padding(start = AppDimensions.paddingXXLarge)
                        .padding(top = AppDimensions.paddingXXXLarge)
                        .testTag("graphScreenTitle"),
                    text = "Statistics Graph",
                    style = AppTypography.largeTitleStyle, // Apply custom style for title
                )

                Text(
                    modifier =
                    Modifier.padding(start = AppDimensions.paddingXXLarge)
                        .padding(top = AppDimensions.paddingExtraLarge)
                        .testTag("talkTimeSecTitle"),
                    text = "Talk Time Seconds",
                    style = AppTypography.smallTitleStyle, // Apply custom style for title
                )

                Row() {
                    Column(
                        modifier = Modifier.padding(
                            top = paddingExtraLarge,
                            bottom = paddingExtraLarge,
                            start = paddingExtraLarge
                        )
                    ) {
                        LineChart(xValues, yValuesTalkTimeSec)
                    }

                    Column(
                        modifier = Modifier.padding(
                            top = paddingExtraLarge,
                            bottom = paddingExtraLarge,
                            start = paddingExtraLarge
                        )
                    ) {
                        Text(
                            modifier =
                            Modifier.padding(start = AppDimensions.paddingLarge)
                                .padding(top = AppDimensions.paddingExtraLarge)
                                .testTag("talkTimeSecMeanTitle"),
                            text = "Mean:",
                            style = AppTypography.smallTitleStyle, // Apply custom style for title
                        )
                    }
                }

                Text(
                    modifier =
                    Modifier.padding(start = AppDimensions.paddingXXLarge)
                        .padding(top = AppDimensions.paddingExtraLarge)
                        .testTag("talkTimePercTitle"),
                    text = "Talk Time Percentage",
                    style = AppTypography.smallTitleStyle, // Apply custom style for title
                )

                Row() {
                    Column(
                        modifier = Modifier.padding(
                            top = paddingExtraLarge,
                            bottom = paddingExtraLarge,
                            start = paddingExtraLarge
                        )
                    ) {
                        LineChart(xValues, yValuesTalkTimePerc)
                    }

                    Column(
                        modifier = Modifier.padding(
                            top = paddingExtraLarge,
                            bottom = paddingExtraLarge,
                            start = paddingExtraLarge
                        )
                    ) {
                        Text(
                            modifier =
                            Modifier.padding(start = AppDimensions.paddingLarge)
                                .padding(top = AppDimensions.paddingExtraLarge)
                                .testTag("talkTimePercMeanTitle"),
                            text = "Mean:",
                            style = AppTypography.smallTitleStyle, // Apply custom style for title
                        )
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
        modifier = Modifier
            .width(graphWidth) // Set smaller width
            .height(graphHeight) // Set smaller height
            .padding(start = paddingExtraLarge, top = paddingSmall)
    ) {
        val maxX = xValues.maxOrNull()?.toFloat() ?: 1f
        val maxY = yValues.maxOrNull() ?: 1f
        val minY = yValues.minOrNull() ?: 0f
        val yRange = maxY - minY

        val xStep = size.width / (xValues.size - 1)
        val yScale = size.height / yRange

        // Draw Axes
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 10f
        )
        drawLine(
            color = Color.Black,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 10f
        )

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
                strokeWidth = 5f
            )
        }

        // Draw Points
        for (i in xValues.indices) {
            val x = i * xStep
            val y = size.height - (yValues[i] - minY) * yScale
            drawCircle(
                color = graphDots,
                center = Offset(x, y),
                radius = 7f // Smaller point size
            )
        }
    }
}
