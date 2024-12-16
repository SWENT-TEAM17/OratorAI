package com.github.se.orator.model.offlinePrompts

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface OfflinePromptsFunctionsInterface {
  val fileData: StateFlow<String?>

  val TRANSCRIBED: String

  fun savePromptsToFile(prompts: Map<String, String>, context: Context)

  fun loadPromptsFromFile(context: Context): List<Map<String, String>>?

  fun createEmptyPromptFile(context: Context, ID: String)

  fun writeToPromptFile(context: Context, ID: String, prompt: String)

  fun readPromptTextFile(context: Context, ID: String)

  fun clearDisplayText()

  fun changePromptStatus(id: String, context: Context, entry: String,value: String = "1"): Boolean
  fun getPromptMapElement(id: String, element: String, context: Context): String?
  fun stopFeedback(ID: String, context: Context)
}
