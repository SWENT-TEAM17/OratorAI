import android.content.Context
import com.google.gson.Gson
import java.io.File

fun savePromptsToFile(prompts: Map<String, String>, context: Context) {
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

fun loadPromptsFromFile(context: Context): List<Map<String, String>>? {
  val file = File(context.cacheDir, "prompts_cache.json")
  return if (file.exists()) {
    val json = file.readText()
    Gson().fromJson(json, List::class.java) as List<Map<String, String>>
  } else null
}
