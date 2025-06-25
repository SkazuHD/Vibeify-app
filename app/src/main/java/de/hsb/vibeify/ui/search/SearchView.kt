package de.hsb.vibeify.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.ui.components.searchbar.SearchbarViewModel
import de.hsb.vibeify.ui.components.searchbar.SimpleSearchBar

@Composable
fun SearchView(
    modifier: Modifier = Modifier,
    vm: SearchbarViewModel = hiltViewModel(),
) {
    val textFieldState = remember { TextFieldState("") }
    var searchResults = vm.tempSearchResultStrings

    Box(modifier = modifier) {
        Column {
            SimpleSearchBar(
                textFieldState = textFieldState,
                onSearch = { query ->
                    vm.onSearch(query)
                },
                searchResults = searchResults,
            )
        }
    }
}