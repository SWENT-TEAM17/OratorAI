package com.github.se.orator.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes

@Composable
fun BottomNavigationMenu(
    onTabSelect: (TopLevelDestination) -> Unit,
    tabList: List<TopLevelDestination>,
    selectedItem: String
) {
  val insets = WindowInsets.systemBars.asPaddingValues()

  BottomNavigation(
      modifier =
          Modifier.fillMaxWidth()
              .height(AppDimensions.bottomNavigationHeight + insets.calculateBottomPadding())
              .padding(bottom = insets.calculateBottomPadding())
              .testTag("bottomNavigationMenu"),
      backgroundColor = MaterialTheme.colorScheme.surface,
      content = {
        tabList.forEach { tab ->
          BottomNavigationItem(
              icon = {
                if (tab.route == selectedItem) {
                  Icon(
                      tab.coloredIcon,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.padding(AppDimensions.smallPadding))
                } else {
                  Icon(
                      tab.outlinedIcon,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.padding(AppDimensions.smallPadding))
                }
              },
              label = { Text(tab.textId, color = MaterialTheme.colorScheme.onSurface) },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              modifier = Modifier.clip(AppShapes.bottomNavigationItemShape).testTag(tab.textId))
        }
      },
  )
}
