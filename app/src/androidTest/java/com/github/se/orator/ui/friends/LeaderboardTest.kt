package com.github.se.orator.ui.friends

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.orator.model.profile.SessionType
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class LeaderboardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository

  private lateinit var userProfileViewModel: UserProfileViewModel

  private val testUserProfile = UserProfile("testId", "testName", 99, statistics = UserStatistics())

  private val testUserProfile2 =
      UserProfile("testId2", "testName2", 98, statistics = UserStatistics())

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    `when`(mockUserProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<((UserProfile?) -> Unit)>(0)(testUserProfile)
    }
    `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
      it.getArgument<((List<UserProfile>) -> Unit)>(0)(listOf(testUserProfile, testUserProfile2))
    }

    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)

    composeTestRule.setContent { LeaderboardScreen(mockNavigationActions, userProfileViewModel) }
  }

  @Test
  fun assertPracticeModeSelectionWorks() {

    composeTestRule.onNodeWithTag("practiceModeSelector").assertExists()

    for (i in 1..3) {
      composeTestRule.onNodeWithTag("practiceModeSelector").performClick()

      composeTestRule.onNodeWithTag("practiceModeOption$i").assertExists()
      composeTestRule.onNodeWithTag("practiceModeOption$i").performClick()

      assert(
          currentPracticeMode.value ==
              when (i) {
                1 -> SessionType.INTERVIEW
                2 -> SessionType.SPEECH
                3 -> SessionType.NEGOTIATION
                else -> null
              })
    }
  }

  @Test
  fun assertMetricSelectionWorks() {

    composeTestRule.onNodeWithTag("rankMetricSelector").assertExists()

    for (i in 1..3) {
      composeTestRule.onNodeWithTag("rankMetricSelector").performClick()

      composeTestRule.onNodeWithTag("rankMetricOption$i").assertExists()
      composeTestRule.onNodeWithTag("rankMetricOption$i").performClick()

      assert(
          currentRankMetric.value ==
              when (i) {
                1 -> "Ratio"
                2 -> "Success"
                3 -> "Improvement"
                else -> null
              })
    }
  }
}
