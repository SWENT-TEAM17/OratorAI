package com.github.se.orator.model.apiLink

import com.github.se.orator.model.speaking.AnalysisData
import com.github.se.orator.model.speaking.InterviewContext
import org.junit.Before
import org.junit.Test

class ApiLinkViewModelTest {

  private lateinit var apiLinkViewModel: ApiLinkViewModel

  private val analysisData = AnalysisData("test", 0, 0.0, 0.0)
  private val practiceContext = InterviewContext("test", "test", "test", listOf("test"))

  @Before
  fun setUp() {
    apiLinkViewModel = ApiLinkViewModel()
  }

  @Test
  fun testUpdateFields() {

    // AnalysisData
    apiLinkViewModel.updateAnalysisData(analysisData)
    assert(apiLinkViewModel.analysisData.value == analysisData)

    // PracticeContext
    apiLinkViewModel.updatePracticeContext(practiceContext)
    assert(apiLinkViewModel.practiceContext.value == practiceContext)
  }

  @Test
  fun testClearFields() {

    // AnalysisData
    apiLinkViewModel.updateAnalysisData(analysisData)
    apiLinkViewModel.clearAnalysisData()
    assert(apiLinkViewModel.analysisData.value == null)

    // PracticeContext
    apiLinkViewModel.updatePracticeContext(practiceContext)
    apiLinkViewModel.clearPracticeContext()
    assert(apiLinkViewModel.practiceContext.value == null)
  }

  @Test
  fun testClearAll() {

    apiLinkViewModel.updateAnalysisData(analysisData)
    apiLinkViewModel.updatePracticeContext(practiceContext)

    apiLinkViewModel.resetAllPracticeData()

    assert(apiLinkViewModel.analysisData.value == null)
    assert(apiLinkViewModel.practiceContext.value == null)
  }
}
