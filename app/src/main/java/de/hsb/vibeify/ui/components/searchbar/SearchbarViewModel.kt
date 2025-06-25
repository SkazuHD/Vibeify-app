package de.hsb.vibeify.ui.components.searchbar

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.SearchResult
import de.hsb.vibeify.services.SearchService
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchbarViewModel @Inject constructor(val searchService: SearchService) : ViewModel() {

    var searchResults = SearchResult()
        private set
    val tempSearchResultStrings: SnapshotStateList<String> = mutableStateListOf()

    fun onSearch(query: String) {
        viewModelScope.launch {
            searchResults = searchService.search(query)
            Log.d("AppHeaderViewModel", "Search results: $searchResults")

            tempSearchResultStrings.clear()

            searchResults.playlists.let { results ->
                tempSearchResultStrings.addAll(results.map { it.title })
            }
            searchResults.songs.let { results ->
                tempSearchResultStrings.addAll(results.map { it.name })
            }
            Log.d(
                "AppHeaderViewModel",
                "Temp search result strings: $tempSearchResultStrings"
            )
        }
    }
}