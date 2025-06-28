package de.hsb.vibeify.services

import de.hsb.vibeify.data.model.Album
import de.hsb.vibeify.data.model.Artist
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.ArtistRepository
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private val playlistRepository: PlaylistRepository,
    private val artistRepository: ArtistRepository
) : SearchService{
    @Volatile
    private var currentSearchJob: Job? = null

    override suspend fun search(query: String): SearchResult = coroutineScope {
        currentSearchJob?.cancel()

        currentSearchJob = coroutineContext[Job]

        try {
            val songsDeferred = async { songRepository.searchSongs(query) }
            val playlistsDeferred = async { playlistRepository.searchPlaylists(query) }
            val artistsDeferred = async { artistRepository.searchArtists(query) }

            val res = SearchResult(
                songs = songsDeferred.await(),
                playlists = playlistsDeferred.await(),
                // Make searchable if enough time is left
                artists = artistsDeferred.await(),
                albums = emptyList()
            )
            val sortedResult = sortByRelevance(res, query)
            sortedResult
        } catch (_: CancellationException) {
            SearchResult()
        } finally {
            if (currentSearchJob == coroutineContext[Job]) {
                currentSearchJob = null
            }
        }
    }

    fun sortByRelevance(
        searchResult: SearchResult,
        query: String
    ): SearchResult {
        val lowerQuery = query.lowercase()

        val sortedSongs = searchResult.songs.sortedByDescending { song ->
            calculateSongRelevanceScore(song, lowerQuery)
        }

        val sortedPlaylists = searchResult.playlists.sortedByDescending { playlist ->
            playlist.title.lowercase().indexOf(lowerQuery)
        }

        val sortedArtists = searchResult.artists.sortedByDescending { artist ->
            calculateArtistRelevanceScore(artist, lowerQuery)
        }

        return searchResult.copy(
            songs = sortedSongs,
            playlists = sortedPlaylists,
            artists = sortedArtists,
        )
    }

    private fun calculateArtistRelevanceScore(artist: Artist, lowerQuery: String): Int {
        val artistName = artist.name.lowercase()
        var score = 0

        if (artistName == lowerQuery) {
            score += 1000
        } else if (artistName.startsWith(lowerQuery)) {
            score += 800
        } else if (artistName.contains(" $lowerQuery")) {
            score += 600
        } else if (artistName.contains(lowerQuery)) {
            score += 400
        }

        return score
    }


    private fun calculateSongRelevanceScore(song: Song, lowerQuery: String): Int {
        val songName = song.name.lowercase()
        val artistName = song.artist?.lowercase() ?: ""
        val albumName = song.album?.lowercase() ?: ""

        var score = 0

        if (songName == lowerQuery) {
            score += 1000
        }
        else if (songName.startsWith(lowerQuery)) {
            score += 800
        }
        else if (songName.contains(" $lowerQuery")) {
            score += 600
        }
        else if (songName.contains(lowerQuery)) {
            score += 400
        }

        if (artistName.isNotEmpty()) {
            if (artistName == lowerQuery) {
                score += 700
            } else if (artistName.startsWith(lowerQuery)) {
                score += 500
            } else if (artistName.contains(" $lowerQuery")) {
                score += 300
            } else if (artistName.contains(lowerQuery)) {
                score += 200
            }
        }

        if (albumName.isNotEmpty()) {
            if (albumName == lowerQuery) {
                score += 300
            } else if (albumName.startsWith(lowerQuery)) {
                score += 200
            } else if (albumName.contains(" $lowerQuery")) {
                score += 100
            } else if (albumName.contains(lowerQuery)) {
                score += 50
            }
        }

        if (songName.contains(lowerQuery)) {
            val nameLength = songName.length
            val queryLength = lowerQuery.length
            val lengthRatio = queryLength.toFloat() / nameLength
            score += (lengthRatio * 100).toInt()
        }

        return score
    }
}