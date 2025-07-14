package de.hsb.vibeify.services

import android.util.Log
import de.hsb.vibeify.data.model.Album
import de.hsb.vibeify.data.model.Artist
import de.hsb.vibeify.data.model.Genre
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.model.User
import de.hsb.vibeify.data.repository.ArtistRepository
import de.hsb.vibeify.data.repository.PlaylistRepository
import de.hsb.vibeify.data.repository.SongRepository
import de.hsb.vibeify.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Represents the result of a search operation, containing lists of various entities
data class SearchResult(
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val profiles: List<User> = emptyList()
)

interface SearchService {
    suspend fun search(query: String): SearchResult
    val recentSearchQueries: Flow<List<String>?>
}


// Implementation of the SearchService interface
class SearchServiceImpl @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val artistRepository: ArtistRepository,
    private val userRepository: UserRepository,
    private val discoveryService: DiscoveryService
) : SearchService {
    @Volatile
    private var currentSearchJob: Job? = null

    override val recentSearchQueries = userRepository.state.map {
        it.currentUser?.recentSearches
    }.filterNotNull().distinctUntilChanged().map { it ->
        it.takeLast(8)
    }

    /**
     * Searches for songs, playlists, artists, genres, and user profiles based on the provided query.
     * The search is performed concurrently using coroutines for better performance.
     *
     * @param query The search query string.
     * @return A SearchResult object containing the search results.
     */
    override suspend fun search(query: String): SearchResult = coroutineScope {
        currentSearchJob?.cancel()

        currentSearchJob = coroutineContext[Job]

        // Searching concurrently for songs, playlists, artists, genres, and user profiles
        try {
            val songsDeferred = async { songRepository.searchSongs(query) }
            val playlistsDeferred = async { playlistRepository.searchPlaylists(query) }
            val artistsDeferred = async { artistRepository.searchArtists(query) }
            val genresDeferred = async {
                discoveryService.getGenreList().filter { genre ->
                    genre.name.contains(query, ignoreCase = true)
                }
            }
            val profilesDeferred = async {
                userRepository.searchUsers(query)
            }

            // Awaiting all deferred results
            val res = SearchResult(
                songs = songsDeferred.await(),
                playlists = playlistsDeferred.await(),
                artists = artistsDeferred.await(),
                albums = emptyList(),
                genres = genresDeferred.await(),
                profiles = profilesDeferred.await()
            )
            val sortedResult = sortByRelevance(res, query)
            Log.d(
                "SearchService",
                "Search completed for query: $query, found ${sortedResult.songs.size} songs, ${sortedResult.playlists.size} playlists, ${sortedResult.artists.size} artists"
            )
            userRepository.addRecentSearch(query)
            sortedResult
        } catch (_: CancellationException) {
            SearchResult()
        } finally {
            if (currentSearchJob == coroutineContext[Job]) {
                currentSearchJob = null
            }

        }
    }

    /**
     * Sorts the search results by relevance based on the provided query.
     * The sorting is done by calculating a relevance score for each entity.
     *
     * @param searchResult The SearchResult object containing the search results.
     * @param query The search query string.
     * @return A new SearchResult object with sorted entities.
     */
    fun sortByRelevance(
        searchResult: SearchResult,
        query: String
    ): SearchResult {
        val lowerQuery = query.lowercase()

        val sortedSongs = searchResult.songs.sortedByDescending { song ->
            calculateSongRelevanceScore(song, lowerQuery)
        }

        val sortedPlaylists = searchResult.playlists.sortedByDescending { playlist ->
            calculatePlaylistRelevanceScore(playlist, lowerQuery)
        }

        val sortedArtists = searchResult.artists.sortedByDescending { artist ->
            calculateArtistRelevanceScore(artist, lowerQuery)
        }
        val sortedGenres = searchResult.genres.sortedByDescending { genre ->
            genre.name.lowercase().indexOf(lowerQuery)
        }
        val sortedProfiles = searchResult.profiles.sortedByDescending { profile ->
            profile.name.lowercase().indexOf(lowerQuery)
        }

        return searchResult.copy(
            songs = sortedSongs,
            playlists = sortedPlaylists,
            artists = sortedArtists,
            genres = sortedGenres,
            profiles = sortedProfiles
        )
    }

    /**
     * Calculates the relevance score for a playlist based on the query.
     * The score is determined by how closely the playlist title and description match the query.
     *
     * @param playlist The Playlist object to evaluate.
     * @param lowerQuery The lowercase version of the search query.
     * @return An integer score representing the relevance of the playlist to the query.
     */
    private fun calculatePlaylistRelevanceScore(playlist: Playlist, lowerQuery: String): Int {
        val title = playlist.title.lowercase()
        val description = playlist.description?.lowercase() ?: ""
        var score = 0

        if (title == lowerQuery) {
            score += 1000
        } else if (title.startsWith(lowerQuery)) {
            score += 800
        } else if (title.contains(" $lowerQuery")) {
            score += 600
        } else if (title.contains(lowerQuery)) {
            score += 400
        }
        if (description.isNotEmpty()) {
            if (description == lowerQuery) {
                score += 700
            } else if (description.startsWith(lowerQuery)) {
                score += 500
            } else if (description.contains(" $lowerQuery")) {
                score += 300
            } else if (description.contains(lowerQuery)) {
                score += 200
            }
        }
        return score
    }

    /**
     * Calculates the relevance score for an artist based on the query.
     * The score is determined by how closely the artist's name matches the query.
     *
     * @param artist The Artist object to evaluate.
     * @param lowerQuery The lowercase version of the search query.
     * @return An integer score representing the relevance of the artist to the query.
     */
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

    /**
     * Calculates the relevance score for a song based on the query.
     * The score is determined by how closely the song's name, artist, and album match the query.
     *
     * @param song The Song object to evaluate.
     * @param lowerQuery The lowercase version of the search query.
     * @return An integer score representing the relevance of the song to the query.
     */

    private fun calculateSongRelevanceScore(song: Song, lowerQuery: String): Int {
        val songName = song.name.lowercase()
        val artistName = song.artist?.lowercase() ?: ""
        val albumName = song.album?.lowercase() ?: ""

        var score = 0

        if (songName == lowerQuery) {
            score += 1000
        } else if (songName.startsWith(lowerQuery)) {
            score += 800
        } else if (songName.contains(" $lowerQuery")) {
            score += 600
        } else if (songName.contains(lowerQuery)) {
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