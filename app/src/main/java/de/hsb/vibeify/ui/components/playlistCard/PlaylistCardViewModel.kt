package de.hsb.vibeify.ui.components.playlistCard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.PlaylistService
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class PlaylistCardViewModel @Inject constructor(
    private val playlistService: PlaylistService,
) : ViewModel() {

    fun isPlaylistFavorite(playlistId: String): Boolean {
        return runBlocking {
            playlistService.getPlaylistDetail(playlistId)?.let { playlist ->
                playlist.isFavorite && !playlist.isOwner
            } ?: false
        }
    }


}