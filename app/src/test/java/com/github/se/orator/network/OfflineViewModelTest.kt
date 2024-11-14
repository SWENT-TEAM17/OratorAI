package com.github.se.orator.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class OfflineViewModelTest {

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

  private lateinit var offlineViewModel: OfflineViewModel

  @Mock private lateinit var observer: Observer<Boolean>

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
    offlineViewModel = OfflineViewModel()
    offlineViewModel.isOffline.observeForever(observer)
  }

  @Test
  fun `initial state is offline false`() {
    // Verify initial value of isOffline is false
    verify(observer).onChanged(false)
  }

  @Test
  fun `setOfflineMode updates isOffline to true`() {
    // Set offline mode to true and verify the observer is notified
    offlineViewModel.setOfflineMode(true)
    verify(observer).onChanged(true)
  }

  @Test
  fun `setOfflineMode updates isOffline to false`() {
    // Set offline mode to true
    offlineViewModel.setOfflineMode(true)

    // Clear previous invocations to avoid counting the initial false value
    clearInvocations(observer)

    // Set offline mode to false and verify the observer is notified only once
    offlineViewModel.setOfflineMode(false)
    verify(observer).onChanged(false)
  }
}
