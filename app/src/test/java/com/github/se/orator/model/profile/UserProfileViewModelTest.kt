package com.github.se.orator.model.profile

import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class UserProfileViewModelTest {

  @Mock private lateinit var repository: UserProfileRepositoryFirestore

  private lateinit var viewModel: UserProfileViewModel

  private val testDispatcher = StandardTestDispatcher()

  private val testUid = "testUid"
  private val testUserProfile =
      UserProfile(
          uid = testUid,
          name = "Test User",
          age = 25,
          statistics = UserStatistics(),
          friends = listOf("friend1", "friend2"),
          bio = "Test bio")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    Dispatchers.setMain(testDispatcher)

    `when`(repository.getCurrentUserUid()).thenReturn(testUid)
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(testUserProfile)
        }
        .`when`(repository)
        .getUserProfile(any(), any(), any())

    viewModel = UserProfileViewModel(repository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `init should fetch user profile if uid is not null`() = runTest {
    verify(repository).getUserProfile(eq(testUid), any(), any())
    testDispatcher.scheduler.advanceUntilIdle()
    val userProfile = viewModel.userProfile.first()
    Assert.assertEquals(testUserProfile, userProfile)
  }

  @Test
  fun `createOrUpdateUserProfile should call addUserProfile when userProfile is null`() = runTest {
    // Simulate that getUserProfile returns null (no existing profile)
    doAnswer {
          val onSuccess = it.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(null) // Simulate no profile exists
          null
        }
        .`when`(repository)
        .getUserProfile(any(), any(), any())

    // Create a new user profile to add
    val newUserProfile =
        UserProfile(uid = testUid, name = "New User", age = 30, statistics = UserStatistics())

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(repository)
        .addUserProfile(any(), any(), any())

    // Reinitialize the ViewModel to ensure userProfile_ is null
    viewModel = UserProfileViewModel(repository)

    // Call the method that should trigger addUserProfile
    viewModel.createOrUpdateUserProfile(newUserProfile)

    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that addUserProfile was called, not updateUserProfile
    verify(repository).addUserProfile(eq(newUserProfile), any(), any())
  }

  @Test
  fun `createOrUpdateUserProfile should call updateUserProfile when userProfile is not null`() =
      runTest {
        val updatedUserProfile = testUserProfile.copy(name = "Updated User")

        doAnswer {
              val onSuccess = it.getArgument<() -> Unit>(1)
              onSuccess()
              null
            }
            .`when`(repository)
            .updateUserProfile(any(), any(), any())

        // Ensure userProfile_ is set to the testUserProfile
        viewModel.createOrUpdateUserProfile(updatedUserProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).updateUserProfile(eq(updatedUserProfile), any(), any())
      }

  @Test
  fun `isProfileIncomplete should return true if profile is null`() = runTest {
    // Simulate loading is complete and profile is null
    `when`(repository.getCurrentUserUid()).thenReturn(null)

    viewModel = UserProfileViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.isProfileIncomplete()
    Assert.assertTrue(result)
  }

  @Test
  fun `isProfileIncomplete should return true if name is blank`() = runTest {
    val incompleteProfile = testUserProfile.copy(name = "")
    `when`(repository.getCurrentUserUid()).thenReturn(testUid)
    doAnswer {
          val onSuccess = it.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(incompleteProfile)
          null
        }
        .`when`(repository)
        .getUserProfile(any(), any(), any())

    // Set up the ViewModel
    viewModel = UserProfileViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    val result = viewModel.isProfileIncomplete()
    Assert.assertTrue(result)
  }

  @Test
  fun `uploadProfilePicture should call repository upload and update methods`() = runTest {
    val imageUri = mock(Uri::class.java)
    val downloadUrl = "http://example.com/profile.jpg"

    doAnswer {
          val onSuccess = it.getArgument<(String) -> Unit>(2)
          onSuccess(downloadUrl)
          null
        }
        .`when`(repository)
        .uploadProfilePicture(any(), any(), any(), any())

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(2)
          onSuccess()
          null
        }
        .`when`(repository)
        .updateUserProfilePicture(any(), any(), any(), any())

    viewModel.uploadProfilePicture(testUid, imageUri)
    testDispatcher.scheduler.advanceUntilIdle()

    verify(repository).uploadProfilePicture(eq(testUid), eq(imageUri), any(), any())
    verify(repository).updateUserProfilePicture(eq(testUid), eq(downloadUrl), any(), any())
  }

  @Test
  fun `selectFriend should update selectedFriend`() = runTest {
    val friendProfile =
        UserProfile(
            uid = "friendUid", name = "Friend User", age = 24, statistics = UserStatistics())

    viewModel.selectFriend(friendProfile)
    testDispatcher.scheduler.advanceUntilIdle()

    val selectedFriend = viewModel.selectedFriend.first()
    Assert.assertEquals(friendProfile, selectedFriend)
  }
}
