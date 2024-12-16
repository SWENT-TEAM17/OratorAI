package com.github.se.orator.ui.settings

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.theme.AppThemeValue
import com.github.se.orator.model.theme.AppThemeViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class SettingsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel

  private var appThemeViewModel: AppThemeViewModel? = null

  private val correctTexts = listOf("Light", "Dark", "System default")

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
    userProfileRepository = mock(UserProfileRepository::class.java)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
  }

  @Test
  fun testBackButtonExistsAndClickable() {
    composeTestRule.setContent {
      SettingsScreen(
          navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
    }

    composeTestRule.onNodeWithTag("back_button").assertExists()
    composeTestRule.onNodeWithTag("back_button").performClick()
    verify(navigationActions).goBack() // Verify navigation back action
  }

  @Test
  fun testSettingsButtonsExist() {
    composeTestRule.setContent {
      appThemeViewModel = AppThemeViewModel(LocalContext.current)
      appThemeViewModel?.saveTheme(AppThemeValue.LIGHT)
      SettingsScreen(
          navigationActions = navigationActions,
          userProfileViewModel = userProfileViewModel,
          themeViewModel = appThemeViewModel)
    }

    // Test that each setting button exists and is clickable
    val settingsTags = listOf("theme", "permissions")

    settingsTags.forEach { tag ->
      composeTestRule.onNodeWithTag(tag).assertExists()
      composeTestRule.onNodeWithTag(tag).assertHasClickAction()
    }
  }

  @Test
  fun clickOnThemeButtonOpensDialog() {

    composeTestRule.setContent {
      appThemeViewModel = AppThemeViewModel(LocalContext.current)
      appThemeViewModel?.saveTheme(AppThemeValue.LIGHT)
      SettingsScreen(
          navigationActions = navigationActions,
          userProfileViewModel = userProfileViewModel,
          themeViewModel = appThemeViewModel)
    }

    // Open the theme dialog
    composeTestRule.onNodeWithTag("theme").assertExists()
    composeTestRule.onNodeWithTag("theme").assertHasClickAction()
    composeTestRule.onNodeWithTag("theme").performClick()

    composeTestRule.onNodeWithTag("settingsThemeDialog").assertIsDisplayed()
  }

  @Test
  fun changeThemeFromDialogWorks() {

    composeTestRule.setContent {
      appThemeViewModel = AppThemeViewModel(LocalContext.current)
      appThemeViewModel?.saveTheme(AppThemeValue.LIGHT)
      SettingsScreen(
          navigationActions = navigationActions,
          userProfileViewModel = userProfileViewModel,
          themeViewModel = appThemeViewModel)
    }

    // Check that each theme option exists and is clickable, and that the text is correctly
    // formatted
    AppThemeValue.entries.zip(correctTexts).forEach { (theme, textValue) ->
      // Open the text dialog
      composeTestRule.onNodeWithTag("theme").performClick()

      // Checks that the corresponding radio button exists and perform click action
      composeTestRule.onNodeWithTag("settingsThemeDialogRow#$theme").assertIsDisplayed()
      composeTestRule.onNodeWithTag("settingsThemeDialogRow#$theme").assertHasClickAction()
      composeTestRule.onNodeWithTag("settingsThemeDialogRow#$theme").performClick()

      // Checks the formatting of the text
      composeTestRule
          .onNodeWithTag("settingsThemeDialogText#$theme", useUnmergedTree = true)
          .assertTextContains(textValue)

      // Close the theme dialog by clicking the confirm button
      composeTestRule
          .onNodeWithTag("settingsThemeDialogConfirm", useUnmergedTree = true)
          .performClick()
      // Check that the theme value is saved correctly
      assert(appThemeViewModel?.currentTheme?.value == theme)
    }
  }

  @Test
  fun dismissDialogDoesNotSaveTheChanges() {

    composeTestRule.setContent {
      appThemeViewModel = AppThemeViewModel(LocalContext.current)
      appThemeViewModel?.saveTheme(AppThemeValue.LIGHT)
      SettingsScreen(
          navigationActions = navigationActions,
          userProfileViewModel = userProfileViewModel,
          themeViewModel = appThemeViewModel)
    }

    assert(appThemeViewModel?.currentTheme?.value == AppThemeValue.LIGHT)
    composeTestRule.onNodeWithTag("theme").performClick()
    composeTestRule.onNodeWithTag("settingsThemeDialogRow#${AppThemeValue.DARK}").performClick()
    // Close the theme dialog by clicking the dismiss button
    composeTestRule
        .onNodeWithTag("settingsThemeDialogCancel", useUnmergedTree = true)
        .performClick()
    assert(appThemeViewModel?.currentTheme?.value == AppThemeValue.LIGHT)
  }

  @Test
  fun noThemeViewModelDoesNotCauseACrash() {
    composeTestRule.setContent {
      appThemeViewModel = AppThemeViewModel(LocalContext.current)
      appThemeViewModel?.saveTheme(AppThemeValue.LIGHT)
      SettingsScreen(
          navigationActions = navigationActions,
          userProfileViewModel = userProfileViewModel,
          appThemeViewModel)
    }

    composeTestRule.onNodeWithTag("theme").performClick()
  }
}
