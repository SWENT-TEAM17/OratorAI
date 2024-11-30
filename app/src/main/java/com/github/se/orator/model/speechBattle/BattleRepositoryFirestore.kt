package com.github.se.orator.model.speechBattle

import android.util.Log
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.network.Message
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.UUID

class BattleRepositoryFirestore {

  private val db = FirebaseFirestore.getInstance()

  /** Generates a unique battle ID. */
  fun generateUniqueBattleId(): String {
    return UUID.randomUUID().toString()
  }

  /**
   * Stores a battle request in the Firestore database.
   *
   * @param speechBattle The battle to store.
   * @param callback A callback function to indicate success or failure.
   */
  fun storeBattleRequest(speechBattle: SpeechBattle, callback: (Boolean) -> Unit) {
    // Serialize the InterviewContext
    val interviewContextMap = interviewContextToMap(speechBattle.context)

    // Create a map to represent the battle data
    val battleMap =
        hashMapOf<String, Any>(
            "battleId" to speechBattle.battleId,
            "challenger" to speechBattle.challenger,
            "opponent" to speechBattle.opponent,
            "status" to speechBattle.status.name,
            "interviewContext" to interviewContextMap,
            "winner" to speechBattle.winner)

    // Store the battle in the "battles" collection
    db.collection("battles")
        .document(speechBattle.battleId)
        .set(battleMap)
        .addOnSuccessListener { callback(true) }
        .addOnFailureListener { e ->
          // Handle the error
          Log.e("BattleRepository", "Error storing battle request", e)
          callback(false)
        }
  }

  /**
   * Listens for pending battles for a specific user.
   *
   * @param userUid The UID of the user.
   * @param callback A callback function to handle the list of pending battles.
   */
  fun listenForPendingBattles(
      userUid: String,
      callback: (List<SpeechBattle>) -> Unit
  ): ListenerRegistration {
    return db.collection("battles")
        .whereEqualTo("opponent", userUid)
        .whereEqualTo("status", BattleStatus.PENDING.name)
        .addSnapshotListener { snapshots, error ->
          if (error != null) {
            Log.e("BattleRepository", "Error listening for pending battles", error)
            return@addSnapshotListener
          }

          val battles =
              snapshots?.documents?.mapNotNull { doc -> documentToSpeechBattle(doc) } ?: emptyList()

          callback(battles)
        }
  }

  /**
   * Updates the status of a battle.
   *
   * @param battleId The ID of the battle.
   * @param status The new status.
   * @param callback A callback function to indicate success or failure.
   */
  fun updateBattleStatus(battleId: String, status: BattleStatus, callback: (Boolean) -> Unit) {
    val battleRef = db.collection("battles").document(battleId)

    battleRef
        .update("status", status.name)
        .addOnSuccessListener { callback(true) }
        .addOnFailureListener { e ->
          Log.e("BattleRepository", "Error updating battle status", e)
          callback(false)
        }
  }

  /**
   * Retrieves a SpeechBattle by its ID.
   *
   * @param battleId The ID of the battle.
   * @param callback A callback function to handle the retrieved SpeechBattle.
   */
  fun getBattleById(battleId: String, callback: (SpeechBattle?) -> Unit) {
    val battleRef = db.collection("battles").document(battleId)

    battleRef
        .get()
        .addOnSuccessListener { document ->
          if (document != null && document.exists()) {
            val speechBattle = documentToSpeechBattle(document)
            callback(speechBattle)
          } else {
            callback(null)
          }
        }
        .addOnFailureListener { e ->
          Log.e("BattleRepository", "Error retrieving battle", e)
          callback(null)
        }
  }

  /** Converts a Firestore DocumentSnapshot to a SpeechBattle object. */
  private fun documentToSpeechBattle(document: DocumentSnapshot): SpeechBattle? {
    val data = document.data ?: return null
    val battleId = data["battleId"] as? String ?: return null
    val challenger = data["challenger"] as? String ?: return null
    val opponent = data["opponent"] as? String ?: return null
    val statusString = data["status"] as? String ?: return null
    val status = BattleStatus.valueOf(statusString)
    val winner = data["winner"] as? String ?: ""
    val interviewContextMap = data["interviewContext"] as? Map<String, Any> ?: return null
    val interviewContext = mapToInterviewContext(interviewContextMap) ?: return null

    return SpeechBattle(
        battleId = battleId,
        challenger = challenger,
        opponent = opponent,
        status = status,
        context = interviewContext,
        winner = winner)
  }

