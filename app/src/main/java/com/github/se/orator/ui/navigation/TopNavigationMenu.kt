package com.github.se.orator.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import com.github.se.orator.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationMenu(
    title: @Composable () -> Unit = {},
    actions: @Composable() (RowScope.() -> Unit) = {},
    navigationIcon: @Composable () -> Unit = {},
    testTag: String = "top_app_bar"
) {
  Column {
    CenterAlignedTopAppBar(
        modifier =
            Modifier.fillMaxWidth()
                .statusBarsPadding()
                .shadow(elevation = AppDimensions.elevationSmall)
                .testTag(testTag),
        title = title,
        actions = actions,
        navigationIcon = navigationIcon)
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
  }
}
