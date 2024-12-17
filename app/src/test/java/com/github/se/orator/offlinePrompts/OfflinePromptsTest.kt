package com.github.se.orator.model.offlinePrompts

import android.content.Context
import com.google.gson.Gson
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class OfflinePromptsFunctionsTest {

  private lateinit var offlinePromptsFunctions: OfflinePromptsFunctions
  private lateinit var mockContext: Context
  private lateinit var mockCacheDir: File
  private lateinit var mockFile: File

  @Before
  fun setUp() {
    offlinePromptsFunctions = OfflinePromptsFunctions()
    mockContext = mock(Context::class.java)
    mockCacheDir = mock(File::class.java)
    mockFile = mock(File::class.java)

    `when`(mockContext.cacheDir).thenReturn(mockCacheDir)
  }

  @Test
  fun `savePromptsToFile adds prompt to existing file`() {
    val prompts = mapOf("ID" to "1234", "targetCompany" to "Google", "jobPosition" to "Engineer")
    val tempDir = createTempDir() // Create a real temporary directory
    val file = File(tempDir, "prompts_cache.json") // put the file in the cache

    // create the file
    val existingPrompts =
        mutableListOf(
            mapOf("ID" to "5678", "targetCompany" to "Apple", "jobPosition" to "Designer"))
    file.writeText(Gson().toJson(existingPrompts))

    `when`(mockContext.cacheDir)
        .thenReturn(tempDir) // Mock cacheDir to point to the temporary directory

    offlinePromptsFunctions.savePromptsToFile(prompts, mockContext)

    // Assert the function called above works fine
    assertTrue(file.exists()) // Verify that the file exists
    val fileContents = file.readText()
    val updatedPrompts =
        Gson().fromJson(fileContents, List::class.java) as List<Map<String, String>>

    // Verify both the old and new prompts are in the file
    assertEquals(2, updatedPrompts.size)
    assertTrue(updatedPrompts.any { it["ID"] == "5678" && it["targetCompany"] == "Apple" })
    assertTrue(updatedPrompts.any { it["ID"] == "1234" && it["targetCompany"] == "Google" })
  }

  @Test
  fun `savePromptsToFile creates new file if it does not exist`() {
    val prompts = mapOf("ID" to "1234", "targetCompany" to "Google", "jobPosition" to "Engineer")
    val tempDir = createTempDir() // Create a real temporary directory
    val file = File(tempDir, "prompts_cache.json")

    // Ensure the file does not exist initially
    assertFalse(file.exists())

    `when`(mockContext.cacheDir).thenReturn(tempDir) // Mock cacheDir to point to the temp directory

    // Act
    offlinePromptsFunctions.savePromptsToFile(prompts, mockContext)

    // Assert
    assertTrue(file.exists()) // Verify that the file was created
    val fileContents = file.readText()
    assertTrue(fileContents.contains("1234")) // Verify that the ID is in the file
    assertTrue(fileContents.contains("Google")) // Verify that the targetCompany is in the file
    assertTrue(fileContents.contains("Engineer")) // Verify that the jobPosition is in the file
  }

  @Test
  fun `createEmptyPromptFile writes empty header`() {
    // Arrange
    val id = "1234"
    val fileName = "$id.txt"
    val fileIsEmptyHeader = "0//!"
    val tempDir = createTempDir()
    val context = mock(Context::class.java)

    // Mock cacheDir to return the real tempDir
    `when`(context.cacheDir).thenReturn(tempDir)

    // Act
    offlinePromptsFunctions.createEmptyPromptFile(context, id)

    // Assert
    val createdFile = File(tempDir, fileName)
    assertTrue(createdFile.exists()) // Verify that the file exists
    assertEquals(fileIsEmptyHeader, createdFile.readText()) // Verify the content
  }

  @Test
  fun `changePromptStatus updates status and returns true`() {
    // Arrange
    val id = "1234"
    val tempDir = createTempDir()
    val fileName = "prompts_cache.json"
    val file = File(tempDir, fileName)

    // Create the initial JSON content with a prompt
    val prompts = mutableListOf(mapOf("ID" to id, "transcribed" to "0"))
    file.writeText(Gson().toJson(prompts)) // Write initial data to the file

    `when`(mockContext.cacheDir).thenReturn(tempDir) // Mock cacheDir to point to tempDir

    val result = offlinePromptsFunctions.changePromptStatus(id, mockContext, "transcribed", "1")
    assertTrue(result) // Verify that the method returned true

    // Verify that the file content has been updated
    val updatedContent = file.readText()
    val updatedPrompts =
        Gson().fromJson(updatedContent, List::class.java) as List<Map<String, String>>
    assertEquals("1", updatedPrompts.first { it["ID"] == id }["transcribed"])
  }

  @Test
  fun `changePromptStatus does not update if value already set`() {
    // Arrange
    val id = "1234"
    val tempDir = createTempDir() // Create a real temporary directory
    val fileName = "prompts_cache.json"
    val file = File(tempDir, fileName)

    // Initial JSON content with "transcribed" already set to "1"
    val prompts = mutableListOf(mapOf("ID" to id, "transcribed" to "1"))
    file.writeText(Gson().toJson(prompts)) // Write the content to the file

    `when`(mockContext.cacheDir).thenReturn(tempDir) // Mock cacheDir to point to the real tempDir

    // try setting it to 1 but it should return false
    val result = offlinePromptsFunctions.changePromptStatus(id, mockContext, "transcribed", "1")
    assertFalse(result) // Verify that the method returns false

    // Verify that the file content remains unchanged
    val unchangedContent = file.readText()
    val unchangedPrompts =
        Gson().fromJson(unchangedContent, List::class.java) as List<Map<String, String>>
    assertEquals("1", unchangedPrompts.first { it["ID"] == id }["transcribed"])
  }

  @Test
  fun `readPromptTextFile sets fileData to file contents`() = runTest {
    // Arrange
    val id = "1234"
    val fileContents = "This is a test"
    val tempDir = createTempDir() // Create a real temporary directory
    val file = File(tempDir, "$id.txt") // Create a real file in the temp directory

    // Write the test content to the file
    file.writeText(fileContents)

    // Mock the cacheDir to point to the real tempDir
    `when`(mockContext.cacheDir).thenReturn(tempDir)

    // Act
    offlinePromptsFunctions.readPromptTextFile(mockContext, id)

    // Assert
    assertEquals(fileContents, offlinePromptsFunctions.fileData.first())
  }

  @Test
  fun `readPromptTextFile sets fileData to loading message for empty file`() = runTest {
    // Arrange
    val id = "1234"
    val tempDir = createTempDir() // Create a real temporary directory
    val file = File(tempDir, "$id.txt") // Create a real file in the temp directory

    // Write empty content to simulate an empty file
    file.writeText("")

    // Mock the cacheDir to point to the real tempDir
    `when`(mockContext.cacheDir).thenReturn(tempDir)

    // Act
    offlinePromptsFunctions.readPromptTextFile(mockContext, id)

    // Assert
    assertEquals("Loading interviewer response...", offlinePromptsFunctions.fileData.first())
  }

  @Test
  fun `clearDisplayText resets fileData`() = runTest {
    // Act
    offlinePromptsFunctions.clearDisplayText()

    // Assert
    assertEquals("", offlinePromptsFunctions.fileData.first())
  }

  @Test
  fun `getPromptMapElement returns correct element`() {
    // Arrange
    val id = "1234"
    val tempDir = createTempDir() // Create a real temporary directory
    val file = File(tempDir, "prompts_cache.json") // Create a real file in the temp directory

    // Write the test content to the file
    val prompts = listOf(mapOf("ID" to id, "transcribed" to "1", "GPTresponse" to "0"))
    file.writeText(Gson().toJson(prompts))

    // Mock the cacheDir to point to the real tempDir
    `when`(mockContext.cacheDir).thenReturn(tempDir)

    // Act
    val result = offlinePromptsFunctions.getPromptMapElement(id, "GPTresponse", mockContext)
    assertEquals("0", result) // Verify that the correct value is returned
  }

  @Test(expected = IllegalArgumentException::class)
  fun `getPromptMapElement throws exception for invalid ID`() {

    val invalidId = "invalid"
    val tempDir = createTempDir() // Create a real temporary directory
    val file = File(tempDir, "prompts_cache.json") // Create a real file in the temp directory

    // Write valid prompt data to the file
    val prompts = listOf(mapOf("ID" to "1234", "transcribed" to "1"))
    file.writeText(Gson().toJson(prompts))

    // Mock the cacheDir to point to the real temporary directory
    `when`(mockContext.cacheDir).thenReturn(tempDir)
    offlinePromptsFunctions.getPromptMapElement(invalidId, "transcribed", mockContext)
  }
}
