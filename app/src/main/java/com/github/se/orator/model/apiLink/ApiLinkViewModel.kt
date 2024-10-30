package com.github.se.orator.model.apiLink


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.speaking.AnalysisData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApiLinkViewModel : ViewModel() {
    private val _transcribedText = MutableStateFlow<AnalysisData?>(null)
    val transcribedText = _transcribedText.asStateFlow()

    fun updateAnalysisData(analysisData: AnalysisData) {
        _transcribedText.value = analysisData
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