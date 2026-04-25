package com.example.moodiq.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text("Listening Insights", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Favorite artist: ${insight?.favoriteArtist ?: "Unknown"}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Mood insight: ${insight?.moodInsight ?: "No data yet"}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        item {
            Text("Most Played", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp))
        }
        items(insight?.mostPlayed ?: emptyList(), key = { it.songId }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "${it.title} • ${it.playCount} plays",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
