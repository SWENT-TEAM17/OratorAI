package com.github.se.orator.ui.profile

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.model.speaking.AnalysisData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class StatScreenTest {

  private lateinit var userProfileViewModel: UserProfileViewModel
  @Mock private lateinit var userProfileRepository: UserProfileRepository

  private val mockedRecentData = createMockedRecentData()

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    userProfileViewModel = UserProfileViewModel(userProfileRepository)

    `when`(userProfileRepository.getUserProfile(any(), any(), any())).then {
      it.getArgument<(UserProfile) -> Unit>(1)(testUserProfile)
    }
    userProfileViewModel = UserProfileViewModel(userProfileRepository)
    userProfileViewModel.getUserProfile(testUserProfile.uid)
  }

  private val talkTimeSecMean =
      (mockedRecentData.toList().map { data -> data.talkTimeSeconds }).average()
  private val paceMean = (mockedRecentData.toList().map { data -> data.pace.toDouble() }).average()

  private val testUserProfile =
      UserProfile(
          uid = "testUid",
          name = "Test User",
          age = 25,
          statistics =
              UserStatistics(
                  recentData = mockedRecentData,
                  talkTimeSecMean = talkTimeSecMean,
                  paceMean = paceMean,
                  sessionsGiven = mapOf("INTERVIEW" to 10, "SPEECH" to 5, "NEGOTIATION" to 8),
                  successfulSessions = mapOf("INTERVIEW" to 7, "SPEECH" to 3, "NEGOTIATION" to 4)),
          friends = listOf("friend1", "friend2"),
          bio = "Test bio")

  @Test
  fun graphStats_displaysAllTitlesAndGraphs() {
    composeTestRule.setContent { GraphStats(profileViewModel = userProfileViewModel) }

    // Check Screen Title
    composeTestRule
        .onNodeWithTag("graphScreenTitle", useUnmergedTree = true)
        .assertIsDisplayed()
        .assert(hasText("Your Stats"))

    // Check Talk Time Seconds Title
    composeTestRule
        .onNodeWithTag("talkTimeSecTitle")
        .assertIsDisplayed()
        .assert(hasText("Talk Time Seconds:"))

    // Check Talk Time Graph
    composeTestRule.onNodeWithTag("talkTimeSecGraph").assertIsDisplayed()

    // Check Talk Time Mean
    composeTestRule
        .onNodeWithTag("talkTimeSecMeanTitle")
        .assertIsDisplayed()
        .assert(hasText("Mean: ${talkTimeSecMean}"))

    // Check Pace Title
    composeTestRule.onNodeWithTag("paceTitle").assertIsDisplayed().assert(hasText("Pace:"))

    // Check Pace Graph
    composeTestRule.onNodeWithTag("paceGraph").assertIsDisplayed()

    // Check Pace Mean
    composeTestRule
        .onNodeWithTag("paceMeanTitle")
        .assertIsDisplayed()
        .assert(hasText("Mean: ${paceMean}"))
  }

  @Test
  fun titleAndStatsRow_displaysCorrectStats() {
    composeTestRule.setContent { TitleAndStatsRow(profile = testUserProfile) }

    // Check Interview Stats
    composeTestRule
        .onNodeWithTag("InterviewTitle")
        .assertIsDisplayed()
        .assertTextEquals("Interview")
    composeTestRule
        .onNodeWithTag("totalSessionsInterviewTitle")
        .assertIsDisplayed()
        .assertTextEquals("Sessions: 10")
    composeTestRule
        .onNodeWithTag("successSessionsInterviewTitle")
        .assertIsDisplayed()
        .assertTextEquals("Successful: 7")

    // Check Speech Stats
    composeTestRule.onNodeWithTag("SpeechTitle").assertIsDisplayed().assertTextEquals("Speech")
    composeTestRule
        .onNodeWithTag("totalSessionsSpeechTitle")
        .assertIsDisplayed()
        .assertTextEquals("Sessions: 5")
    composeTestRule
        .onNodeWithTag("successSessionsSpeechTitle")
        .assertIsDisplayed()
        .assertTextEquals("Successful: 3")

    // Check Negotiation Stats
    composeTestRule
        .onNodeWithTag("NegotiationTitle")
        .assertIsDisplayed()
        .assertTextEquals("Negotiation")
    composeTestRule
        .onNodeWithTag("totalSessionsNegotiationTitle")
        .assertIsDisplayed()
        .assertTextEquals("Sessions: 8")
    composeTestRule
        .onNodeWithTag("successSessionsNegotiationTitle")
        .assertIsDisplayed()
        .assertTextEquals("Successful: 4")
  }
}

fun createMockedRecentData(): ArrayDeque<AnalysisData> {
  val mockedRecentData = ArrayDeque<AnalysisData>()
  for (i in 1..10) {
    mockedRecentData.addFirst(
        AnalysisData(
            transcription = "a",
            fillerWordsCount = 0,
            averagePauseDuration = 0.0,
            talkTimeSeconds = i.toDouble(),
            talkTimePercentage = 0.0,
            pace = i))
  }
  return mockedRecentData
}
