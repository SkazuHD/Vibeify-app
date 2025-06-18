package de.hsb.vibeify.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.core.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val textFieldState = remember { TextFieldState("Search") }
    var searchResults by remember { mutableStateOf(listOf("Result 1", "Result 2", "Result 3")) }

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
                    println(query)
                },
                searchResults = searchResults,
            )
            // Avatar
            Box(modifier.size(50.dp)
                .clickable {
                    authViewModel.signOut()
                }, contentAlignment = Alignment.Center) {
                val color = MaterialTheme.colorScheme.primary
                val initials = ("J" + "L").uppercase()
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(SolidColor(color))
                }
                Text(text = initials, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }



}