package com.github.se.orator.model.speechBattle

import com.github.se.orator.ui.network.Message

enum class BattleStatus {
  PENDING,
  IN_PROGRESS,
  CANCELLED,
  FINISHED
}

data class SpeechBattle(
    val battleId: String,
    val challenger: String, // User ID of the user that send the challenge
    val opponent: String,
    val status: BattleStatus,
    val initialMessages: List<Message>, // First response from GPT that contains the info
    val winner: String // User ID of the winner
)
