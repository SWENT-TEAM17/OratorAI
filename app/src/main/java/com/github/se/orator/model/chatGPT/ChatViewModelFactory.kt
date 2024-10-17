package com.github.se.orator.model.chatGPT

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.ui.network.ChatGPTService

class ChatViewModelFactory(private val chatGPTService: ChatGPTService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatGPTService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}