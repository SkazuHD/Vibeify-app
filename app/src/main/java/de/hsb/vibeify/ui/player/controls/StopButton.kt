package de.hsb.vibeify.ui.player.controls

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
// StopButton is a composable that provides a button to stop playback in the player.
internal fun StopButton(player: Player, modifier: Modifier = Modifier) {
    val state = rememberStopButtonState(player)
    val icon = Icons.Default.Stop
    val contentDescription =
        if (state.stopped) {
            "Stop Playback"
        } else {
            "Playback Stopped"
        }
    IconButton(onClick = state::onClick, modifier = modifier, enabled = state.isEnabled) {
        Icon(icon, contentDescription = contentDescription, modifier = modifier)
    }
}
// This composable uses the StopButtonState to manage the state of the stop button,
@OptIn(UnstableApi::class)
@Composable
fun rememberStopButtonState(player: Player): StopButtonState {
    val stopButtonState = remember(player) { StopButtonState(player) }
    LaunchedEffect(player) { stopButtonState.observe() }
    return stopButtonState
}

// StopButtonState is a state holder for the stop button, managing its enabled state and whether playback has stopped.
@UnstableApi
class StopButtonState(private val player: Player) {
    var isEnabled by mutableStateOf(player.isCommandAvailable(Player.COMMAND_STOP))
        private set

    var stopped by mutableStateOf(player.isPlaying)
        private set

    fun onClick() {
        if (isEnabled) {
            player.stop()
            player.clearMediaItems()
        }
    }

    // observe listens to player events and updates the state accordingly.
    suspend fun observe(): Nothing =
        player.listen { events ->
            if (
                events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                )
            ) {
                stopped = !player.isPlaying
                isEnabled = isCommandAvailable(Player.COMMAND_STOP)
            }
        }
}