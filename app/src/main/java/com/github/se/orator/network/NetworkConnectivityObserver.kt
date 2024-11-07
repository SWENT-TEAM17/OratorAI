package com.github.se.orator.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * NetworkConnectivityObserver is a class that observes network connectivity changes
 * and emits connectivity status updates to its subscribers.
 *
 * It uses the Android ConnectivityManager to monitor network status changes and
 * emits a ConnectivityObserver.Status indicating whether the network is available
 * or unavailable.
 *
 * @param context The application context, used to obtain the ConnectivityManager service.
 */
class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {

    // ConnectivityManager instance to monitor network changes.
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Starts observing network status changes and returns a Flow<ConnectivityObserver.Status>.
     *
     * The flow emits:
     * - ConnectivityObserver.Status.Available when the network is available with internet.
     * - ConnectivityObserver.Status.Unavailable when the network is lost or lacks internet capability.
     *
     * @return Flow<ConnectivityObserver.Status> emitting distinct network status updates.
     */
    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        // NetworkCallback to handle network changes and emit status updates.
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Emit "Available" status when the network is connected.
                trySend(ConnectivityObserver.Status.Available).isSuccess
            }

            override fun onLost(network: Network) {
                // Emit "Unavailable" status when the network connection is lost.
                trySend(ConnectivityObserver.Status.Unavailable).isSuccess
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // Emit "Available" status if the network has internet capability;
                // otherwise, emit "Unavailable".
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    trySend(ConnectivityObserver.Status.Available).isSuccess
                } else {
                    trySend(ConnectivityObserver.Status.Unavailable).isSuccess
                }
            }
        }

        // Register the callback with the ConnectivityManager to start monitoring network changes.
        connectivityManager.registerDefaultNetworkCallback(callback)

        // Ensure the callback is unregistered and resources are released when Flow collection stops.
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged() // Emit only when the status changes to avoid redundant emissions.
}
