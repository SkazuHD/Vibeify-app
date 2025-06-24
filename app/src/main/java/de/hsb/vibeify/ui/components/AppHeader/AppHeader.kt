package de.hsb.vibeify.ui.components.AppHeader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.core.AuthViewModel
import de.hsb.vibeify.ui.components.Avatar
import de.hsb.vibeify.ui.components.SimpleSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    vm: AppHeaderViewModel = hiltViewModel()

) {
    val textFieldState = remember { TextFieldState("") }
    var searchResults = vm.tempSearchResultStrings

    val elevation = if (scrollBehavior.state.overlappedFraction > 0f) 4.dp else 0.dp
    Surface(
        tonalElevation = elevation,
        modifier = modifier.wrapContentHeight()
    ) {

        Row (
            modifier = Modifier
                .fillMaxWidth().padding(
                    horizontal = 4.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ){
            SimpleSearchBar(
                textFieldState = textFieldState,
                onSearch = { query ->
                    vm.onSearch(query)
                },
                searchResults = searchResults,
            )
            // Avatar
            Avatar(
                initials = "JL",
                onClick = { authViewModel.signOut() }
            )
        }
    }



}