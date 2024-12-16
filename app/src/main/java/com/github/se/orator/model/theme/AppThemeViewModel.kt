package com.github.se.orator.model.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Enum class holding the possible values for the app theme. */
enum class AppThemeValue {
  LIGHT,
  DARK,
  SYSTEM_DEFAULT
}

/**
 * ViewModel managing the current theme of the app.
 *
 * @param context The context of the app.
 */
class AppThemeViewModel(private val context: Context) {

  private val THEME_STORAGE_LABEL = "themeValueStorage"

  private val _currentTheme = MutableStateFlow(AppThemeValue.SYSTEM_DEFAULT)
  /**
   * StateFlow holding the `AppThemeValue` describing the current theme. Use the `isDark` object if
   * you only need to know if the device is currently in dark or light theme, without caring about
   * if the theme is the system default one or instead chosen by the user.
   */
  val currentTheme = _currentTheme.asStateFlow()

  private val _isDark = MutableStateFlow(false)
  /**
   * StateFlow holding the value `true` if the app is in dark mode, `false` otherwise. This
   * attribute takes into account the case where the user selected the system default theme :
   * `isDark` is updated automatically.
   */
  val isDark = _isDark.asStateFlow()

  // Saves the theme of the device when `loadTheme` is called.
  private var deviceThemeIsDarkTheme = false

  /**
   * Load the theme from memory. If no value present, the theme of the device is used, and saved as
   * the current theme in memory.
   *
   * @param deviceDefaultThemeIsDark The theme of the device.
   */
  fun loadTheme(deviceDefaultThemeIsDark: Boolean) {
    deviceThemeIsDarkTheme = deviceDefaultThemeIsDark

    val preference =
        context.getSharedPreferences( // NOSONAR - This is not sensitive information
            THEME_STORAGE_LABEL, Context.MODE_PRIVATE)

    _currentTheme.value =
        AppThemeValue.valueOf(
            preference.getString(THEME_STORAGE_LABEL, AppThemeValue.SYSTEM_DEFAULT.toString())!!)
    chooseTheme()
  }

  /**
   * Change the theme and save the value to memory, if it is not the same as previously.
   *
   * @param themeValue The new theme to save.
   */
  fun saveTheme(themeValue: AppThemeValue) {
    val preference =
        context.getSharedPreferences( // NOSONAR - This is not sensitive information
            THEME_STORAGE_LABEL, Context.MODE_PRIVATE)
    if (themeValue != _currentTheme.value) {
      preference.edit().putString(THEME_STORAGE_LABEL, themeValue.toString()).apply()
      _currentTheme.value = themeValue
      chooseTheme()
    }
  }

  /**
   * Switch the theme of the app, from dark to light or light to dark depending on the current
   * theme. If the current theme is the system default, the theme is switched to its opposite.
   */
  fun switchTheme() {
    when (_currentTheme.value) {
      AppThemeValue.LIGHT -> saveTheme(AppThemeValue.DARK)
      AppThemeValue.DARK -> saveTheme(AppThemeValue.LIGHT)
      AppThemeValue.SYSTEM_DEFAULT ->
          saveTheme(if (deviceThemeIsDarkTheme) AppThemeValue.DARK else AppThemeValue.LIGHT)
    }
  }

  /**
   * Logic determining which theme to use, differentiating between the system default and the
   * user-selected theme. This updates the `isDark` object.
   */
  private fun chooseTheme() {
    _isDark.value =
        when (_currentTheme.value) {
          AppThemeValue.LIGHT -> false
          AppThemeValue.DARK -> true
          AppThemeValue.SYSTEM_DEFAULT -> deviceThemeIsDarkTheme
        }
  }
}
