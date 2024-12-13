package com.github.se.orator.model.offlinePrompts

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class OfflinePromptsFunctions: OfflinePromptsFunctionsInterface {

    private val _fileData = MutableStateFlow<String?>("")
    override val fileData: StateFlow<String?> = _fileData.asStateFlow()

    /**
     * Helper function that allows to save offline recording context
     *
     * @param prompts : dictionary mapping strings to strings. It is of the following format:
     * targetCompany: the target company for this interview
     * jobPosition: the target job position for this interview
     * ID: the uniquely identifying, randomly generated string that is the title of the feedback prompt text file and the audio recording of the interview
     */
    override fun savePromptsToFile(prompts: Map<String, String>, context: Context) {
        val file = File(context.cacheDir, "prompts_cache.json")

        // Load existing list of prompts if the file exists
        val existingPrompts: MutableList<Map<String, String>> =
            if (file.exists()) {
                val json = file.readText()
                Gson().fromJson(json, List::class.java) as List<Map<String, String>>
            } else {
                mutableListOf()
            }
                .toMutableList()

        // Add the new prompts to the list
        existingPrompts.add(prompts)

        // Save the updated list back to the file
        val json = Gson().toJson(existingPrompts)
        file.writeText(json)
    }

    override fun loadPromptsFromFile(context: Context): List<Map<String, String>>? {
        val file = File(context.cacheDir, "prompts_cache.json")
        return if (file.exists()) {
            val json = file.readText()
            Gson().fromJson(json, List::class.java) as List<Map<String, String>>
        } else null
    }

    override fun createEmptyPromptFile(context: Context, ID: String) {
        val fileName = "$ID.txt"
        val fileIsEmptyHeader = "0//!"
        val file = File(context.cacheDir, fileName)
        file.writeText(fileIsEmptyHeader)
    }

    override fun writeToPromptFile(context: Context, ID: String, prompt: String) {
        val fileName = "$ID.txt"
        val file = File(context.cacheDir, fileName)
        file.writeText(prompt)
        _fileData.value = prompt
        Log.d("wrote to file $fileName", "$prompt")
//        if (fileContents == "0//!") {
//            file.writeText(prompt)
//        }
//        else {
//            Log.d("in writeToPromptFile function: ", "file is not empty!")
//        }
    }

    override fun readPromptTextFile(context: Context, ID: String) {
        val fileName = "$ID.txt"
        Log.d("repo readptf", "inside the txt file $fileName")
        val file = File(context.cacheDir, fileName)
        val fileContents: String = file.readText()

        if (fileContents == "0//!" || fileContents == "") {
            _fileData.value = "Loading interviewer response..."
        }
        else {
            _fileData.value = fileContents
            Log.d("off prompt", "file name: $fileName; repo stuff $fileContents")
        }
    }

    override fun clearDisplayText() {
        _fileData.value = ""
    }
}