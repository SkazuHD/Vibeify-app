package de.hsb.vibeify.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.hsb.vibeify.services.PlayerServiceLocator

class PlayerControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("🔥 Received intent: ${intent.action}")

        val playerService = PlayerServiceLocator.get()
        if (playerService == null) {
            println("❌ PlayerService is null!")
            return
        }

        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                println("🟡 Action: PLAY_PAUSE")
                if (playerService.isPlaying.value) {
                    println("⏸ Pausing...")
                    playerService.pause()
                } else {
                    println("▶️ Resuming...")
                    playerService.resume()
                }
            }
            ACTION_NEXT -> {
                println("⏭ Skipping to next")
                playerService.skipToNext()
            }
            ACTION_PREVIOUS -> {
                println("⏮ Skipping to previous")
                playerService.skipToPrevious()
            }
            else -> println("❓ Unknown action")
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "de.hsb.vibeify.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "de.hsb.vibeify.ACTION_NEXT"
        const val ACTION_PREVIOUS = "de.hsb.vibeify.ACTION_PREVIOUS"
    }
}
