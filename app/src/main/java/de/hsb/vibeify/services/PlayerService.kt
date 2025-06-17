package de.hsb.vibeify.services

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object PlayerService {
    @Volatile
    private var player: ExoPlayer? = null

    fun getInstance(context: Context): ExoPlayer {
        return player ?: synchronized(this) {
            player ?: ExoPlayer.Builder(context.applicationContext).build().also { player = it }
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}