package com.example.moodiq.domain.repository

import com.example.moodiq.domain.model.LyricLine
import com.example.moodiq.domain.model.ListeningInsight
import com.example.moodiq.domain.model.RecommendationResult
import com.example.moodiq.domain.model.SmartPlaylist
import com.example.moodiq.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun scanLocalSongs(): List<Song>
    fun observeSongs(): Flow<List<Song>>
    fun observeFavorites(): Flow<List<Song>>
    suspend fun toggleFavorite(songId: Long)
    suspend fun isFavorite(songId: Long): Boolean
    suspend fun loadLyrics(song: Song): List<LyricLine>
    suspend fun trackPlay(songId: Long, durationPlayed: Long, skipped: Boolean)
    suspend fun getRecommendations(): RecommendationResult
    suspend fun getInsights(): ListeningInsight
    suspend fun getDynamicPlaylists(): List<SmartPlaylist>
}
