package com.github.se.orator.network

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class NetworkConnectivityObserverTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockConnectivityManager: ConnectivityManager

    @Mock
    private lateinit var mockNetworkCapabilities: NetworkCapabilities

    @Mock
    private lateinit var mockNetwork: Network

    private lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    private lateinit var autoCloseable: AutoCloseable

    @Before
    fun setUp() {
        // Initialize the mocks and NetworkChangeReceiver
        autoCloseable = MockitoAnnotations.openMocks(this)
        networkConnectivityObserver = NetworkConnectivityObserver()

        // Set up the context to return the mocked ConnectivityManager
        `when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)
    }

    @After
    fun tearDown() {
        // Close any open mocks to avoid memory leaks
        autoCloseable.close()
    }

    @Test
    fun `test network available`() = runBlockingTest {
        // Set up mocks to simulate a connected network with internet capability
        `when`(mockConnectivityManager.activeNetwork).thenReturn(mockNetwork)
        `when`(mockConnectivityManager.getNetworkCapabilities(mockNetwork)).thenReturn(mockNetworkCapabilities)
        `when`(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)

        // Trigger the receiver with an Intent
        networkConnectivityObserver.onReceive(mockContext, Intent())

        // Assert that isNetworkAvailable is true
        val isConnected = NetworkConnectivityObserver.isNetworkAvailable.first() // Get the first emitted value
        assertEquals(true, isConnected)
    }

    @Test
    fun `test network not available`() = runBlockingTest {
        // Set up mocks to simulate no active network
        `when`(mockConnectivityManager.activeNetwork).thenReturn(null)

        // Trigger the receiver with an Intent
        networkConnectivityObserver.onReceive(mockContext, Intent())

        // Assert that isNetworkAvailable is false
        val isConnected = NetworkConnectivityObserver.isNetworkAvailable.first() // Get the first emitted value
        assertEquals(false, isConnected)
    }

    @Test
    fun `test network available but no internet capability`() = runBlockingTest {
        // Set up mocks to simulate a network without internet capability
        `when`(mockConnectivityManager.activeNetwork).thenReturn(mockNetwork)
        `when`(mockConnectivityManager.getNetworkCapabilities(mockNetwork)).thenReturn(mockNetworkCapabilities)
        `when`(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false)

        // Trigger the receiver with an Intent
        networkConnectivityObserver.onReceive(mockContext, Intent())

        // Assert that isNetworkAvailable is false
        val isConnected = NetworkConnectivityObserver.isNetworkAvailable.first() // Get the first emitted value
        assertEquals(false, isConnected)
    }
}

