package com.example.moodiq.domain.usecase

import com.example.moodiq.domain.repository.MusicRepository

class ScanMusicUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke() = repository.scanLocalSongs()
}

class ObserveSongsUseCase(private val repository: MusicRepository) {
    operator fun invoke() = repository.observeSongs()
}

class ObserveFavoritesUseCase(private val repository: MusicRepository) {
    operator fun invoke() = repository.observeFavorites()
}

class ToggleFavoriteUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(songId: Long) = repository.toggleFavorite(songId)
}

class GetRecommendationsUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke() = repository.getRecommendations()
}

class GetInsightsUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke() = repository.getInsights()
}

class GetDynamicPlaylistsUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke() = repository.getDynamicPlaylists()
}
