package com.github.se.orator.ui.offline

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setup() {
    navigationActions = mock()
  }

  @Test
  fun testScreenElementsDisplayed() {
    composeTestRule.setContent { OfflineScreen(navigationActions = navigationActions) }

    // Verify "No Internet Connection" title is displayed
    composeTestRule.onNodeWithText("No Internet Connection").assertIsDisplayed()

    // Verify subtext message is displayed
    composeTestRule
        .onNodeWithText(
            "It seems like you don't have any WiFi connection... You can still practice offline!")
        .assertIsDisplayed()

    // Verify "Practice Offline" button is displayed
    composeTestRule.onNodeWithText("Practice Offline").assertIsDisplayed()
  }

  //  @Test
  //  fun testPracticeOfflineButtonNavigation() {
  //    composeTestRule.setContent { OfflineScreen(navigationActions = navigationActions) }
  //
  //    // Click on "Practice Offline" button and verify navigation action
  //    composeTestRule.onNodeWithText("Practice Offline").performClick()
  //    verify(navigationActions).navigateTo(Screen.OFFLINE_INTERVIEW_MODULE)
  //  }
}
