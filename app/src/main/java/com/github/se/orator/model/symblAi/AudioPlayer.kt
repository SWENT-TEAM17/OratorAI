package com.github.se.orator.model.symblAi

import java.io.File

interface AudioPlayer {
  fun playFile(audioFile: File)

  fun stop()
}
