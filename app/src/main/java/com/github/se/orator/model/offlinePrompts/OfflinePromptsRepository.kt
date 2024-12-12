package com.github.se.orator.model.offlinePrompts

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter

class OfflinePromptsRepository: OfflinePromptsRepoInterface {

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
//        val writer = FileWriter(fileName)
//        writer.write(fileIsEmptyHeader)
//        writer.close()
        file.writeText(fileIsEmptyHeader)
    }

    override fun writeToPromptFile(context: Context, ID: String, prompt: String) {
        val fileName = "$ID.txt"
        val file = File(context.cacheDir, fileName)
        val fileContents: String = file.reader().use{it.readText()}
        if (fileContents == "0//!") {
            val writer = FileWriter(fileName)
            writer.write(prompt)
            writer.close()
        }
        else {
            Log.d("in writeToPromptFile function: ", "file is not empty!")
        }
    }

    override fun openPromptTextFile(context: Context, ID: String): String {
        val fileName = "$ID.txt"
        val file = File(context.cacheDir, fileName)
        val fileContents: String = file.reader().use{it.readText()}

        if (fileContents == "0//!") {
            return "Loading interviewer response..."
        }
        else {
            return fileContents
        }
    }
}