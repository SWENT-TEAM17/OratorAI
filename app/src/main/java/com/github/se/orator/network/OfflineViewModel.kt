package com.github.se.orator.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel to manage the state of offline mode and any related logic. It exposes a LiveData to
 * observe the offline state and handles any data or actions needed when the app is offline.
 */
class OfflineViewModel : ViewModel() {

  // LiveData to observe whether the app is in offline mode
  private val _isOffline = MutableLiveData(false)
  val isOffline: LiveData<Boolean>
    get() = _isOffline

  /**
   * Sets the app's offline status. This can be triggered by network changes.
   *
   * @param isOffline Boolean representing whether the app should be in offline mode.
   */
  fun setOfflineMode(isOffline: Boolean) {
    _isOffline.value = isOffline
  }
}
