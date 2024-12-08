package com.github.se.orator.model.speechBattle

import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.network.Message
import com.google.firebase.firestore.ListenerRegistration

/** Interface for managing battles in the data store. */
interface BattleRepository {
    fun generateUniqueBattleId(): String
    fun storeBattleRequest(speechBattle: SpeechBattle, callback: (Boolean) -> Unit)
    fun listenForPendingBattles(userUid: String, callback: (List<SpeechBattle>) -> Unit): ListenerRegistration
    fun updateBattleStatus(battleId: String, status: BattleStatus, callback: (Boolean) -> Unit)
    fun getBattleById(battleId: String, callback: (SpeechBattle?) -> Unit)
    fun getPendingBattlesForUser(userUid: String, callback: (List<SpeechBattle>) -> Unit, onFailure: (Exception) -> Unit)
    fun listenToBattleUpdates(battleId: String, callback: (SpeechBattle?) -> Unit): ListenerRegistration
    fun updateUserBattleData(battleId: String, userId: String, messages: List<Message>, callback: (Boolean) -> Unit)
    fun updateBattleResult(battleId: String, winnerUid: String, evaluationText: String, callback: (Boolean) -> Unit)
}
