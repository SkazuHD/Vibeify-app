package de.hsb.vibeify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.ui.components.songCard.SmartSongCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SurpriseCard(
    modifier: Modifier = Modifier,
    autoLoad: Boolean = true,
    viewModel: SurpriseCardViewModel = hiltViewModel()
) {
    val randomSong by viewModel.randomSong.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(autoLoad) {
        if (autoLoad && randomSong == null && errorMessage == null && !isLoading) {
            viewModel.loadRandomSong()
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        when {
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                RerollButton(
                    onClick = { viewModel.loadRandomSong() },
                    modifier = Modifier.size(40.dp)
                )
            }

            isLoading -> {
                LoadingIndicator(
                    Modifier
                        .weight(0.8f)
                        .height(65.dp)
                )
                RerollButton(
                    onClick = { viewModel.loadRandomSong() },
                    modifier = Modifier.size(40.dp)
                )
            }

            randomSong != null -> {
                SmartSongCard(
                    song = randomSong!!,
                    showMenu = false,
                    modifier = Modifier.weight(0.8f),
                )
                RerollButton(
                    onClick = { viewModel.loadRandomSong() },
                    modifier = Modifier.size(40.dp)
                )
            }

            else -> {
                Text(
                    text = "Surprise me",
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center
                )
                RerollButton(
                    onClick = { viewModel.loadRandomSong() },
                    modifier = Modifier.size(40.dp)
                )
            }
        }

    }
}

@Composable
fun RerollButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Casino,
            contentDescription = "Re-roll",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(40.dp)
        )
    }
}