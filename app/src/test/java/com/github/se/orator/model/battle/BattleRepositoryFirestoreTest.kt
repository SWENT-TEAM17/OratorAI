package com.github.se.orator.model.battle

import android.os.Looper
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speechBattle.BattleRepositoryFirestore
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class BattleRepositoryFirestoreTest {

  @Mock private lateinit var mockFirestore: FirebaseFirestore

  @Mock private lateinit var mockCollectionReference: CollectionReference

  @Mock private lateinit var mockDocumentReference: DocumentReference

  @Mock private lateinit var mockDocumentSnapshot: DocumentSnapshot

  @Mock private lateinit var mockListenerRegistration: ListenerRegistration

  private lateinit var repository: BattleRepositoryFirestore

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize the repository with the mocked Firestore instance
    repository = BattleRepositoryFirestore(mockFirestore)

    // Mock Firestore collection and document references
    `when`(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference)
    `when`(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference)
  }

  /** Test that generateUniqueBattleId returns a non-empty string */
  @Test
  fun generateUniqueBattleId_returnsNonEmptyString() {
    val battleId = repository.generateUniqueBattleId()
    assert(battleId.isNotEmpty())
  }

  /** Test storing a battle request successfully */
  @Test
  fun storeBattleRequest_success_callsCallbackWithTrue() {
    val speechBattle = createTestSpeechBattle()

    // Mock Firestore set operation to succeed
    `when`(mockDocumentReference.set(any())).thenReturn(Tasks.forResult(null))

    var callbackCalled = false

    repository.storeBattleRequest(speechBattle) { success ->
      assert(success)
      callbackCalled = true
    }

    // Process any pending tasks
    shadowOf(Looper.getMainLooper()).idle()

    // Verify that set was called on the document reference
    verify(mockDocumentReference).set(any())
    assert(callbackCalled)
  }

  /** Test retrieving a battle by ID successfully */
  @Test
  fun getBattleById_success_returnsSpeechBattle() {
    val battleId = "battle1"

    // Mock Firestore get operation to return a valid document
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(true)
    `when`(mockDocumentSnapshot.data).thenReturn(createTestBattleDataMap())

    var callbackBattle: SpeechBattle? = null

    repository.getBattleById(battleId) { battle -> callbackBattle = battle }

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).get()
    assert(callbackBattle != null)
    assert(callbackBattle?.battleId == "battle1")
    assert(callbackBattle?.challenger == "user1")
    assert(callbackBattle?.opponent == "user2")
    assert(callbackBattle?.status == BattleStatus.PENDING)
  }

  /** Test retrieving a battle by ID when document does not exist */
  @Test
  fun getBattleById_documentDoesNotExist_returnsNull() {
    val battleId = "battle1"

    // Mock Firestore get operation to return a non-existent document
    `when`(mockDocumentReference.get()).thenReturn(Tasks.forResult(mockDocumentSnapshot))
    `when`(mockDocumentSnapshot.exists()).thenReturn(false)

    var callbackBattle: SpeechBattle? = null

    repository.getBattleById(battleId) { battle -> callbackBattle = battle }

    shadowOf(Looper.getMainLooper()).idle()

    verify(mockDocumentReference).get()
    assert(callbackBattle == null)
  }

  /** Test listenForPendingBattles method */
  @Test
  fun listenForPendingBattles_receivesBattles_callsCallbackWithList() {
    val userUid = "user2"

    // Mock QuerySnapshot and DocumentSnapshot
    val mockQuerySnapshot = mock(QuerySnapshot::class.java)
    val mockDocumentSnapshot1 = mock(DocumentSnapshot::class.java)
    `when`(mockDocumentSnapshot1.data).thenReturn(createTestBattleDataMap())

    `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocumentSnapshot1))

    // Mock Firestore query and snapshot listener
    val query = mock(Query::class.java)
    `when`(mockCollectionReference.whereEqualTo("opponent", userUid)).thenReturn(query)
    `when`(query.whereEqualTo("status", BattleStatus.PENDING.name)).thenReturn(query)
    `when`(query.addSnapshotListener(any<EventListener<QuerySnapshot>>())).thenAnswer { invocation
      ->
      val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
      listener.onEvent(mockQuerySnapshot, null)
      mockListenerRegistration
    }

    var callbackBattles: List<SpeechBattle>? = null

    repository.listenForPendingBattles(userUid) { battles -> callbackBattles = battles }

    shadowOf(Looper.getMainLooper()).idle()

    verify(query).addSnapshotListener(any<EventListener<QuerySnapshot>>())
    assert(callbackBattles != null)
    assert(callbackBattles!!.size == 1)
    val battle = callbackBattles!![0]
    assert(battle.battleId == "battle1")
    assert(battle.opponent == "user2")
  }

  // Helper methods to create test data

  private fun createTestSpeechBattle(): SpeechBattle {
    return SpeechBattle(
        battleId = "battle1",
        challenger = "user1",
        opponent = "user2",
        status = BattleStatus.PENDING,
        context =
        InterviewContext(
          targetPosition = "",
          companyName = "",
          interviewType = "",
          experienceLevel = "",
          jobDescription = "",
          focusArea = ""),
        winner = "")
  }

  private fun createTestBattleDataMap(): Map<String, Any> {
    return mapOf(
        "battleId" to "battle1",
        "challenger" to "user1",
        "opponent" to "user2",
        "status" to "PENDING",
        "winner" to "",
        "interviewContext" to
            mapOf(
                "interviewType" to "Job Interview",
                "role" to "Developer",
                "company" to "TechCorp",
                "focusAreas" to listOf("Algorithms", "System Design")),
        "challengerCompleted" to true,
        "opponentCompleted" to false)
  }
}
