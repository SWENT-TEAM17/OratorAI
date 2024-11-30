package com.github.se.orator.model.speechBattle

import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PracticeContext
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions

fun createBattleRequest(
    friendUid: String,
    interviewContext: InterviewContext,
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
    // Generate a unique battle ID
    //val battleId = generateUniqueBattleId()

    // Get the current user's ID (challenger)
    val challengerUid = userProfileViewModel.userProfile.value!!.uid

    // Create the SpeechBattle object
    val speechBattle = SpeechBattle(
        battleId = "1",
        challenger = challengerUid,
        opponent = friendUid,
        status = BattleStatus.PENDING,
        context = interviewContext
    )

    // Store the battle request in the database
    storeBattleRequest(speechBattle)

    // Navigate to the BattleRequestSentScreen
    navigationActions.navigateToBattleRequestSentScreen(friendUid)
}



