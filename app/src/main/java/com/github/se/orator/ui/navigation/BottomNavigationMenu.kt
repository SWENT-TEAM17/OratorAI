package com.github.se.orator.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
  BottomNavigation(
      modifier =
          Modifier.fillMaxWidth()
              .height(AppDimensions.bottomNavigationHeight)
              .testTag("bottomNavigationMenu"),
      backgroundColor = MaterialTheme.colorScheme.surface,
      content = {
        tabList.forEach { tab ->
          BottomNavigationItem(
              icon = { Icon(tab.icon, contentDescription = null) },
              label = { Text(tab.textId) },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              modifier = Modifier.clip(AppShapes.bottomNavigationItemShape).testTag(tab.textId))
        }
      },
  )
}
