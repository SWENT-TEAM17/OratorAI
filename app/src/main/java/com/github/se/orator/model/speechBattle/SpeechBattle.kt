package com.github.se.orator.model.speechBattle

import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.network.Message

enum class BattleStatus {
  PENDING,
  IN_PROGRESS,
  CANCELLED,
  COMPLETED
}

data class SpeechBattle(
    val battleId: String,
    val challenger: String, // User ID of the user that sent the challenge
    val opponent: String,
    val status: BattleStatus,
    val context: InterviewContext,
    val winner: String = "", // User ID of the winner
    val challengerCompleted: Boolean = false,
    val opponentCompleted: Boolean = false,
    val challengerData: List<Message> = emptyList(),
    val opponentData: List<Message> = emptyList()
)
