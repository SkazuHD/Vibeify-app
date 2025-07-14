package de.hsb.vibeify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays a card with a title and an optional icon.
 *
 * @param title The title to be displayed in the card.
 * @param modifier Modifier to be applied to the card.
 * @param icon A composable function that provides an icon to be displayed in the card.
 */

@Composable
fun NoContentCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { }
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterVertically
            )
        )
        {
            icon()
            Text(
                text = title,
            )
        }

    }
}