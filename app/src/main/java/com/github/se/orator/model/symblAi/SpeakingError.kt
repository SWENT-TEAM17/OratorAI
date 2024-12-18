package com.github.se.orator.model.symblAi

/**
 * Enum class to represent the different errors that can occur during the processing of the user's
 * speech.
 */
enum class SpeakingError {
  NO_ERROR,
  CREDENTIALS_ERROR,
  ACCESS_TOKEN_ERROR,
  HTTP_REQUEST_ERROR,
  JOB_PROCESSING_ERROR,
  MISSING_CONV_ID_ERROR,
  JSON_PARSING_ERROR,
  NO_MESSAGES_FOUND_ERROR,
  NO_ANALYTICS_FOUND_ERROR;

  override fun toString(): String {
    return when (this) {
      NO_ERROR -> "No error"
      CREDENTIALS_ERROR -> Companion.STRING_SYMBL_DATA_ERROR
      ACCESS_TOKEN_ERROR -> Companion.STRING_SYMBL_DATA_ERROR
      HTTP_REQUEST_ERROR -> Companion.STRING_HTTP_ERROR
      JOB_PROCESSING_ERROR -> Companion.STRING_SYMBL_JOB_ERROR
      MISSING_CONV_ID_ERROR -> Companion.STRING_SYMBL_DATA_ERROR
      JSON_PARSING_ERROR -> Companion.STRING_SYMBL_PROCESSING_ERROR
      NO_MESSAGES_FOUND_ERROR -> Companion.STRING_SYMBL_PROCESSING_ERROR
      NO_ANALYTICS_FOUND_ERROR -> Companion.STRING_SYMBL_PROCESSING_ERROR
    }
  }

  companion object {
    const val STRING_HTTP_ERROR = "There was an error when issuing the HTTP request"
    const val STRING_SYMBL_DATA_ERROR = "There was an error with the SymblAI data"
    const val STRING_SYMBL_JOB_ERROR = "There was an error processing the job"
    const val STRING_SYMBL_PROCESSING_ERROR = "There was an error processing the data from SymblAI"
  }
}
