package com.github.se.orator.model.symblAi

import com.github.se.orator.model.speaking.AnalysisData
import java.io.File

interface VoiceAnalysisApi {
  fun getTranscription(
      audioFile: File,
      onSuccess: (AnalysisData) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
