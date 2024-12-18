package com.github.se.orator.model.symblAi

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
      CREDENTIALS_ERROR -> "There was an error with the SymblAI data"
      ACCESS_TOKEN_ERROR -> "There was an error with the SymblAI data"
      HTTP_REQUEST_ERROR -> "There was an error when issuing the HTTP request"
      JOB_PROCESSING_ERROR -> "There was an error processing the job"
      MISSING_CONV_ID_ERROR -> "There was an error with the SymblAI data"
      JSON_PARSING_ERROR -> "There was an error processing the received data from SymblAI"
      NO_MESSAGES_FOUND_ERROR -> "There was an error processing the received data from SymblAI"
      NO_ANALYTICS_FOUND_ERROR -> "There was an error processing the received data from SymblAI"
    }
  }
}
