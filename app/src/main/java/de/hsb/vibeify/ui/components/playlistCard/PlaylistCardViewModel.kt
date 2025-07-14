package de.hsb.vibeify.ui.components.playlistCard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.PlaylistService
import javax.inject.Inject

// PlaylistCardViewModel handles the logic for checking if a playlist is favorite.
@HiltViewModel
class PlaylistCardViewModel @Inject constructor(
    private val playlistService: PlaylistService,
) : ViewModel() {
    suspend fun isPlaylistFavorite(playlistId: String): Boolean {
        return playlistService.isPlaylistLiked(playlistId)
    }
}