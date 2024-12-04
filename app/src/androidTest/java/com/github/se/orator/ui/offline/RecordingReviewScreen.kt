package com.github.se.orator.ui.offline

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class RecordingReviewScreen {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var speakingViewModel: SpeakingViewModel
    private lateinit var speakingRepository: SpeakingRepository
    private lateinit var apiLinkViewModel: ApiLinkViewModel

    @Before
    fun setUp() {
        navigationActions = mock(NavigationActions::class.java)
        speakingRepository = mock(SpeakingRepository::class.java)
        apiLinkViewModel = ApiLinkViewModel()

        speakingViewModel = SpeakingViewModel(speakingRepository, apiLinkViewModel)

        composeTestRule.setContent {
            RecordingReviewScreen(navigationActions, speakingViewModel)
        }
    }

    @Test
    fun testEverythingIsDisplayed() {
        composeTestRule.onNodeWithTag("RecordingReviewScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("BackButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Back").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hear_recording_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stop_recording_button").assertIsDisplayed()


    }

}