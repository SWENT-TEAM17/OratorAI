import android.content.Context
import com.google.gson.Gson
import java.io.File

fun savePromptsToFile(prompts: Map<String, String>, context: Context) {
    val file = File(context.cacheDir, "prompts_cache.json")
    val json = Gson().toJson(prompts)
    file.writeText(json)
}

fun loadPromptsFromFile(context: Context): Map<String, String>? {
    val file = File(context.cacheDir, "prompts_cache.json")
    return if (file.exists()) {
        val json = file.readText()
        Gson().fromJson(json, Map::class.java) as Map<String, String>
    } else null
}
