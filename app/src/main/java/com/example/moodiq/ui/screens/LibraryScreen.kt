package com.example.moodiq.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moodiq.ui.components.SongCard
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun LibraryScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val filtered = state.songs.filter {
        it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        item {
            Text("Library")
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.padding(vertical = 10.dp),
                label = { Text("Search songs") }
            )
        }
        items(filtered, key = { it.id }) { song ->
            SongCard(
                song = song,
                favorite = viewModel.isFavorite(song.id),
                onClick = { viewModel.playSong(song) },
                onFavoriteClick = { viewModel.toggleFavorite(song) }
            )
        }
    }
}
