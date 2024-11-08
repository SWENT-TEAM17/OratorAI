package com.github.se.orator.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * NetworkChangeReceiver is a BroadcastReceiver that listens for network connectivity changes and
 * updates the isNetworkAvailable StateFlow to reflect the current network status.
 *
 * This receiver allows the app to observe changes in network connectivity in real-time and react
 * accordingly, such as switching between online and offline modes.
 */
class NetworkConnectivityObserver : BroadcastReceiver() {

  // MutableStateFlow to hold the current network status (true for online, false for offline)
  companion object {
    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable // Public read-only StateFlow
  }

  /**
   * This method is triggered whenever the network connectivity changes. It updates the
   * isNetworkAvailable StateFlow based on the current network status.
   *
   * @param context The Context in which the receiver is running.
   * @param intent The Intent being received, which holds the connectivity information.
   *
   * When the system detects a change in network connectivity, it broadcasts an implicit intent that
   * is sent to any BroadcastReceiver registered to listen for it.
   */
  override fun onReceive(context: Context, intent: Intent) {
    val isConnected = isConnected(context)
    // Only update StateFlow if there's an actual change
    if (_isNetworkAvailable.value != isConnected) {
      _isNetworkAvailable.value = isConnected
    }
  }

  /**
   * Checks the network connectivity status.
   *
   * @param context The context used to access system services.
   * @return Boolean indicating whether the device is connected to a network with internet access.
   */
  private fun isConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
