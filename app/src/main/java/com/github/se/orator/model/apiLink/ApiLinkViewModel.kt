package com.github.se.orator.model.apiLink

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.PracticeContext
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

  /**
   * Resets all practice data fields to null. Useful to clear all fields when ending a practice
   * session.
   */
  fun resetAllPracticeData() {
    clearAnalysisData()
    clearPracticeContext()
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
