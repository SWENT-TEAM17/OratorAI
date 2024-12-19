package com.github.se.orator.model.theme

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class AppThemeViewModelTest {

  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockSharedPreferences: SharedPreferences
  @Mock private lateinit var mockEditor: SharedPreferences.Editor

  private lateinit var appThemeViewModel: AppThemeViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(mockContext.getSharedPreferences("themeValueStorage", Context.MODE_PRIVATE))
        .thenReturn(mockSharedPreferences)
    `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
    `when`(mockEditor.putString(any(), any())).thenReturn(mockEditor)

    appThemeViewModel = AppThemeViewModel(mockContext)

    // We simulate a value stored in memory equal to "false" in all cases.
    `when`(mockSharedPreferences.getString(any(), any())).thenReturn(AppThemeValue.LIGHT.toString())

    appThemeViewModel.loadTheme(false)
  }

  @Test
  fun loadThemeCorrectlyRetrievesExistingSavedThemeFromMemory() {
    verify(mockSharedPreferences)
        .getString(eq("themeValueStorage"), eq(AppThemeValue.SYSTEM_DEFAULT.toString()))
    assert(!appThemeViewModel.isDark.value)
  }

  @Test
  fun changingTheThemeToTheSameValueAsCurrentDoesNothing() {
    assert(!appThemeViewModel.isDark.value)
    appThemeViewModel.saveTheme(AppThemeValue.LIGHT)

    // The value stored in memory was already false.
    verify(mockSharedPreferences, never()).edit()
    assert(!appThemeViewModel.isDark.value)
  }

  @Test
  fun changingTheThemeToADifferentValueSavesTheNewValue() {
    assert(!appThemeViewModel.isDark.value)

    appThemeViewModel.saveTheme(AppThemeValue.DARK)

    verify(mockSharedPreferences).edit()
    verify(mockEditor).putString(eq("themeValueStorage"), eq(AppThemeValue.DARK.toString()))
    verify(mockEditor).apply()

    assert(appThemeViewModel.isDark.value)
  }

  @Test
  fun switchingTheThemeChangesTheTheme() {
    assert(!appThemeViewModel.isDark.value)

    appThemeViewModel.switchTheme()

    verify(mockSharedPreferences).edit()
    verify(mockEditor).putString(eq("themeValueStorage"), eq(AppThemeValue.DARK.toString()))
    verify(mockEditor).apply()

    assert(appThemeViewModel.isDark.value)
  }

  @Test
  fun invalidMemoryValueDefaultsToSystemDefault() {
    assert(!appThemeViewModel.isDark.value)

    `when`(mockSharedPreferences.getString(any(), any())).thenReturn("invalid value")

    appThemeViewModel.loadTheme(true)

    assert(appThemeViewModel.isDark.value)
  }
}
