package com.github.se.orator.ui.battle

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.overview.ChatButtonType
import com.github.se.orator.ui.overview.ChatScreen

/**
 * Composable function for the Battle Chat Screen.
 *
 * This screen displays the chat interface for a battle, where the user interacts with the
 * assistant. It uses the reusable ChatScreen composable and adds battle-specific logic for session
 * management.
 *
 * @param battleId The unique ID of the battle.
 * @param userId The ID of the current user.
 * @param navigationActions Actions to handle navigation events.
 * @param battleViewModel ViewModel for managing battle-related data.
 * @param chatViewModel ViewModel for managing chat messages and interactions.
 */
@Composable
fun BattleChatScreen(
    battleId: String,
    userId: String,
    navigationActions: NavigationActions,
    battleViewModel: BattleViewModel,
    chatViewModel: ChatViewModel,
    userProfileViewModel: UserProfileViewModel
) {

  // Get the opponent's UID from the battle data and initialize the battle-specific conversation
  battleViewModel.getOpponentUid(battleId, userId) { friendUid ->
    if (friendUid != null) {
      val friendName = userProfileViewModel.getName(friendUid)
      // Initialize the battle-specific conversation
      chatViewModel.initializeBattleConversation(battleId, friendName)
    } else {
      Log.e("BattleChatScreen", "Failed to retrieve friend UID for battleId: $battleId")
    }
  }

  // Collect chat messages and loading state from ChatViewModel
  val chatMessages by chatViewModel.chatMessages.collectAsState()

  // Use the generic ChatScreen composable for the UI
  ChatScreen(
      navigationActions = navigationActions,
      chatViewModel = chatViewModel,
      chatButtonType = ChatButtonType.FINISH_BATTLE_BUTTON,
      onChatButtonClick = {
        // Handle finishing the battle session
        battleViewModel.markUserBattleCompleted(battleId, userId, chatMessages)
      },
      showBackButton = false)
}
