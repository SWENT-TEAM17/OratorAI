package com.github.se.orator.model.apiLink


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.profile.UserProfileRepositoryFirestore
import com.github.se.orator.model.profile.UserProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class apiLinkViewModel : ViewModel() {
    private val _transcribedText = MutableStateFlow<String?>(null)
    val transcribedText = _transcribedText.asStateFlow()

    fun updateTranscribedText(text: String) {
        _transcribedText.value = text
    }

companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return apiLinkViewModel() as T
            }
        }
}
}