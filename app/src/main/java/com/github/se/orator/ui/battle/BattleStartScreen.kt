package com.github.se.orator.ui.battle

import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.ui.navigation.NavigationActions

/**
 * This composable takes care of the screen where the the user can send a battle request to another
 * user.
 *
 * @param navigationActions The navigation actions to navigate to other screens.
 * @param userProfileViewModel The view model to get the user profile.
 * @param battleViewModel The view model to send the battle request.
 */
fun BattleInitializationScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel,
    battleViewModel: BattleViewModel
) {}
