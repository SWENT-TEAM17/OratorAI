package com.github.se.orator.model.offlinePrompts

import android.content.Context
import com.google.gson.Gson
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class OfflinePromptsFunctionsTest {

  private lateinit var offlinePromptsFunctions: OfflinePromptsFunctions
  private lateinit var mockContext: Context
  private lateinit var mockCacheDir: File

  @Before
  fun setUp() {
    offlinePromptsFunctions = OfflinePromptsFunctions()
    mockContext = mock(Context::class.java)
    mockCacheDir = mock(File::class.java)

    `when`(mockContext.cacheDir).thenReturn(mockCacheDir)
  }

  @Test
  fun `loadPromptsFromFile returns prompts if file exists`() {
    val prompts =
        listOf(mapOf("targetCompany" to "Google", "jobPosition" to "Engineer", "ID" to "1234"))
    val file = mock(File::class.java)

    `when`(mockCacheDir.resolve("prompts_cache.json")).thenReturn(file)
    `when`(file.exists()).thenReturn(true)
    `when`(file.readText()).thenReturn(Gson().toJson(prompts))

    val result = offlinePromptsFunctions.loadPromptsFromFile(mockContext)

    assertEquals(prompts, result)
  }

  @Test
  fun `loadPromptsFromFile returns null if file does not exist`() {
    val file = mock(File::class.java)

    `when`(mockCacheDir.resolve("prompts_cache.json")).thenReturn(file)
    `when`(file.exists()).thenReturn(false)

    val result = offlinePromptsFunctions.loadPromptsFromFile(mockContext)

    assertEquals(null, result)
  }

  @Test
  fun `createEmptyPromptFile creates file with header`() {
    val file = mock(File::class.java)
    val id = "1234"

    `when`(mockCacheDir.resolve("$id.txt")).thenReturn(file)

    offlinePromptsFunctions.createEmptyPromptFile(mockContext, id)

    verify(file).writeText("0//!")
  }

  @Test
  fun `writeToPromptFile writes to file and updates state`() = runTest {
    val file = mock(File::class.java)
    val id = "1234"
    val prompt = "Interview prompt"

    `when`(mockCacheDir.resolve("$id.txt")).thenReturn(file)

    offlinePromptsFunctions.writeToPromptFile(mockContext, id, prompt)

    verify(file).writeText(prompt)
    assertEquals(prompt, offlinePromptsFunctions.fileData.first())
  }

  @Test
  fun `readPromptTextFile updates fileData with file contents`() = runTest {
    val file = mock(File::class.java)
    val id = "1234"
    val fileContents = "File content"

    `when`(mockCacheDir.resolve("$id.txt")).thenReturn(file)
    `when`(file.readText()).thenReturn(fileContents)

    offlinePromptsFunctions.readPromptTextFile(mockContext, id)

    assertEquals(fileContents, offlinePromptsFunctions.fileData.first())
  }

  @Test
  fun `readPromptTextFile sets loading message if file is empty`() = runTest {
    val file = mock(File::class.java)
    val id = "1234"
    doReturn(true).`when`(file).exists()
    doReturn(file).`when`(mockCacheDir).resolve("$id.txt")

    //        when(bar.getFoo()).thenReturn(fooBar)
    //        to
    //
    //        doReturn(fooBar).when(bar).getFoo()
    doReturn("").`when`(file).readText()

    offlinePromptsFunctions.readPromptTextFile(mockContext, id)

    assertEquals("Loading interviewer response...", offlinePromptsFunctions.fileData.first())
  }

  @Test
  fun `clearDisplayText resets fileData state`() = runTest {
    offlinePromptsFunctions.clearDisplayText()

    assertEquals("", offlinePromptsFunctions.fileData.first())
  }
}
