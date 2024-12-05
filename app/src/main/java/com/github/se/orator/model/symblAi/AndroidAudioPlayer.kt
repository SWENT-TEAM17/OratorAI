package com.github.se.orator.model.symblAi

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File

class AndroidAudioPlayer(private val context: Context) : AudioPlayer {
  private var player: MediaPlayer? = null

  override fun playFile(audioFile: File) {
    MediaPlayer.create(context, audioFile.toUri()).apply {
      player = this
      start()
    }
  }

  override fun stop() {
    player?.stop()
    player?.release()
    player = null
  }
}
