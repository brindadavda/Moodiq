package com.example.moodiq.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moodiq.ui.components.SongCard
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        item {
            Text("AI Mood: ${state.recommendations.moodTag}", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(modifier = Modifier.padding(vertical = 10.dp)) {
                Text(
                    text = "Smart Queue: ${state.recommendations.smartQueue.size} songs",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item { Text("Recently Added", style = MaterialTheme.typography.titleLarge) }
        items(state.songs.take(8), key = { it.id }) { song ->
            SongCard(
                song = song,
                favorite = viewModel.isFavorite(song.id),
                onClick = { viewModel.playSong(song) },
                onFavoriteClick = { viewModel.toggleFavorite(song) }
            )
        }
    }
}
