package com.github.se.orator.model.profile

import android.content.Context
import android.net.Uri
import com.github.se.orator.model.speaking.AnalysisData
import java.io.FilenameFilter
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
  private lateinit var mockContext: Context
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
    mockContext = mock(Context::class.java)
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

  @Test
  fun `updateLoginStreak should call repository to update streak`() = runTest {
    `when`(repository.getCurrentUserUid()).thenReturn(testUid)

    doAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
          null
        }
        .`when`(repository)
        .updateLoginStreak(any(), any(), any())

    // Reset interactions to exclude calls from ViewModel initialization
    clearInvocations(repository)

    viewModel.updateLoginStreak()
    testDispatcher.scheduler.advanceUntilIdle()

    verify(repository).updateLoginStreak(eq(testUid), any(), any())
    verify(repository).getUserProfile(eq(testUid), any(), any())
  }

  @Test
  fun testLoadSavedRecordings() = runTest {
    // Mock filesDir and listFiles for the Context
    val mockFilesDir = mock(java.io.File::class.java)
    `when`(mockContext.filesDir).thenReturn(mockFilesDir)

    val mockFile1 = mock(java.io.File::class.java)
    val mockFile2 = mock(java.io.File::class.java)

    // Mock file names and directory listing
    `when`(mockFile1.name).thenReturn("recording1.wav")
    `when`(mockFile2.name).thenReturn("recording2.wav")
    `when`(mockFilesDir.listFiles(any<FilenameFilter>())).thenReturn(arrayOf(mockFile1, mockFile2))
    // Call the method to load saved recordings
    viewModel.loadSavedRecordings(mockContext)

    // Assert that the ViewModel has updated the savedRecordings StateFlow
    val savedRecordings = viewModel.savedRecordings.value
    assert(savedRecordings.size == 2)
    assert(savedRecordings.contains(mockFile1))
    assert(savedRecordings.contains(mockFile2))

    // Verify the mocked interactions
    verify(mockContext).filesDir
    verify(mockFilesDir).listFiles(any<FilenameFilter>())
  }

  @Test
  fun `addNewestData should add new data to recentData and update user profile`() = runTest {
    // Arrange
    val newAnalysisData =
        AnalysisData(
            transcription = "a",
            fillerWordsCount = 0,
            averagePauseDuration = 0.0,
            talkTimeSeconds = 100.0,
            talkTimePercentage = 50.0,
            pace = 0)

    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }
        .`when`(repository)
        .updateUserProfile(any(), any(), any())

    // Act
    viewModel.addNewestData(newAnalysisData)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val recentData = viewModel.recentData.first()
    Assert.assertEquals(1, recentData.size)
    Assert.assertEquals(newAnalysisData, recentData.last())

    verify(repository).updateUserProfile(any(), any(), any())
  }

  @Test
  fun `addNewestData should maintain a maximum size of 10 in recentData`() = runTest {
    // Arrange
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }
        .`when`(repository)
        .updateUserProfile(any(), any(), any())

    // Prepopulate recentData with 10 items
    for (i in 1..10) {
      val analysisData =
          AnalysisData(
              transcription = "a",
              fillerWordsCount = 0,
              averagePauseDuration = 0.0,
              talkTimeSeconds = i.toDouble(),
              talkTimePercentage = i.toDouble(),
              pace = 0)
      viewModel.addNewestData(analysisData)
    }
    testDispatcher.scheduler.advanceUntilIdle()

    val initialRecentData = viewModel.recentData.first()
    Assert.assertEquals(10, initialRecentData.size)
    Assert.assertEquals(1.0, initialRecentData.first().talkTimeSeconds, 0.0)

    // Act
    val newAnalysisData =
        AnalysisData(
            transcription = "a",
            fillerWordsCount = 0,
            averagePauseDuration = 0.0,
            talkTimeSeconds = 11.0,
            talkTimePercentage = 11.0,
            pace = 0)
    viewModel.addNewestData(newAnalysisData)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val updatedRecentData = viewModel.recentData.first()
    Assert.assertEquals(10, updatedRecentData.size)
    Assert.assertEquals(2.0, updatedRecentData.first().talkTimeSeconds, 0.0)
    Assert.assertEquals(newAnalysisData, updatedRecentData.last())

    verify(repository, times(11)).updateUserProfile(any(), any(), any())
  }

  @Test
  fun `addNewestData should not add data when userProfile is null`() = runTest {
    // Arrange
    // Mock the repository to return null for getUserProfile
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(null)
        }
        .`when`(repository)
        .getUserProfile(any(), any(), any())

    // Re-initialize viewModel to use the mocked getUserProfile
    viewModel = UserProfileViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Confirm that userProfile is null
    Assert.assertNull(viewModel.userProfile.first())

    // Act
    val newAnalysisData =
        AnalysisData(
            transcription = "a",
            fillerWordsCount = 0,
            averagePauseDuration = 0.0,
            talkTimeSeconds = 50.0,
            talkTimePercentage = 50.0,
            pace = 0)
    viewModel.addNewestData(newAnalysisData)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val recentData = viewModel.recentData.first()
    Assert.assertTrue(recentData.isEmpty())

    verify(repository, never()).updateUserProfile(any(), any(), any())
  }

  @Test
  fun `updateMetricMean should calculate means and update user profile`() = runTest {
    // Arrange
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }
        .`when`(repository)
        .updateUserProfile(any(), any(), any())

    // Add several AnalysisData items
    val analysisDataList =
        listOf(
            AnalysisData(
                transcription = "a",
                fillerWordsCount = 0,
                averagePauseDuration = 0.0,
                talkTimeSeconds = 10.0,
                talkTimePercentage = 20.0,
                pace = 0),
            AnalysisData(
                transcription = "a",
                fillerWordsCount = 0,
                averagePauseDuration = 0.0,
                talkTimeSeconds = 20.0,
                talkTimePercentage = 30.0,
                pace = 0),
            AnalysisData(
                transcription = "a",
                fillerWordsCount = 0,
                averagePauseDuration = 0.0,
                talkTimeSeconds = 30.0,
                talkTimePercentage = 40.0,
                pace = 0))

    analysisDataList.forEach { data -> viewModel.addNewestData(data) }

    testDispatcher.scheduler.advanceUntilIdle()

    // Mock getMetricMean to return the average
    `when`(repository.getMetricMean(any())).thenAnswer { invocation ->
      val list = invocation.getArgument<List<Double>>(0)
      list.average()
    }

    // Clear previous interactions with the repository
    clearInvocations(repository)
    // Act
    viewModel.updateMetricMean()
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val updatedUserProfile = viewModel.userProfile.first()
    val expectedTalkTimeSecMean = analysisDataList.map { it.talkTimeSeconds }.average()
    val expectedTalkTimePercMean = analysisDataList.map { it.talkTimePercentage }.average()

    updatedUserProfile?.statistics?.talkTimeSecMean?.let {
      Assert.assertEquals(expectedTalkTimeSecMean, it, 0.001)
    }
    updatedUserProfile?.statistics?.talkTimePercMean?.let {
      Assert.assertEquals(expectedTalkTimePercMean, it, 0.001)
    }

    verify(repository).updateUserProfile(any(), any(), any())
  }

  @Test
  fun `updateMetricMean should not update user profile when userProfile is null`() = runTest {
    // Arrange
    // Mock the repository to return null for getUserProfile
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
          onSuccess(null)
        }
        .`when`(repository)
        .getUserProfile(any(), any(), any())

    // Re-initialize viewModel to use the mocked getUserProfile
    viewModel = UserProfileViewModel(repository)
    testDispatcher.scheduler.advanceUntilIdle()

    // Confirm that userProfile is null
    Assert.assertNull(viewModel.userProfile.first())

    // Act
    viewModel.updateMetricMean()
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    verify(repository, never()).updateUserProfile(any(), any(), any())
  }

  @Test
  fun `updateMetricMean should set means to zero when recentData is empty`() = runTest {
    // Arrange
    doAnswer { invocation ->
          val onSuccess = invocation.getArgument<() -> Unit>(1)
          onSuccess()
        }
        .`when`(repository)
        .updateUserProfile(any(), any(), any())

    // Ensure recentData is empty
    val recentData = viewModel.recentData.first()
    Assert.assertTrue(recentData.isEmpty())

    // Mock getMetricMean to return 0.0 when list is empty
    `when`(repository.getMetricMean(any())).thenReturn(0.0)

    // Act
    viewModel.updateMetricMean()
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val updatedUserProfile = viewModel.userProfile.first()
    updatedUserProfile?.statistics?.talkTimeSecMean?.let { Assert.assertEquals(0.0, it, 0.001) }
    updatedUserProfile?.statistics?.talkTimePercMean?.let { Assert.assertEquals(0.0, it, 0.001) }

    verify(repository).updateUserProfile(any(), any(), any())
  }
}
