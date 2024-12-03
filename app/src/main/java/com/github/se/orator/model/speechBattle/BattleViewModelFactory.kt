package com.github.se.orator.model.speechBattle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions

class BattleViewModelFactory(
    private val userProfileViewModel: UserProfileViewModel,
    private val navigationActions: NavigationActions,
    private val apiLinkViewModel: ApiLinkViewModel,
    private val chatViewModel: ChatViewModel
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(BattleViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return BattleViewModel(
          userProfileViewModel, navigationActions, apiLinkViewModel, chatViewModel)
          as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
