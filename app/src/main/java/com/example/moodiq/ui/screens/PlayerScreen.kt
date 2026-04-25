package com.example.moodiq.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moodiq.ui.theme.Accent
import com.example.moodiq.ui.theme.AccentSecondary
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun PlayerScreen(viewModel: MainViewModel, paddingValues: PaddingValues) {
    val state by viewModel.uiState.collectAsState()
    val current = state.currentSong

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(listOf(Accent.copy(alpha = 0.45f), AccentSecondary.copy(alpha = 0.28f))),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(48.dp))
                    }
                }

                Text(current?.title ?: "No song selected", style = MaterialTheme.typography.headlineSmall)
                Text(
                    current?.artist ?: "Start playback from library",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )

                Slider(
                    value = state.playbackPosition.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..(state.playbackDuration.takeIf { it > 0 } ?: 1L).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::previous) { Icon(Icons.Default.SkipPrevious, null) }
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = viewModel::playPause) {
                            Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                        }
                    }
                    IconButton(onClick = viewModel::next) { Icon(Icons.Default.SkipNext, null) }
                }
            }
        }

        Text(
            "Lyrics",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        if (state.isGeneratingLyrics) {
            Text(
                "Generating lyrics from audio…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else if (state.lyrics.isEmpty() && state.currentSong != null && state.isPlaying) {
            Text(
                if (state.recordPermissionGranted) {
                    "Listening for vocals… make sure audio is audible for transcription."
                } else {
                    "Microphone permission is needed to generate live lyrics."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.lyrics) { idx, line ->
                val isCurrent = idx == state.highlightedLyric
                val color by animateColorAsState(
                    if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    label = "lyricHighlight"
                )
                Text(
                    text = line.content,
                    color = color,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}
