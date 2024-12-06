package com.github.se.orator.model.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel managing the current theme of the app.
 *
 * @param context The context of the app.
 */
class AppThemeViewModel(private val context: Context) {

  private val IS_DARK_NAME = "isDark"

  private val _isDark = MutableStateFlow(false)
  /** StateFlow holding the value `true` if the app is in dark mode, `false` otherwise. */
  val isDark = _isDark.asStateFlow()

  private var deviceThemeIsDarkTheme = false

  /**
   * Load the theme from memory. If no value present, the theme of the device is used, and saved as
   * the current theme in memory.
   *
   * @param deviceThemeIsDarkTheme The theme of the device.
   */
  fun loadTheme(deviceThemeIsDarkTheme: Boolean) {
    val preference =
        context.getSharedPreferences( // NOSONAR - This is not sensitive information
            "isDark", Context.MODE_PRIVATE)
    _isDark.value = preference.getBoolean(IS_DARK_NAME, deviceThemeIsDarkTheme)
    this.deviceThemeIsDarkTheme = deviceThemeIsDarkTheme
  }

  /**
   * Change the theme and save the value to memory, if it is not the same as previously.
   *
   * @param isDark `true` if the new theme need to be the dark theme.
   */
  fun saveTheme(isDark: Boolean) {
    val preference =
        context.getSharedPreferences( // NOSONAR - This is not sensitive information
            "isDark", Context.MODE_PRIVATE)
    if (isDark != this.isDark.value) {
      preference.edit().putBoolean(IS_DARK_NAME, isDark).apply()
      _isDark.value = isDark
    }
  }

  /**
   * Switch the theme of the app, from dark to light or light to dark depending on the current
   * theme.
   */
  fun switchTheme() {
    saveTheme(!isDark.value)
  }
}
