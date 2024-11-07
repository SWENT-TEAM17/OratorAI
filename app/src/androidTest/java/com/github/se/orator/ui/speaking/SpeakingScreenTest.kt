package com.github.se.orator.ui.speaking

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class SpeakingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var userProfileRepository: UserProfileRepository
  private lateinit var userProfileViewModel: UserProfileViewModel

  private lateinit var speakingRepository: SpeakingRepository

  private val analysisStateFlow = MutableStateFlow(SpeakingRepository.AnalysisState.PROCESSING)

  private lateinit var speakingViewModel: SpeakingViewModel

  private lateinit var apiLinkViewModel: ApiLinkViewModel

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    apiLinkViewModel = ApiLinkViewModel()

    speakingRepository = mock(SpeakingRepository::class.java)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.SPEAKING)
    `when`(speakingRepository.getAnalysisState()).thenReturn(analysisStateFlow)

    speakingViewModel = SpeakingViewModel(speakingRepository, apiLinkViewModel)
    composeTestRule.setContent {
      SpeakingScreen(navigationActions = navigationActions, speakingViewModel)
    }
  }

  @Test
  fun testEverythingIsPresent() {
    // composeTestRule.onNodeWithTag("back_button").isDisplayed()
    Thread.sleep(5000)
    analysisStateFlow.value = SpeakingRepository.AnalysisState.FINISHED
    Thread.sleep(5000)
    analysisStateFlow.value = SpeakingRepository.AnalysisState.IDLE
    Thread.sleep(5000)
    analysisStateFlow.value = SpeakingRepository.AnalysisState.PROCESSING
  }
}
