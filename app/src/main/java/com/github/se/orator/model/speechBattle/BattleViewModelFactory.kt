package com.github.se.orator.model.speechBattle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.NavigationActions

class BattleViewModelFactory(
    private val userProfileViewModel: UserProfileViewModel,
    private val navigationActions: NavigationActions,
    private val apiLinkViewModel: ApiLinkViewModel
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(BattleViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return BattleViewModel(userProfileViewModel, navigationActions, apiLinkViewModel) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