  /** Serializes an InterviewContext to a Map. */
  private fun interviewContextToMap(interviewContext: InterviewContext): Map<String, Any> {
    return mapOf(
        "interviewType" to interviewContext.interviewType,
        "role" to interviewContext.role,
        "company" to interviewContext.company,
        "focusAreas" to interviewContext.focusAreas)
  }

  /** Deserializes a Map to an InterviewContext. */
  private fun mapToInterviewContext(map: Map<String, Any>): InterviewContext? {
    val interviewType = map["interviewType"] as? String ?: return null
    val role = map["role"] as? String ?: return null
    val company = map["company"] as? String ?: return null
    val focusAreas = map["focusAreas"] as? List<String> ?: emptyList()

    return InterviewContext(
        interviewType = interviewType, role = role, company = company, focusAreas = focusAreas)
  }

  fun getPendingBattlesForUser(
      userUid: String,
      callback: (List<SpeechBattle>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection("battles")
        .whereEqualTo("opponent", userUid)
        .whereEqualTo("status", BattleStatus.PENDING.name)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val battles =
              querySnapshot.documents.mapNotNull { document -> documentToSpeechBattle(document) }
          callback(battles)
        }
        .addOnFailureListener { exception ->
          Log.e("BattleRepository", "Error fetching pending battles", exception)
          onFailure(exception)
        }
  }

  fun listenToBattleUpdates(
      battleId: String,
      callback: (SpeechBattle?) -> Unit
  ): ListenerRegistration {
    return db.collection("battles").document(battleId).addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.e("BattleRepository", "Error listening to battle updates", error)
        callback(null)
        return@addSnapshotListener
      }

      if (snapshot != null && snapshot.exists()) {
        val battle = documentToSpeechBattle(snapshot)
        callback(battle)
      } else {
        callback(null)
      }
    }
  }

  /**
   * Updates the user's battle data with the given messages and marks the user as completed in the
   * battle.
   *
   * @param battleId The ID of the battle.
   * @param userId The ID of the user (either challenger or opponent).
   * @param messages The list of messages exchanged by the user during the battle.
   * @param callback A callback to indicate success or failure.
   */
  fun updateUserBattleData(
      battleId: String,
      userId: String,
      messages: List<Message>,
      callback: (Boolean) -> Unit
  ) {
    val battleRef = db.collection("battles").document(battleId)

    // Fetch the battle document to update the appropriate fields.
    battleRef
        .get()
        .addOnSuccessListener { document ->
          if (document.exists()) {
            val battle = documentToSpeechBattle(document)
            val updates = mutableMapOf<String, Any>()

            // Determine if the user is the challenger or opponent and update accordingly.
            if (userId == battle?.challenger) {
              updates["challengerData"] = messages.map { it.toMap() }
              updates["challengerCompleted"] = true
            } else if (userId == battle?.opponent) {
              updates["opponentData"] = messages.map { it.toMap() }
              updates["opponentCompleted"] = true
            } else {
              Log.e("BattleRepository", "User ID does not match challenger or opponent.")
              callback(false)
              return@addOnSuccessListener
            }

            // Perform the update in Firestore.
            battleRef
                .update(updates)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { e ->
                  Log.e("BattleRepository", "Error updating battle data", e)
                  callback(false)
                }
          } else {
            Log.e("BattleRepository", "Battle document not found.")
            callback(false)
          }
        }
        .addOnFailureListener { e ->
          Log.e("BattleRepository", "Error fetching battle document.", e)
          callback(false)
        }
  }

  /**
   * Converts a Message object into a Map for Firestore storage.
   *
   * @return A Map representation of the Message object.
   */
  private fun Message.toMap(): Map<String, String> {
    return mapOf("role" to role, "content" to content)
  }

  fun updateBattleResult(battleId: String, winnerUid: String, evaluationText: String) {
    val battleRef = db.collection("battles").document(battleId)
    val updates =
        mapOf(
            "status" to BattleStatus.COMPLETED.name,
            "winner" to winnerUid,
            "evaluation" to evaluationText)
    battleRef
        .update(updates)
        .addOnSuccessListener { Log.d("BattleRepository", "Battle result updated") }
        .addOnFailureListener { e -> Log.e("BattleRepository", "Error updating battle result", e) }
  }
}
