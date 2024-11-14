package com.github.se.orator.ui.offline

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.ui.speaking.OfflineSpeakingViewModel

class OfflineSpeakingViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(OfflineSpeakingViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST") return OfflineSpeakingViewModel(context) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
