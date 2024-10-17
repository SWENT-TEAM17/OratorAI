package com.github.se.orator.ui.speaking

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.orator.model.symblAi.SpeakingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class SpeakingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun createMockViewModel(
      isRecording: Boolean = false,
      isProcessing: Boolean = false,
      errorMessage: String? = null,
      transcribedText: String? = null,
      sentimentResult: String? = null,
      fillersResult: String? = null
  ): SpeakingViewModel {
    val mockViewModel = mock<SpeakingViewModel>()
    whenever(mockViewModel.isRecording).thenReturn(MutableStateFlow(isRecording))
    whenever(mockViewModel.isProcessing).thenReturn(MutableStateFlow(isProcessing))
    whenever(mockViewModel.errorMessage).thenReturn(MutableStateFlow(errorMessage))
    whenever(mockViewModel.transcribedText).thenReturn(MutableStateFlow(transcribedText))
    whenever(mockViewModel.sentimentResult).thenReturn(MutableStateFlow(sentimentResult))
    whenever(mockViewModel.fillersResult).thenReturn(MutableStateFlow(fillersResult))
    return mockViewModel
  }

  @Test
  fun testInitialState() {
    val mockViewModel = createMockViewModel()

    composeTestRule.setContent { SpeakingScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag("MicButton").assertExists().assert(hasClickAction())

    composeTestRule
        .onNodeWithTag("FeedbackMessage")
        .assertTextContains("Tap the mic to start recording.")
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag("TranscribedText").assertDoesNotExist()
    composeTestRule.onNodeWithTag("SentimentAnalysis").assertDoesNotExist()
    composeTestRule.onNodeWithTag("FillerWords").assertDoesNotExist()
  }

  @Test
  fun testRecordingState() {
    val mockViewModel = createMockViewModel(isRecording = true)

    composeTestRule.setContent { SpeakingScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithTag("MicButton")
        .assertContentDescriptionContains("Stop recording")
        .assertExists()
        .assert(hasClickAction())

    composeTestRule
        .onNodeWithTag("FeedbackMessage")
        .assertTextContains("Recording...")
        .assertIsDisplayed()
  }

  @Test
  fun testProcessingState() {
    val mockViewModel = createMockViewModel(isProcessing = true)

    composeTestRule.setContent { SpeakingScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithTag("FeedbackMessage")
        .assertTextContains("Processing...")
        .assertIsDisplayed()
  }

  @Test
  fun testErrorState() {
    val error = "Microphone access denied."
    val mockViewModel = createMockViewModel(errorMessage = error)

    composeTestRule.setContent { SpeakingScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithTag("FeedbackMessage")
        .assertTextContains("Error: $error")
        .assertIsDisplayed()
  }

  @Test
  fun testMicButtonClick() {
    // Create a mock ViewModel
    val mockViewModel = createMockViewModel()

    // Set up permissionGranted to be true
    val permissionGranted = true

    composeTestRule.setContent { SpeakingScreen(viewModel = mockViewModel) }

    // Simulate clicking the mic button
    composeTestRule.onNodeWithTag("MicButton").performClick()

    // Use ArgumentCaptor to capture the parameter
    val argumentCaptor = ArgumentCaptor.forClass(Boolean::class.java)
    verify(mockViewModel).onMicButtonClicked(argumentCaptor.capture())
  }

  @Test
  fun testDisplayingResults() {
    val transcribed = "Hello, this is a test."
    val sentiment = "Positive"
    val fillers = "um, uh, like"
    val mockViewModel =
        createMockViewModel(
            transcribedText = transcribed, sentimentResult = sentiment, fillersResult = fillers)

    composeTestRule.setContent { SpeakingScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithTag("TranscribedText")
        .assertTextContains("Transcribed Text: $transcribed")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("SentimentAnalysis")
        .assertTextContains("Sentiment Analysis: $sentiment")
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("FillerWords")
        .assertTextContains("Filler Words: $fillers")
        .assertIsDisplayed()
  }
}
