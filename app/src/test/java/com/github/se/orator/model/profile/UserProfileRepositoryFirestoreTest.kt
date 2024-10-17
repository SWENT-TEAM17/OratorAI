package com.github.se.orator.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import junit.framework.TestCase.fail
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class UserProfileRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore
  @Mock private lateinit var mockDocumentReference: DocumentReference
  @Mock private lateinit var mockCollectionReference: CollectionReference
  @Mock private lateinit var mockQuery: Query
  @Mock private lateinit var mockQuerySnapshot: QuerySnapshot
  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot
  private lateinit var repository: UserProfileRepositoryFirestore
  @Mock private lateinit var mockStorage: FirebaseStorage

  private val testUserProfile =
      UserProfile(
          uid = "testUid",
          name = "Test User",
          age = 25,
          statistics = UserStatistics(),
          friends = listOf("friend1", "friend2"),
          bio = "Test bio")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    repository = UserProfileRepositoryFirestore(mockFirestore)

    // Mock Firestore collection and document references
    whenever(mockFirestore.collection(any())).thenReturn(mockCollectionReference)
    whenever(mockCollectionReference.document(any())).thenReturn(mockDocumentReference)
    `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)
  }

  /**
   * This test verifies that when fetching a user profile, the Firestore `get()` is called on the
   * document reference and not the collection reference (to avoid fetching all documents).
   */
  @Test
  fun getUserProfileCallsDocuments() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Ensure the QuerySnapshot returns a list of mock DocumentSnapshots
    `when`(mockQuerySnapshot.documents).thenReturn(listOf())

    // Call the method under test
    repository.getUserProfile(
        testUserProfile.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(org.mockito.kotlin.timeout(100)) { (mockQuerySnapshot).documents }
  }

  /**
   * This test verifies that when fetching a user profile, the Firestore `get()` is called on the
   * document reference and not the collection reference (to avoid fetching all documents).
   */
  @Test
  fun addUserProfile_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    repository.addUserProfile(testUserProfile, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  /**
   * This test verifies that when updating a user profile, the Firestore `set()` is called on the
   * document reference.
   */
  @Test
  fun updateUserProfile_shouldCallFirestoreCollection() {
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null)) // Simulate success

    repository.updateUserProfile(testUserProfile, onSuccess = {}, onFailure = {})

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).set(any())
  }

  /**
   * This test verifies that when fetching friends' profiles, the Firestore `whereIn()` is called on
   * the collection reference with the correct friend UIDs.
   */
  @Test
  fun getFriendsProfiles_whenFriendUidsEmpty_shouldReturnEmptyList() {
    `when`(mockCollectionReference.whereIn(any<String>(), any())).thenReturn(mockQuery)
    // Call the method under test with an empty list of friend UIDs
    repository.getFriendsProfiles(
        emptyList(),
        onSuccess = { friends ->
          // Assert that the list of friends returned is empty
          assert(friends.isEmpty())
        },
        onFailure = { fail("Failure callback should not be called") })

    // Verify Firestore query was not called
    verify(mockCollectionReference, never()).whereIn(anyString(), any())
  }

  @Test
  fun getFriendsProfiles_whenQuerySuccessful_shouldReturnListOfFriends() {
    `when`(mockCollectionReference.whereIn(any<String>(), any())).thenReturn(mockQuery)
    val friendUids = listOf("friend1", "friend2")
    val mockFriendProfile1 = mock(UserProfile::class.java)
    val mockFriendProfile2 = mock(UserProfile::class.java)

    // Mock query success and return a list of documents
    `when`(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    `when`(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot))

    // Mock document to user profile conversion
    `when`(mockDocumentSnapshot.toObject(UserProfile::class.java))
        .thenReturn(mockFriendProfile1)
        .thenReturn(mockFriendProfile2)

    repository.getFriendsProfiles(
        friendUids,
        onSuccess = { friends ->
          // Assert that the correct number of friends was returned
          assert(2 == friends.size)
        },
        onFailure = { fail("Failure callback should not be called") })

    verify(mockCollectionReference).whereIn("uid", friendUids)
  }

  /**
   * This test verifies that when fetching friends' profiles, the failure callback is called when
   * the Firestore query fails.
   */
  @Test
  fun getFriendsProfiles_whenQueryFails_shouldCallFailureCallback() {
    `when`(mockCollectionReference.whereIn(any<String>(), any())).thenReturn(mockQuery)
    val friendUids = listOf("friend1", "friend2")

    // Mock query failure
    val exception = Exception("Query failed")
    `when`(mockQuery.get()).thenReturn(Tasks.forException(exception))

    repository.getFriendsProfiles(
        friendUids,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { error ->
          // Assert that the failure callback is called with the correct exception
          assert(exception == error)
        })

    verify(mockCollectionReference).whereIn("uid", friendUids)
  }

  /** Indirectly tests the behavior of documentToUserProfile function. */
  @Test
  fun getUserProfile_whenDocumentSnapshotIsValid_shouldReturnUserProfile() {
    // Prepare a mock DocumentSnapshot with the expected fields
    val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
    `when`(mockDocumentSnapshot.id).thenReturn("testUid")
    `when`(mockDocumentSnapshot.getString("name")).thenReturn("Test User")
    `when`(mockDocumentSnapshot.getLong("age")).thenReturn(25L)
    `when`(mockDocumentSnapshot.get("friends")).thenReturn(listOf("friend1", "friend2"))
    `when`(mockDocumentSnapshot.getString("bio")).thenReturn("Test bio")
    val statisticsMap =
        mapOf(
            "speechesGiven" to 10,
            "improvement" to 4.5f,
            "previousRuns" to
                listOf(
                    mapOf(
                        "title" to "Speech 1",
                        "duration" to 5,
                        "date" to 0,
                        "accuracy" to 85.0f,
                        "wordsPerMinute" to 120),
                    mapOf(
                        "title" to "Speech 2",
                        "duration" to 7,
                        "date" to 0,
                        "accuracy" to 90.0f,
                        "wordsPerMinute" to 110)))
    `when`(mockDocumentSnapshot.get("statistics")).thenReturn(statisticsMap)

    // Simulate a successful Firestore query with Tasks.forResult()
    val mockTask = Tasks.forResult(mockDocumentSnapshot)
    `when`(mockFirestore.collection(anyString()).document(anyString()).get()).thenReturn(mockTask)

    repository.getUserProfile(
        "testUid",
        onSuccess = { userProfile ->
          // Assertions using the Kotlin assert function
          assert(userProfile != null)
          assert(userProfile?.uid == "testUid")
          assert(userProfile?.name == "Test User")
          assert(userProfile?.age == 25)
          assert(userProfile?.friends == listOf("friend1", "friend2"))
          assert(userProfile?.bio == "Test bio")

          // Assertions for UserStatistics
          val statistics = userProfile?.statistics
          assert(statistics != null)
          assert(statistics?.speechesGiven == 10)
          assert(statistics?.improvement == 4.5f)

          // Assertions for previous runs in UserStatistics
          assert(statistics?.previousRuns?.size == 2)
          val firstRun = statistics?.previousRuns?.get(0)
          assert(firstRun?.title == "Speech 1")
          assert(firstRun?.duration == 5)
          assert(firstRun?.accuracy == 85.0f)
          assert(firstRun?.wordsPerMinute == 120)

          val secondRun = statistics?.previousRuns?.get(1)
          assert(secondRun?.title == "Speech 2")
          assert(secondRun?.duration == 7)
          assert(secondRun?.accuracy == 90.0f)
          assert(secondRun?.wordsPerMinute == 110)
        },
        onFailure = { fail("Failure callback should not be called") })

    // Make sure any tasks on the main looper are executed
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that the Firestore document was fetched
    verify(mockFirestore.collection(anyString()).document(anyString())).get()
  }

  /**
   * This test verifies that when updating a user profile picture, the Firestore `update()` is
   * called on the document reference.
   */
  @Test
  fun updateUserProfilePicture_whenUpdateIsSuccessful_shouldCallFirestoreUpdate() {
    val testUid = "testUid"
    val expectedDownloadUrl = "https://example.com/downloaded_image.jpg"

    // Simulate successful Firestore update
    `when`(
            mockFirestore
                .collection(anyString())
                .document(testUid)
                .update(UserProfileRepositoryFirestore.FIELD_PROFILE_PIC, expectedDownloadUrl))
        .thenReturn(Tasks.forResult(null))

    var updateSuccess: Boolean? = null
    var updateFailure: Exception? = null

    repository.updateUserProfilePicture(
        testUid,
        expectedDownloadUrl,
        onSuccess = { updateSuccess = true },
        onFailure = { updateFailure = it })

    // Ensure the Firestore update is processed
    shadowOf(Looper.getMainLooper()).idle()

    // Assertions
    assert(updateSuccess == true)
    assert(updateFailure == null)

    // Verify that Firestore was called to update the profile picture
    verify(mockFirestore.collection(anyString()).document(testUid))
        .update(UserProfileRepositoryFirestore.FIELD_PROFILE_PIC, expectedDownloadUrl)
  }

  @Test
  fun `UserProfile data class should hold correct values`() {
    val speechStats =
        SpeechStats(
            title = "Inauguration Speech",
            duration = 15,
            date = com.google.firebase.Timestamp.now(),
            accuracy = 98.5f,
            wordsPerMinute = 120)

    val userStatistics =
        UserStatistics(speechesGiven = 10, improvement = 20.0f, previousRuns = listOf(speechStats))

    val userProfile =
        UserProfile(
            uid = "user123",
            name = "John Doe",
            age = 30,
            profilePic = "http://example.com/john.jpg",
            statistics = userStatistics,
            friends = listOf("friend1", "friend2"),
            bio = "An experienced speaker.")

    assertEquals("user123", userProfile.uid)
    assertEquals("John Doe", userProfile.name)
    assertEquals(30, userProfile.age)
    assertEquals("http://example.com/john.jpg", userProfile.profilePic)
    assertEquals(userStatistics, userProfile.statistics)
    assertEquals(listOf("friend1", "friend2"), userProfile.friends)
    assertEquals("An experienced speaker.", userProfile.bio)
  }
}
