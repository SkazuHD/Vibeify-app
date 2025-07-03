package de.hsb.vibeify.data.repository

import de.hsb.vibeify.data.model.Artist
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


interface ArtistRepository {
    suspend fun getArtistById(id: String): Artist?
    suspend fun getAllArtists(): List<Artist>
    suspend fun searchArtists(query: String): List<Artist>
}

@Singleton
class ArtistRepositoryImpl @Inject constructor(
    private val songRepository: SongRepository
) : ArtistRepository {

    private var cachedArtists: List<Artist>? = null

    private suspend fun getArtistsFromSongs(): List<Artist> {
        if (cachedArtists != null) {
            return cachedArtists!!
        }

        val allSongs = songRepository.getAllSongs()

        val artistMap = allSongs
            .filter { !it.artist.isNullOrBlank() }
            .groupBy { it.artist!! }
            .map { (artistName, songs) ->
                val songIds = songs.map { it.id }
                val albumIds = songs.mapNotNull { it.album }.distinct()
                val imageUrl = songs.firstNotNullOfOrNull { it.imageUrl }

                Artist(
                    id = generateArtistId(artistName),
                    name = artistName,
                    imageUrl = imageUrl,
                    songIds = songIds,
                    albumIds = albumIds
                )
            }

        cachedArtists = artistMap

        return artistMap
    }

    private fun generateArtistId(artistName: String): String {
        return "artist_${artistName.lowercase().replace(" ", "_")}_${
            UUID.nameUUIDFromBytes(
                artistName.toByteArray()
            ).toString().take(8)
        }"
    }

    override suspend fun getArtistById(id: String): Artist? {
        val artists = getArtistsFromSongs()
        return artists.find { it.id == id }
    }

    override suspend fun getAllArtists(): List<Artist> {
        return getArtistsFromSongs()
    }

    override suspend fun searchArtists(query: String): List<Artist> {
        val artists = getArtistsFromSongs()
        return artists.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }


}