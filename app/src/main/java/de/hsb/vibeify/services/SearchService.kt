package de.hsb.vibeify.services

import de.hsb.vibeify.data.model.Album
import de.hsb.vibeify.data.model.Artist
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class SearchResult(
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList()
)


interface SearchService {
    suspend fun search(query: String): SearchResult
}


class SearchServiceImpl @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository
) : SearchService{

    override suspend fun search(query: String): SearchResult = coroutineScope {
        val songsDeferred = async { songRepository.searchSongs(query) }
        val playlistsDeferred = async { playlistRepository.searchPlaylists(query) }

        SearchResult(
            songs = songsDeferred.await(),
            playlists = playlistsDeferred.await(),
            // Make searchable if enough time is left
            artists = emptyList(),
            albums = emptyList()
        )
    }
}