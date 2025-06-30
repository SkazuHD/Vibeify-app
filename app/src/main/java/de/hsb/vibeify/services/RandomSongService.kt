package de.hsb.vibeify.services

import de.hsb.vibeify.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RandomSongService @Inject constructor(
    private val discoveryService: DiscoveryService
) {
    private val _currentRandomSong = MutableStateFlow<Song?>(null)
    val currentRandomSong: StateFlow<Song?> = _currentRandomSong.asStateFlow()

    suspend fun refreshRandomSong(): Song? {
        val randomSong = discoveryService.generateRandomSongs(1).firstOrNull()
        _currentRandomSong.value = randomSong
        return randomSong
    }
}