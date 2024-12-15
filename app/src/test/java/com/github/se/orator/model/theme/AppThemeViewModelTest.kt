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

class AppThemeValueViewModelTest {

  @Mock private lateinit var mockContext: Context
  @Mock private lateinit var mockSharedPreferences: SharedPreferences
  @Mock private lateinit var mockEditor: SharedPreferences.Editor

  private lateinit var appThemeViewModel: AppThemeViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(mockContext.getSharedPreferences("isDark", Context.MODE_PRIVATE))
        .thenReturn(mockSharedPreferences)
    `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
    `when`(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)

    appThemeViewModel = AppThemeViewModel(mockContext)

    // We simulate a value stored in memory equal to "false" in all cases.
    `when`(mockSharedPreferences.getBoolean(any(), any())).thenReturn(false)

    appThemeViewModel.loadTheme(false)
  }

  @Test
  fun loadThemeCorrectlyRetrievesExistingSavedThemeFromMemory() {
    verify(mockSharedPreferences).getString(eq("isDark"), eq(AppThemeValue.DARK.toString()))
    assert(!appThemeViewModel.currentTheme.value)
  }

  @Test
  fun changingTheThemeToTheSameValueAsCurrentDoesNothing() {
    assert(!appThemeViewModel.currentTheme.value)
    appThemeViewModel.saveTheme(false)

    // The value stored in memory was already false.
    verify(mockSharedPreferences, never()).edit()
    assert(!appThemeViewModel.currentTheme.value)
  }

  @Test
  fun changingTheThemeToADifferentValueSavesTheNewValue() {
    assert(!appThemeViewModel.currentTheme.value)

    appThemeViewModel.saveTheme(true)

    verify(mockSharedPreferences).edit()
    verify(mockEditor).putBoolean(eq("isDark"), eq(true))
    verify(mockEditor).apply()

    assert(appThemeViewModel.currentTheme.value)
  }

  @Test
  fun switchingTheThemeChangesTheTheme() {
    assert(!appThemeViewModel.currentTheme.value)

    appThemeViewModel.switchTheme()

    verify(mockSharedPreferences).edit()
    verify(mockEditor).putBoolean(eq("isDark"), eq(true))
    verify(mockEditor).apply()

    assert(appThemeViewModel.currentTheme.value)
  }
}
