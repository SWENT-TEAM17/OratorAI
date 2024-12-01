package com.github.se.orator.model.profile

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
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
  @Mock private lateinit var mockTransaction: Transaction

  private val testUserProfile =
      UserProfile(
          uid = "testUid",
          name = "Test User",
          age = 25,
          statistics = UserStatistics(),
          friends = listOf("friend1", "friend2"),
          bio = "Test bio")

  val currentUid = "currentUserUid"
  val friendUid = "friendUserUid"

  // Mock references
  val currentUserRef = mock(DocumentReference::class.java)
  val friendUserRef = mock(DocumentReference::class.java)

  // Mock user snapshots
  val currentUserSnapshot = mock(DocumentSnapshot::class.java)
  val friendUserSnapshot = mock(DocumentSnapshot::class.java)

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

    // Set up the collection and document references
    `when`(mockCollectionReference.document(currentUid)).thenReturn(currentUserRef)
    `when`(mockCollectionReference.document(friendUid)).thenReturn(friendUserRef)
  }

  /**
   * This test verifies that when fetching a user profile, the Firestore `get()` is called on the
   * document reference and not the collection reference (to avoid fetching all documents).
   */
  @Test
  fun getUserProfileCallsDocuments() {
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))

    // Call the method under test
    repository.getUserProfile(
        testUserProfile.uid,
        onSuccess = {},
        onFailure = { fail("Failure callback should not be called") })

    verify(mockDocumentReference).get()
  }

  /**
   * This test verifies that when adding a user profile, the Firestore `set()` is called on the
   * document reference.
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

    // Prepare sessionsGivenMap and successfulSessionsMap
    val sessionsGivenMap = mapOf("SPEECH" to 10L, "INTERVIEW" to 5L, "NEGOTIATION" to 3L)

    val successfulSessionsMap = mapOf("SPEECH" to 7L, "INTERVIEW" to 2L, "NEGOTIATION" to 1L)

    // Prepare the statisticsMap
    val statisticsMap =
        mapOf(
            "sessionsGiven" to sessionsGivenMap,
            "successfulSessions" to successfulSessionsMap,
            "improvement" to 4.5f,
            "previousRuns" to
                listOf(
                    mapOf(
                        "title" to "Speech 1",
                        "duration" to 5L,
                        "date" to Timestamp.now(), // Use a valid Timestamp
                        "accuracy" to 85.0f,
                        "wordsPerMinute" to 120L),
                    mapOf(
                        "title" to "Speech 2",
                        "duration" to 7L,
                        "date" to Timestamp.now(), // Use a valid Timestamp
                        "accuracy" to 90.0f,
                        "wordsPerMinute" to 110L)))

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

          // Check sessionsGiven
          assert(statistics?.sessionsGiven?.get("SPEECH") == 10)
          assert(statistics?.sessionsGiven?.get("INTERVIEW") == 5)
          assert(statistics?.sessionsGiven?.get("NEGOTIATION") == 3)

          // Check successfulSessions
          assert(statistics?.successfulSessions?.get("SPEECH") == 7)
          assert(statistics?.successfulSessions?.get("INTERVIEW") == 2)
          assert(statistics?.successfulSessions?.get("NEGOTIATION") == 1)

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

    repository.updateUserProfilePicture(
        testUid,
        expectedDownloadUrl,
        onSuccess = { updateSuccess = true },
        onFailure = { fail("Failure callback should not be called") })

    // Ensure the Firestore update is processed
    shadowOf(Looper.getMainLooper()).idle()

    // Assertions
    assert(updateSuccess == true)

    // Verify that Firestore was called to update the profile picture
    verify(mockFirestore.collection(anyString()).document(testUid))
        .update(UserProfileRepositoryFirestore.FIELD_PROFILE_PIC, expectedDownloadUrl)
  }

  // New tests for updateLoginStreak

  /** Test that updateLoginStreak calls runTransaction and calls onSuccess when successful. */
  @Test
  fun updateLoginStreak_callsRunTransaction_andCallsOnSuccess() {
    val uid = "testUid"

    // Mock the runTransaction method to return a successful Task
    `when`(mockFirestore.runTransaction<Void>(any())).thenReturn(Tasks.forResult(null))

    var successCalled = false

    repository.updateLoginStreak(
        uid,
        onSuccess = { successCalled = true },
        onFailure = { fail("Failure callback should not be called") })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that runTransaction was called
    verify(mockFirestore).runTransaction<Void>(any())

    // Assert that onSuccess was called
    assert(successCalled)
  }

  /** Test that updateLoginStreak calls onFailure when transaction fails. */
  @Test
  fun updateLoginStreak_whenTransactionFails_callsOnFailure() {
    val uid = "testUid"

    // Mock the runTransaction method to return a failed Task
    val exception = Exception("Transaction failed")
    `when`(mockFirestore.runTransaction<Void>(any())).thenReturn(Tasks.forException(exception))

    var failureCalled = false

    repository.updateLoginStreak(
        uid,
        onSuccess = { fail("Success callback should not be called") },
        onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that runTransaction was called
    verify(mockFirestore).runTransaction<Void>(any())

    // Assert that onFailure was called
    assert(failureCalled)
  }

  /** Test that sendFriendRequest successfully sends a friend request. */
  @Test
  fun sendFriendRequest_whenSuccessful_callsOnSuccess() {

    // Set up the current sent requests and friend received requests
    `when`(currentUserSnapshot.get("sentReq")).thenReturn(emptyList<String>())
    `when`(friendUserSnapshot.get("recReq")).thenReturn(emptyList<String>())

    // Inside the transaction, simulate getting the snapshots and updating fields
    `when`(mockTransaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
    `when`(mockTransaction.get(friendUserRef)).thenReturn(friendUserSnapshot)

    // Mock the transaction
    `when`(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.getArgument<Transaction.Function<Void>>(0)
      transactionFunction.apply(mockTransaction)
      Tasks.forResult(null)
    }

    var successCalled = false
    var failureCalled = false

    repository.sendFriendRequest(
        currentUid,
        friendUid,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that transaction was run
    verify(mockFirestore).runTransaction<Void>(any())

    // Verify that transaction updated the correct fields
    verify(mockTransaction).update(currentUserRef, "sentReq", listOf(friendUid))
    verify(mockTransaction).update(friendUserRef, "recReq", listOf(currentUid))
  }

  /** Test that acceptFriendRequest successfully accepts a friend request. */
  @Test
  fun acceptFriendRequest_whenSuccessful_callsOnSuccess() {

    // Set up the collection and document references
    `when`(mockCollectionReference.document(currentUid)).thenReturn(currentUserRef)
    `when`(mockCollectionReference.document(friendUid)).thenReturn(friendUserRef)

    // Mock user snapshots
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
    val friendUserSnapshot = mock(DocumentSnapshot::class.java)

    // Set up the current received requests and friend sent requests
    `when`(currentUserSnapshot.get("recReq")).thenReturn(listOf(friendUid))
    `when`(currentUserSnapshot.get("friends")).thenReturn(emptyList<String>())
    `when`(friendUserSnapshot.get("sentReq")).thenReturn(listOf(currentUid))
    `when`(friendUserSnapshot.get("friends")).thenReturn(emptyList<String>())

    // Inside the transaction, simulate getting the snapshots and updating fields
    `when`(mockTransaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
    `when`(mockTransaction.get(friendUserRef)).thenReturn(friendUserSnapshot)

    // Mock the transaction
    `when`(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.getArgument<Transaction.Function<Void>>(0)
      transactionFunction.apply(mockTransaction)
      Tasks.forResult(null)
    }

    var successCalled = false
    var failureCalled = false

    repository.acceptFriendRequest(
        currentUid,
        friendUid,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that transaction was run
    verify(mockFirestore).runTransaction<Void>(any())

    // Verify that transaction updated the correct fields
    verify(mockTransaction).update(currentUserRef, "friends", listOf(friendUid))
    verify(mockTransaction).update(currentUserRef, "recReq", emptyList<String>())
    verify(mockTransaction).update(friendUserRef, "sentReq", emptyList<String>())
    verify(mockTransaction).update(friendUserRef, "friends", listOf(currentUid))
  }

  /** Test that declineFriendRequest successfully declines a friend request. */
  @Test
  fun declineFriendRequest_whenSuccessful_callsOnSuccess() {

    // Set up the collection and document references
    `when`(mockCollectionReference.document(currentUid)).thenReturn(currentUserRef)
    `when`(mockCollectionReference.document(friendUid)).thenReturn(friendUserRef)

    // Mock user snapshots
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
    val friendUserSnapshot = mock(DocumentSnapshot::class.java)

    // Set up the current received requests and friend sent requests
    `when`(currentUserSnapshot.get("recReq")).thenReturn(listOf(friendUid))
    `when`(friendUserSnapshot.get("sentReq")).thenReturn(listOf(currentUid))

    // Inside the transaction, simulate getting the snapshots and updating fields
    `when`(mockTransaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
    `when`(mockTransaction.get(friendUserRef)).thenReturn(friendUserSnapshot)

    // Mock the transaction
    `when`(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.getArgument<Transaction.Function<Void>>(0)
      transactionFunction.apply(mockTransaction)
      Tasks.forResult(null)
    }

    var successCalled = false
    var failureCalled = false

    repository.declineFriendRequest(
        currentUid,
        friendUid,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that transaction was run
    verify(mockFirestore).runTransaction<Void>(any())

    // Verify that transaction updated the correct fields
    verify(mockTransaction).update(currentUserRef, "recReq", emptyList<String>())
    verify(mockTransaction).update(friendUserRef, "sentReq", emptyList<String>())
  }

  /** Test that cancelFriendRequest successfully cancels a sent friend request. */
  @Test
  fun cancelFriendRequest_whenSuccessful_callsOnSuccess() {

    // Set up the collection and document references
    `when`(mockCollectionReference.document(currentUid)).thenReturn(currentUserRef)
    `when`(mockCollectionReference.document(friendUid)).thenReturn(friendUserRef)

    // Mock user snapshots
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
    val friendUserSnapshot = mock(DocumentSnapshot::class.java)

    // Set up the current sent requests and friend received requests
    `when`(currentUserSnapshot.get("sentReq")).thenReturn(listOf(friendUid))
    `when`(friendUserSnapshot.get("recReq")).thenReturn(listOf(currentUid))

    // Inside the transaction, simulate getting the snapshots and updating fields
    `when`(mockTransaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
    `when`(mockTransaction.get(friendUserRef)).thenReturn(friendUserSnapshot)

    // Mock the transaction
    `when`(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.getArgument<Transaction.Function<Void>>(0)
      transactionFunction.apply(mockTransaction)
      Tasks.forResult(null)
    }

    var successCalled = false
    var failureCalled = false

    repository.cancelFriendRequest(
        currentUid,
        friendUid,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that transaction was run
    verify(mockFirestore).runTransaction<Void>(any())

    // Verify that transaction updated the correct fields
    verify(mockTransaction).update(currentUserRef, "sentReq", emptyList<String>())
    verify(mockTransaction).update(friendUserRef, "recReq", emptyList<String>())
  }

  /** Test deleteFriend successfully deletes a friend. */
  @Test
  fun deleteFriend_whenSuccessful_callsOnSuccess() {

    // Set up the collection and document references
    whenever(mockCollectionReference.document(currentUid)).thenReturn(currentUserRef)
    whenever(mockCollectionReference.document(friendUid)).thenReturn(friendUserRef)

    // Mock user snapshots
    val currentUserSnapshot = mock(DocumentSnapshot::class.java)
    val friendUserSnapshot = mock(DocumentSnapshot::class.java)

    // Set up the current friends lists
    whenever(currentUserSnapshot.get("friends")).thenReturn(listOf(friendUid))
    whenever(friendUserSnapshot.get("friends")).thenReturn(listOf(currentUid))

    // Inside the transaction, simulate getting the snapshots and updating fields
    whenever(mockTransaction.get(currentUserRef)).thenReturn(currentUserSnapshot)
    whenever(mockTransaction.get(friendUserRef)).thenReturn(friendUserSnapshot)

    // Mock the transaction
    whenever(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.getArgument<Transaction.Function<Void>>(0)
      transactionFunction.apply(mockTransaction)
      Tasks.forResult(null)
    }

    var successCalled = false
    var failureCalled = false

    repository.deleteFriend(
        currentUid,
        friendUid,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that transaction was run
    verify(mockFirestore).runTransaction<Void>(any())

    // Verify that transaction updated the correct fields
    verify(mockTransaction).update(currentUserRef, "friends", emptyList<String>())
    verify(mockTransaction).update(friendUserRef, "friends", emptyList<String>())
  }

  /** Test updateLoginStreak updates the streak correctly. */
  @Test
  fun updateLoginStreak_whenSuccessful_callsOnSuccess() {
    val uid = "testUid"

    // Mock user document reference
    val userRef = mock(DocumentReference::class.java)
    whenever(mockCollectionReference.document(uid)).thenReturn(userRef)

    // Mock user snapshot
    val userSnapshot = mock(DocumentSnapshot::class.java)

    // Set up the last login date and current streak
    whenever(userSnapshot.getString("lastLoginDate")).thenReturn("2021-09-01")
    whenever(userSnapshot.getLong("currentStreak")).thenReturn(5L)

    // Inside the transaction, simulate getting the snapshot and updating fields
    whenever(mockTransaction.get(userRef)).thenReturn(userSnapshot)

    // Mock the transaction
    whenever(mockFirestore.runTransaction<Void>(any())).thenAnswer { invocation ->
      val transactionFunction = invocation.getArgument<Transaction.Function<Void>>(0)
      transactionFunction.apply(mockTransaction)
      Tasks.forResult(null)
    }

    var successCalled = false
    var failureCalled = false

    repository.updateLoginStreak(
        uid, onSuccess = { successCalled = true }, onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that transaction was run
    verify(mockFirestore).runTransaction<Void>(any())

    // Verify that transaction updated the correct fields
    verify(mockTransaction).update(eq(userRef), anyMap<String, Any>())
  }

  /** Test getRecReqProfiles returns the correct profiles. */
  @Test
  fun getRecReqProfiles_whenSuccessful_returnsProfiles() {
    val recReqUids = listOf("user1", "user2")
    val mockUserProfile1 =
        UserProfile(uid = "user1", name = "User One", age = 20, statistics = UserStatistics())
    val mockUserProfile2 =
        UserProfile(uid = "user2", name = "User Two", age = 22, statistics = UserStatistics())

    // Mock query
    whenever(mockCollectionReference.whereIn("uid", recReqUids)).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    whenever(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot))

    // Mock document snapshots to return user profiles
    whenever(mockDocumentSnapshot.toObject(UserProfile::class.java))
        .thenReturn(mockUserProfile1)
        .thenReturn(mockUserProfile2)

    var profiles: List<UserProfile>? = null

    repository.getRecReqProfiles(
        recReqUids,
        onSuccess = { profiles = it },
        onFailure = { fail("Failure callback should not be called") })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify the results
    assertNotNull(profiles)
  }

  /** Test getSentReqProfiles returns the correct profiles. */
  @Test
  fun getSentReqProfiles_whenSuccessful_returnsProfiles() {
    val sentReqUids = listOf("user3", "user4")
    val mockUserProfile3 =
        UserProfile(uid = "user3", name = "User Three", age = 23, statistics = UserStatistics())
    val mockUserProfile4 =
        UserProfile(uid = "user4", name = "User Four", age = 24, statistics = UserStatistics())

    // Mock query
    whenever(mockCollectionReference.whereIn("uid", sentReqUids)).thenReturn(mockQuery)
    whenever(mockQuery.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    whenever(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot))

    // Mock document snapshots to return user profiles
    whenever(mockDocumentSnapshot.toObject(UserProfile::class.java))
        .thenReturn(mockUserProfile3)
        .thenReturn(mockUserProfile4)

    var profiles: List<UserProfile>? = null

    repository.getSentReqProfiles(
        sentReqUids,
        onSuccess = { profiles = it },
        onFailure = { fail("Failure callback should not be called") })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify the results
    assertNotNull(profiles)
  }

  /** Test getAllUserProfiles returns all profiles. */
  @Test
  fun getAllUserProfiles_whenSuccessful_returnsProfiles() {
    val mockUserProfile1 =
        UserProfile(uid = "user1", name = "User One", age = 20, statistics = UserStatistics())
    val mockUserProfile2 =
        UserProfile(uid = "user2", name = "User Two", age = 22, statistics = UserStatistics())

    // Mock query
    whenever(mockCollectionReference.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))
    whenever(mockQuerySnapshot.documents)
        .thenReturn(listOf(mockDocumentSnapshot, mockDocumentSnapshot))

    // Mock document snapshots to return user profiles
    whenever(mockDocumentSnapshot.toObject(UserProfile::class.java))
        .thenReturn(mockUserProfile1)
        .thenReturn(mockUserProfile2)

    var profiles: List<UserProfile>? = null

    repository.getAllUserProfiles(
        onSuccess = { profiles = it },
        onFailure = { fail("Failure callback should not be called") })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify the results
    assertNotNull(profiles)
  }

  /** Test deleteUserProfile deletes the profile successfully. */
  @Test
  fun deleteUserProfile_whenSuccessful_callsOnSuccess() {
    val uid = "testUid"

    // Mock delete
    whenever(mockDocumentReference.delete()).thenReturn(Tasks.forResult(null))

    var successCalled = false
    var failureCalled = false

    repository.deleteUserProfile(
        uid, onSuccess = { successCalled = true }, onFailure = { failureCalled = true })

    // Execute pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that success was called
    assertTrue(successCalled)
    assertFalse(failureCalled)

    // Verify that delete was called on the document reference
    verify(mockDocumentReference).delete()
  }

  // Add any additional tests if necessary

}
