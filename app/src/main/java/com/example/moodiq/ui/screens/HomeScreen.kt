package com.example.moodiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.moodiq.ui.components.SongCard
import com.example.moodiq.ui.theme.Accent
import com.example.moodiq.ui.theme.AccentSecondary
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Moodiq", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("For You", style = MaterialTheme.typography.headlineSmall)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Accent.copy(alpha = 0.38f), AccentSecondary.copy(alpha = 0.28f))))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("AI Mix", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            text = "Mood: ${state.recommendations.moodTag}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                        )
                        Text(
                            text = "${state.recommendations.smartQueue.size} tracks curated for this moment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                            }
                            Text("Play Smart Queue", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        item {
            Text("Recently Added", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp))
        }

        items(state.songs.take(10), key = { it.id }) { song ->
            SongCard(
                song = song,
                favorite = viewModel.isFavorite(song.id),
                onClick = { viewModel.playSong(song) },
                onFavoriteClick = { viewModel.toggleFavorite(song) }
            )
        }
    }
}
