package de.hsb.vibeify.services

import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RandomSongService @Inject constructor(
    private val songRepository: SongRepository
) {
    private val _currentRandomSong = MutableStateFlow<Song?>(null)
    val currentRandomSong: StateFlow<Song?> = _currentRandomSong.asStateFlow()

    suspend fun refreshRandomSong(): Song? {
        val songs = songRepository.getRandomSongs(1)
        val randomSong = songs.firstOrNull()
        _currentRandomSong.value = randomSong
        return randomSong
    }

    fun getCurrentRandomSong(): Song? = _currentRandomSong.value
}