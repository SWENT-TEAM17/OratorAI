package com.github.se.orator.model.apiLink

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PracticeContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * This class offers a way to create communication between view models to efficiently share data
 * about the analysis of the user's speech.
 */
class ApiLinkViewModel : ViewModel() {
  private val _analysisData = MutableStateFlow<AnalysisData?>(null)

  /** The analysis data to be shared with other view models that need it. */
  val analysisData = _analysisData.asStateFlow()

  private val _practiceContext = MutableStateFlow<PracticeContext?>(null)

  /** The practice context to be shared with other view models that need it. */
  val practiceContext = _practiceContext.asStateFlow()

  /**
   * Sets the analysis data to be shared with other view models.
   *
   * @param analysisData The analysis data to be shared.
   */
  fun updateAnalysisData(analysisData: AnalysisData) {
    _analysisData.value = analysisData
    Log.d("ApiLinkViewModel", "Analysis data set: ${this.analysisData.value}")
  }

  /**
   * Sets the practice context to be shared with other view models.
   *
   * @param practiceContext The practice context to be shared.
   */
  fun updatePracticeContext(practiceContext: PracticeContext) {
    _practiceContext.value = practiceContext
    Log.e("ApiLinkViewModel", "Practice context: $practiceContext")
  }

  /** Sets the analysis data field to null */
  fun clearAnalysisData() {
    _analysisData.value = null
  }

  /** Sets the practice context field to null */
  fun clearPracticeContext() {
    _practiceContext.value = null
  }

  /** Returns the current practice context, or null if not set. */
  fun getCurrentPracticeContext(): PracticeContext? {
    return _practiceContext.value
  }

  /**
   * Resets all practice data fields to null. Useful to clear all fields when ending a practice
   * session.
   */
  fun resetAllPracticeData() {
    clearAnalysisData()
    clearPracticeContext()
  }

  /**
   * Returns a list of tips based on the current practice context. If no context is set, a generic
   * list of tips is returned.
   */
  fun getTipsForModule(): List<String> {
    // Example tips for each module
    val interviewTips =
        listOf(
            "Avoid filler words like 'um' or 'uh' to sound more confident.",
            "Maintain a steady pace and organize your thoughts before speaking.",
            "Research the company's culture and align your answers accordingly.",
            "Highlight both technical and soft skills relevant to the role.",
            "Demonstrate problem-solving abilities with concrete examples.")

    val publicSpeakingTips =
        listOf(
            "Engage with your audience by varying your tone and volume.",
            "Use a clear structure: introduction, main points, and conclusion.",
            "Incorporate storytelling to make your speech memorable.",
            "Practice your pacing to avoid rushing through key points.",
            "Anticipate audience questions and prepare responses.")

    val salesPitchTips =
        listOf(
            "Emphasize unique selling points that differentiate you from competitors.",
            "Address potential objections head-on and confidently.",
            "Focus on the client's needs rather than just product features.",
            "Use concrete metrics and case studies to demonstrate value.",
            "Show adaptability if the client raises unexpected concerns.")

    // Return tips based on the current practice context
    return when (practiceContext.value) {
      is InterviewContext -> interviewTips
      is PublicSpeakingContext -> publicSpeakingTips
      is SalesPitchContext -> salesPitchTips
      else -> {
        // If no context or a different one is set, return a generic tip list
        listOf("Stay calm, be clear, and remember your main goals!")
      }
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ApiLinkViewModel() as T
          }
        }
  }
}
