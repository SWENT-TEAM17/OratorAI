package com.github.se.orator.ui.battle

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.model.speechBattle.BattleRepository
import com.github.se.orator.model.speechBattle.BattleStatus
import com.github.se.orator.model.speechBattle.BattleViewModel
import com.github.se.orator.model.speechBattle.SpeechBattle
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.network.ChatGPTService
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class BattleScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockUserProfileRepository: UserProfileRepository
  @Mock private lateinit var mockBattleRepository: BattleRepository
  @Mock private lateinit var chatGPTService: ChatGPTService

  private lateinit var userProfileViewModel: UserProfileViewModel
  private lateinit var apiLinkViewModel: ApiLinkViewModel
  private lateinit var chatViewModel: ChatViewModel
  private lateinit var battleViewModel: BattleViewModel

  private val friendUid = "friendUid"
  private val friendName = "Friend Name"
  private val testBattleId = "testBattleId"

  // Variable to capture the SpeechBattle object
  private var capturedSpeechBattle: SpeechBattle? = null

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock getCurrentUserUid to return "testUser"
    `when`(mockUserProfileRepository.getCurrentUserUid()).thenReturn("testUser")

    // Mock getUserProfile to return the friend's UserProfile
    `when`(mockUserProfileRepository.getUserProfile(eq(friendUid), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      val userProfile =
          UserProfile(uid = friendUid, name = friendName, age = 100, statistics = UserStatistics())
      onSuccess(userProfile)
      null
    }

    // Mock getUserProfile for the current user
    `when`(mockUserProfileRepository.getUserProfile(eq("testUser"), any(), any())).thenAnswer {
        invocation ->
      val onSuccess = invocation.getArgument<(UserProfile?) -> Unit>(1)
      val userProfile =
          UserProfile(
              uid = "testUser", name = "Test User", age = 100, statistics = UserStatistics())
      onSuccess(userProfile)
      null
    }

    // Mock BattleRepository's generateUniqueBattleId to return testBattleId
    `when`(mockBattleRepository.generateUniqueBattleId()).thenReturn(testBattleId)

    // Mock storeBattleRequest to capture the SpeechBattle object
    doAnswer { invocation ->
          val speechBattle = invocation.getArgument<SpeechBattle>(0)
          val callback = invocation.getArgument<(Boolean) -> Unit>(1)
          // Capture the SpeechBattle
          capturedSpeechBattle = speechBattle
          callback(true)
          null
        }
        .`when`(mockBattleRepository)
        .storeBattleRequest(any(), any())

    // Initialize ViewModels
    userProfileViewModel = UserProfileViewModel(mockUserProfileRepository)
    apiLinkViewModel = ApiLinkViewModel()
    chatViewModel = ChatViewModel(chatGPTService, apiLinkViewModel)
    battleViewModel =
        BattleViewModel(
            battleRepository = mockBattleRepository,
            userProfileViewModel = userProfileViewModel,
            navigationActions = mockNavigationActions,
            apiLinkViewModel = apiLinkViewModel,
            chatViewModel = chatViewModel)

    // Set the composable content
    composeTestRule.setContent {
      BattleScreen(
          friendUid = friendUid,
          userProfileViewModel = userProfileViewModel,
          navigationActions = mockNavigationActions,
          battleViewModel = battleViewModel)
    }

    // Close keyboard after input operations
    Espresso.closeSoftKeyboard()
  }

  /**
   * Test that sending a battle request with all fields filled calls storeBattleRequest and
   * navigates correctly.
   */
  @Test
  fun testSendBattleRequestWithAllFieldsFilled() {
    // Input all required fields
    inputText("targetPositionInput-TextField", "Software Engineer")
    inputText("companyNameInput-TextField", "Tech Corp")

    // Select interview type from dropdown
    selectDropdownItem("interviewTypeInput-DropdownBox", "Phone Interview")

    // Select experience level
    selectDropdownItem("experienceLevelInput-DropdownBox", "Mid-Level")

    // Input job description using placeholder text to target the BasicTextField
    composeTestRule
        .onNodeWithText("Paste the job description here")
        .performTextInput("Develop and maintain software applications.")

    // Select focus area
    selectDropdownItem("focusAreaInput-DropdownBox", "Technical Questions")

    // Click on the Send Battle Request button
    composeTestRule.onNodeWithTag("getStartedButton").performScrollTo().performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Verify that storeBattleRequest was called
    verify(mockBattleRepository).storeBattleRequest(any(), any())

    // Assert that capturedSpeechBattle is not null
    Assert.assertNotNull(capturedSpeechBattle)
    val speechBattle = capturedSpeechBattle!!

    // Assert the contents of SpeechBattle
    Assert.assertEquals(testBattleId, speechBattle.battleId)
    Assert.assertEquals("testUser", speechBattle.challenger)
    Assert.assertEquals(friendUid, speechBattle.opponent)
    Assert.assertEquals(BattleStatus.PENDING, speechBattle.status)
    Assert.assertEquals("Software Engineer", speechBattle.context.targetPosition)
    Assert.assertEquals("Tech Corp", speechBattle.context.companyName)
    Assert.assertEquals("Phone Interview", speechBattle.context.interviewType)
    Assert.assertEquals("Mid-Level", speechBattle.context.experienceLevel)
    Assert.assertEquals(
        "Develop and maintain software applications.", speechBattle.context.jobDescription)
    Assert.assertEquals("Technical Questions", speechBattle.context.focusArea)

    // Verify navigation to BattleRequestSentScreen
    verify(mockNavigationActions).navigateToBattleRequestSentScreen(friendUid, testBattleId)
  }

  /**
   * Test that sending a battle request with some fields empty does not call storeBattleRequest and
   * does not navigate.
   */
  @Test
  fun testSendBattleRequestWithEmptyFields() {
    // Fill only some required fields
    inputText("targetPositionInput-TextField", "Software Engineer")
    // Leave companyNameInput empty

    // Select interview type from dropdown
    selectDropdownItem("interviewTypeInput-DropdownBox", "Phone Interview")

    // Select experience level
    selectDropdownItem("experienceLevelInput-DropdownBox", "Mid-Level")

    // Input job description using placeholder text to target the BasicTextField
    composeTestRule
        .onNodeWithText("Paste the job description here")
        .performTextInput("Develop and maintain software applications.")

    // Select focus area
    selectDropdownItem("focusAreaInput-DropdownBox", "Technical Questions")

    // Click on the Send Battle Request button
    composeTestRule.onNodeWithTag("getStartedButton").performScrollTo().performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Verify that storeBattleRequest was never called since not all fields are filled
    verify(mockBattleRepository, never()).storeBattleRequest(any(), any())

    // Verify that navigation to BattleRequestSentScreen was never called
    verify(mockNavigationActions, never())
        .navigateToBattleRequestSentScreen(anyString(), anyString())
  }

  /** Helper function to input text into a TextField with the given testTag. */
  private fun inputText(testTag: String, text: String) {
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag(testTag))
    composeTestRule.onNodeWithTag(testTag).performTextInput(text)
    composeTestRule.onNodeWithTag(testTag).assertTextContains(text)
  }

  /** Helper function to select an item from a dropdown with the given dropdownBoxTag. */
  private fun selectDropdownItem(dropdownBoxTag: String, itemText: String) {
    composeTestRule.onNodeWithTag("content").performScrollToNode(hasTestTag(dropdownBoxTag))
    composeTestRule.onNodeWithTag(dropdownBoxTag).performClick()
    composeTestRule.onNodeWithText(itemText).performClick()
    // After selection, verify that the chosen text is visible in the field
    composeTestRule
        .onNodeWithTag(dropdownBoxTag.replace("DropdownBox", "DropdownField"))
        .assertTextContains(itemText)
  }
}
