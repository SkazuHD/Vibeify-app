package de.hsb.vibeify.ui.search.searchbar

import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hsb.vibeify.services.SearchResult
import de.hsb.vibeify.services.SearchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchbarViewModel @Inject constructor(val searchService: SearchService) : ViewModel() {

    var searchResults = mutableStateOf(SearchResult())
        private set

    var recentSearches = searchService.recentSearchQueries

    var isLoading = MutableStateFlow(false)
        private set
    private val searchCache = LruCache<String, SearchResult>(50)

    fun onSearch(query: String) {
        viewModelScope.launch {
            searchCache.get(query)?.let { cachedResult ->
                searchResults.value = cachedResult
                Log.d("SearchbarViewModel", "Using cached search results for query: $query")
                return@launch
            }

            if (query.isBlank() || query.length < 2) {
                searchResults.value = SearchResult()
                Log.d("SearchbarViewModel", "Invalid search query: $query")
                return@launch
            }

            isLoading.value = true
            val result = searchService.search(query)
            searchResults.value = result
            isLoading.value = false

            searchCache.put(query, result)
            Log.d("SearchbarViewModel", "Search completed and cached for query: $query")
        }
    }

    fun clearSearchResults() {
        searchResults.value = SearchResult()
        Log.d("SearchbarViewModel", "Search results cleared")
    }
}