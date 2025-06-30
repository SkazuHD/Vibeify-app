package de.hsb.vibeify.ui.search.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.data.model.Genre
import de.hsb.vibeify.services.DiscoveryService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val discoveryService: DiscoveryService,
) : ViewModel() {

    var trendingSongs = discoveryService.trendingSongs

    var featuredPlaylists = discoveryService.featuredPlaylists

    var randomSongs = discoveryService.randomSongs

    val availableGenres: StateFlow<List<Genre>> =
        discoveryService.genreList.map { list -> list.sortedByDescending { it.count } }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var isLoading = discoveryService.isLoading
        private set


    fun refreshContent() {
        viewModelScope.launch {
            discoveryService.refreshContent()
        }
    }
}