package com.example.moodiq.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun FavoritesScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text(
                text = "Favorites (${state.favorites.size})",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        items(state.favorites, key = { it.id }) { song ->
            SongCard(
                song = song,
                favorite = true,
                onClick = { viewModel.playSong(song) },
                onFavoriteClick = { viewModel.toggleFavorite(song) }
            )
        }
    }
}
