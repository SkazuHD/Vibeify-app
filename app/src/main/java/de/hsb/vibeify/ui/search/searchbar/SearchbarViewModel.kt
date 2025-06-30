package de.hsb.vibeify.ui.search.searchbar

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.SearchResult
import de.hsb.vibeify.services.SearchService
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchbarViewModel @Inject constructor(val searchService: SearchService) : ViewModel() {

    var searchResults = mutableStateOf(SearchResult())
        private set

    fun onSearch(query: String) {
        viewModelScope.launch {
            if (
                query.isBlank() || query.length < 2
            ) {
                searchResults.value = SearchResult()
                Log.d("AppHeaderViewModel", "Invalid search query: $query")
                return@launch
            }
            searchResults.value = searchService.search(query)
            Log.d("AppHeaderViewModel", "Search results: $searchResults")

        }
    }

    fun clearSearchResults() {
        searchResults.value = SearchResult()
        Log.d("AppHeaderViewModel", "Search results cleared")
    }
}