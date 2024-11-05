package com.github.se.orator.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.capture

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class NetworkConnectivityObserverTest {

    // Mock objects for testing
    @Mock private lateinit var mockContext: Context
    @Mock private lateinit var mockConnectivityManager: ConnectivityManager
    @Mock private lateinit var mockNetwork: Network
    @Mock private lateinit var mockNetworkCapabilities: NetworkCapabilities

    // Captor to capture NetworkCallback instances
    @Captor private lateinit var networkCallbackCaptor: ArgumentCaptor<ConnectivityManager.NetworkCallback>

    private lateinit var networkConnectivityObserver: NetworkConnectivityObserver
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // Initialize mocks and set up the main dispatcher for coroutines
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock the connectivity service to return our mocked connectivity manager
        `when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager)

        // Initialize the observer to be tested
        networkConnectivityObserver = NetworkConnectivityObserver(mockContext)
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher to the default after tests
        Dispatchers.resetMain()
    }

    @Test
    fun `registerDefaultNetworkCallback is called when observing network status`() = runTest {
        // Launch a coroutine to start collecting network status changes
        val job = launch {
            networkConnectivityObserver.observe().first() // Only collects the first emission
        }

        // Advance coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify if registerDefaultNetworkCallback was called
        verify(mockConnectivityManager).registerDefaultNetworkCallback(capture(networkCallbackCaptor))

        job.cancel() // Clean up the launched job
    }

    @Test
    fun `emits Available when network is available`() = runTest {
        // Collect network statuses in a list
        val collectedStatuses = mutableListOf<ConnectivityObserver.Status>()

        // Launch a coroutine to collect network status changes
        val job = launch {
            networkConnectivityObserver.observe().collect { collectedStatuses.add(it) }
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Capture the NetworkCallback passed to ConnectivityManager
        verify(mockConnectivityManager).registerDefaultNetworkCallback(capture(networkCallbackCaptor))
        val callback = networkCallbackCaptor.value

        // Simulate network availability
        callback.onAvailable(mockNetwork)

        // Advance coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        // Check that the status is correctly emitted as Available
        assertEquals(listOf(ConnectivityObserver.Status.Available), collectedStatuses)
    }

    @Test
    fun `emits Unavailable when network is lost`() = runTest {
        // Collect network statuses in a list
        val collectedStatuses = mutableListOf<ConnectivityObserver.Status>()

        // Launch a coroutine to collect network status changes
        val job = launch {
            networkConnectivityObserver.observe().collect { collectedStatuses.add(it) }
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Capture the NetworkCallback passed to ConnectivityManager
        verify(mockConnectivityManager).registerDefaultNetworkCallback(capture(networkCallbackCaptor))
        val callback = networkCallbackCaptor.value

        // Simulate network loss
        callback.onLost(mockNetwork)

        // Advance coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        // Check that the status is correctly emitted as Unavailable
        assertEquals(listOf(ConnectivityObserver.Status.Unavailable), collectedStatuses)
    }

    @Test
    fun `emits Available and then Unavailable when internet capability changes`() = runTest {
        // Collect network statuses in a list
        val collectedStatuses = mutableListOf<ConnectivityObserver.Status>()

        // Launch a coroutine to collect network status changes
        val job = launch {
            networkConnectivityObserver.observe().collect { collectedStatuses.add(it) }
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Capture the NetworkCallback passed to ConnectivityManager
        verify(mockConnectivityManager).registerDefaultNetworkCallback(capture(networkCallbackCaptor))
        val callback = networkCallbackCaptor.value

        // Simulate network with internet capability
        `when`(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)
        callback.onAvailable(mockNetwork)

        // Simulate loss of internet capability
        `when`(mockNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false)
        callback.onCapabilitiesChanged(mockNetwork, mockNetworkCapabilities)

        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        // Check that statuses are emitted as Available then Unavailable
        assertEquals(
            listOf(ConnectivityObserver.Status.Available, ConnectivityObserver.Status.Unavailable),
            collectedStatuses
        )
    }
}
