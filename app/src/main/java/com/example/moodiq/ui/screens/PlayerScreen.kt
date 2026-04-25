package com.example.moodiq.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun PlayerScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()
    val current = state.currentSong

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
        Text(current?.title ?: "No song", style = MaterialTheme.typography.headlineSmall)
        Text(current?.artist ?: "-")

        Slider(
            value = state.playbackPosition.toFloat(),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..(state.playbackDuration.takeIf { it > 0 } ?: 1L).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = viewModel::previous) { Icon(Icons.Default.SkipPrevious, null) }
            IconButton(onClick = viewModel::playPause) {
                Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
            }
            IconButton(onClick = viewModel::next) { Icon(Icons.Default.SkipNext, null) }
        }

        Text("Lyrics", modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.lyrics) { idx, line ->
                val color by animateColorAsState(
                    if (idx == state.highlightedLyric) MaterialTheme.colorScheme.primary else Color.LightGray,
                    label = "lyricHighlight"
                )
                Text(
                    text = line.content,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(if (idx == state.highlightedLyric) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                        .padding(8.dp)
                )
            }
        }
    }
}
