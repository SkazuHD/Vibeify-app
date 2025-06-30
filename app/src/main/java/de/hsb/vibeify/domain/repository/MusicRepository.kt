package de.hsb.vibeify.domain.repository

import de.hsb.vibeify.domain.model.Playlist
import de.hsb.vibeify.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain Repository Interface - Clean Contract
 * Keine Implementation Details, nur Business Logic!
 */
interface MusicRepository {

    // Reactive data streams
    fun getFeaturedSongs(): Flow<de.hsb.vibeify.core.result.Result<List<Song>>>
    fun getPlaylistById(id: String): Flow<Result<Playlist>>
    fun getUserPlaylists(userId: String): Flow<Result<List<Playlist>>>

    // Single operations
    suspend fun searchSongs(query: String): de.hsb.vibeify.core.result.Result<List<Song>>
    suspend fun likeSong(songId: String): de.hsb.vibeify.core.result.Result<Unit>
    suspend fun unlikeSong(songId: String): Result<Unit>
    suspend fun createPlaylist(name: String, description: String?): Result<Playlist>
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit>

    // Cache management
    suspend fun refreshFeaturedSongs(): Result<Unit>
}
