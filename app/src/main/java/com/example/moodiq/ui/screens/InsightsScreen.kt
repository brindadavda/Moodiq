package com.example.moodiq.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun InsightsScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()
    val insight = state.insights

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        item {
            Text("Listening Insights")
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Favorite artist: ${insight?.favoriteArtist ?: "Unknown"}", modifier = Modifier.padding(16.dp))
            }
            Card(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Mood insight: ${insight?.moodInsight ?: "No data yet"}", modifier = Modifier.padding(16.dp))
            }
            Text("Most Played")
        }
        items(insight?.mostPlayed ?: emptyList(), key = { it.songId }) {
            Card(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("${it.title} • ${it.playCount} plays", modifier = Modifier.padding(12.dp))
            }
        }
    }
}
