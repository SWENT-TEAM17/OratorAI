package com.github.se.orator.model.speechBattle

import com.github.se.orator.ui.network.Message
import com.google.firebase.firestore.ListenerRegistration

/** Interface for managing battles in the data store. */
interface BattleRepository {
  fun generateUniqueBattleId(): String
  /**
   * Stores a battle request in the Firestore database.
   *
   * @param speechBattle The battle to store.
   * @param callback A callback function to indicate success or failure.
   */
  fun storeBattleRequest(speechBattle: SpeechBattle, callback: (Boolean) -> Unit)
  /**
   * Listens for pending battles for a specific user.
   *
   * @param userUid The UID of the user.
   * @param callback A callback function to handle the list of pending battles.
   */
  fun listenForPendingBattles(
      userUid: String,
      callback: (List<SpeechBattle>) -> Unit
  ): ListenerRegistration
  /**
   * Updates the status of a battle.
   *
   * @param battleId The ID of the battle.
   * @param status The new status.
   * @param callback A callback function to indicate success or failure.
   */
  fun updateBattleStatus(battleId: String, status: BattleStatus, callback: (Boolean) -> Unit)
  /**
   * Retrieves a SpeechBattle by its ID.
   *
   * @param battleId The ID of the battle.
   * @param callback A callback function to handle the retrieved SpeechBattle.
   */
  fun getBattleById(battleId: String, callback: (SpeechBattle?) -> Unit)
  /**
   * Fetches pending battles for a specific user.
   *
   * @param userUid The UID of the user.
   * @param callback A callback function to handle the list of pending battles.
   * @param onFailure A callback function to handle any errors that occur.
   */
  fun getPendingBattlesForUser(
      userUid: String,
      callback: (List<SpeechBattle>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Listens for updates to a specific battle.
   *
   * @param battleId The ID of the battle to listen for updates.
   * @param callback A callback function to handle the updated SpeechBattle.
   * @return A ListenerRegistration object to manage the listener.
   */
  fun listenToBattleUpdates(
      battleId: String,
      callback: (SpeechBattle?) -> Unit
  ): ListenerRegistration

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
  )
  /**
   * Updates the winner of a battle.
   *
   * @param battleId The ID of the battle.
   * @param winnerUid The UID of the winner.
   * @param evaluationText The evaluation text.
   */
  fun updateBattleResult(
      battleId: String,
      winnerUid: String,
      evaluationText: String,
      callback: (Boolean) -> Unit
  )
  /**
   * Completes a battle by updating the status and storing the evaluation result.
   *
   * @param battleId The ID of the battle.
   * @param evaluationResult The evaluation result.
   * @param callback A callback function to execute.
   */
  fun completeBattle(
      battleId: String,
      evaluationResult: EvaluationResult,
      callback: (Boolean) -> Unit
  )
}
