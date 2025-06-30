package de.hsb.vibeify.ui.search.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.hsb.vibeify.data.model.Genre
import de.hsb.vibeify.data.model.Playlist
import de.hsb.vibeify.data.model.Song
import de.hsb.vibeify.ui.components.LoadingIndicator
import de.hsb.vibeify.ui.components.songCard.SmartSongCard
import de.hsb.vibeify.ui.components.songCard.TrendingSongCard
import kotlin.random.Random

@Composable
fun DiscoverySection(
    modifier: Modifier = Modifier,
    discoveryViewModel: DiscoveryViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onGenreClick: (String) -> Unit = {}
) {
    val trendingSongs by discoveryViewModel.trendingSongs
    val featuredPlaylists by discoveryViewModel.featuredPlaylists
    val randomSongs by discoveryViewModel.randomSongs

    val availableGenres by discoveryViewModel.availableGenres.collectAsState()

    val isLoading by discoveryViewModel.isLoading

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discovery",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { discoveryViewModel.refreshContent() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Aktualisieren"
                        )
                    }
                }
            }

            if (trendingSongs.isNotEmpty()) {
                item {
                    SectionHeader(title = "🔥 Trending Songs")
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trendingSongs) { song ->
                            TrendingSongCard(
                                song = song,
                                onClick = { onSongClick(song) }
                            )
                        }
                    }
                }
            }

            if (availableGenres.isNotEmpty()) {
                item {
                    SectionHeader(title = "🎵 Browse Genres")
                }
                item {
                    GenreGrid(
                        genres = availableGenres,
                        onGenreClick = onGenreClick
                    )
                }
            }

            if (featuredPlaylists.isNotEmpty()) {
                item {
                    SectionHeader(title = "⭐ Recommended Playlists")
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredPlaylists) { playlist ->
                            PlaylistDiscoveryCard(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) }
                            )
                        }
                    }
                }
            }

            if (randomSongs.isNotEmpty()) {
                item {
                    SectionHeader(title = "🎲 Surprise Selection")
                }
                items(randomSongs) { song ->
                    SmartSongCard(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}


@Composable
private fun GenreGrid(
    genres: List<Genre>,
    onGenreClick: (String) -> Unit
) {
    Column {
        var showAllGenres by rememberSaveable { mutableStateOf(false) }
        val resultLimit = 4

        val chunkedGenres = genres.chunked(2)
        chunkedGenres.take(if (showAllGenres) Int.MAX_VALUE else resultLimit).forEach { genreRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genreRow.forEach { genre ->
                    GenreChip(
                        genre = genre.name,
                        count = genre.count ?: 0,
                        onClick = { onGenreClick(genre.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (genreRow.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (genres.size > resultLimit) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (showAllGenres) "Show Less" else "Show All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { showAllGenres = !showAllGenres }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun GenreChip(
    genre: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .height(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$genre (${count})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PlaylistDiscoveryCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Placeholder für Playlist Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Color.hsv(Random.nextFloat() * 360f, 0.4f, 0.8f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎶",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
