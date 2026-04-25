package com.example.moodiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moodiq.core.AppContainer
import com.example.moodiq.domain.model.ListeningInsight
import com.example.moodiq.domain.model.LyricLine
import com.example.moodiq.domain.model.RecommendationResult
import com.example.moodiq.domain.model.SmartPlaylist
import com.example.moodiq.domain.model.Song
import com.example.moodiq.player.PlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    data class UiState(
        val songs: List<Song> = emptyList(),
        val favorites: List<Song> = emptyList(),
        val recommendations: RecommendationResult = RecommendationResult(emptyList(), emptyList(), "Loading"),
        val playlists: List<SmartPlaylist> = emptyList(),
        val insights: ListeningInsight? = null,
        val currentSong: Song? = null,
        val isPlaying: Boolean = false,
        val lyrics: List<LyricLine> = emptyList(),
        val highlightedLyric: Int = 0,
        val playbackPosition: Long = 0L,
        val playbackDuration: Long = 0L,
        val permissionGranted: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    val playerController: PlayerController = appContainer.playerController

    init {
        observeData()
        observePlayer()
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
        if (granted) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            appContainer.scanMusicUseCase()
            val recommendations = appContainer.getRecommendationsUseCase()
            val insights = appContainer.getInsightsUseCase()
            val playlists = appContainer.getDynamicPlaylistsUseCase()
            _uiState.update {
                it.copy(
                    recommendations = recommendations,
                    insights = insights,
                    playlists = playlists
                )
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            appContainer.observeSongsUseCase().collect { songs ->
                _uiState.update { it.copy(songs = songs) }
            }
        }
        viewModelScope.launch {
            appContainer.observeFavoritesUseCase().collect { favorites ->
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }

    private fun observePlayer() {
        viewModelScope.launch {
            playerController.currentSong.collect { song ->
                _uiState.update { it.copy(currentSong = song) }
                if (song != null) {
                    val lyrics = appContainer.musicRepository.loadLyrics(song)
                    _uiState.update { it.copy(lyrics = lyrics, highlightedLyric = 0) }
                }
            }
        }
        viewModelScope.launch {
            playerController.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlaying = playing) }
                if (playing) startTicker() else tickerJob?.cancel()
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val position = playerController.position()
                val duration = playerController.duration()
                val index = _uiState.value.lyrics.indexOfLast { it.timestampMs <= position }.coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        playbackPosition = position,
                        playbackDuration = duration,
                        highlightedLyric = index
                    )
                }
                delay(250)
            }
        }
    }

    fun playSong(song: Song) {
        val queue = _uiState.value.songs
        val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerController.setQueue(queue, index)
        playerController.play()
    }

    fun playPause() {
        ensureQueueInitialized()
        playerController.playPause()
    }

    fun next() {
        ensureQueueInitialized()
        playerController.next()
    }

    fun previous() {
        ensureQueueInitialized()
        playerController.previous()
    }

    fun seekTo(position: Long) {
        ensureQueueInitialized()
        playerController.seekTo(position)
    }

    private fun ensureQueueInitialized() {
        val queue = _uiState.value.songs
        if (queue.isEmpty()) return

        val currentSong = _uiState.value.currentSong
        val missingQueueInPlayer = !playerController.hasMediaItems()

        if (currentSong != null && !missingQueueInPlayer) return

        val startIndex = currentSong
            ?.let { song -> queue.indexOfFirst { it.id == song.id } }
            ?.takeIf { it >= 0 }
            ?: 0

        playerController.setQueue(queue, startIndex = startIndex)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            appContainer.toggleFavoriteUseCase(song.id)
        }
    }

    fun isFavorite(songId: Long): Boolean = _uiState.value.favorites.any { it.id == songId }

    fun trackCurrentSong(skipped: Boolean = false) {
        val song = _uiState.value.currentSong ?: return
        viewModelScope.launch {
            appContainer.musicRepository.trackPlay(song.id, _uiState.value.playbackPosition, skipped)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(container) as T
        }
    }
}
