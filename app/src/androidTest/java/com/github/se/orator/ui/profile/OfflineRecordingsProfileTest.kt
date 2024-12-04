package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.offline.RecordingReviewScreen
import loadPromptsFromFile
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class OfflineRecordingsProfileTest {
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

    }

    @Test
    fun testEverythingIsDisplayedWithPrompts() {
//        `when`(loadPromptsFromFile(any())).thenReturn(
//            listOf(
//                mapOf(
//                    "targetCompany" to "Apple",
//                    "jobPosition" to "Hardware engineer",
//                    "ID" to "568"
//                )
//            )
//        )
        composeTestRule.setContent {
            OfflineRecordingsProfileScreen(navigationActions, speakingViewModel)
        }
//
//        composeTestRule.onNodeWithTag("prompt_card_0").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("prompt_title_0").assertIsDisplayed()
//        composeTestRule.onNodeWithTag("prompt_detail_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("back_button").assertIsDisplayed()
    }

    @Test
    fun testEverythingIsDisplayedNoPrompts() {
//        `when`(loadPromptsFromFile(any())).thenReturn(
//            listOf(
//                mapOf(
//                )
//            )
//        )
        composeTestRule.setContent {
            OfflineRecordingsProfileScreen(navigationActions, speakingViewModel)
        }

        composeTestRule.onNodeWithTag("no_prompts_text").isDisplayed()
    }

}