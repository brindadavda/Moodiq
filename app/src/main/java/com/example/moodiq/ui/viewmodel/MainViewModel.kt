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
        val permissionGranted: Boolean = false,
        val recordPermissionGranted: Boolean = false,
        val isGeneratingLyrics: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    val playerController: PlayerController = appContainer.playerController

    init {
        observeData()
        observePlayer()
    }

    fun setPermissionsGranted(musicGranted: Boolean, recordGranted: Boolean) {
        _uiState.update {
            it.copy(
                permissionGranted = musicGranted,
                recordPermissionGranted = recordGranted
            )
        }
        if (musicGranted) refresh()
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
                stopLiveLyricsGeneration()
                if (song != null) {
                    val lyrics = appContainer.musicRepository.loadLyrics(song)
                    _uiState.update { it.copy(lyrics = lyrics, highlightedLyric = 0) }
                    if (_uiState.value.isPlaying) {
                        maybeStartLiveLyricsGeneration()
                    }
                }
            }
        }
        viewModelScope.launch {
            playerController.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlaying = playing) }
                if (playing) {
                    startTicker()
                    maybeStartLiveLyricsGeneration()
                } else {
                    tickerJob?.cancel()
                    stopLiveLyricsGeneration()
                }
            }
        }
    }

    private fun maybeStartLiveLyricsGeneration() {
        val state = _uiState.value
        if (state.currentSong == null || state.lyrics.isNotEmpty()) return
        if (!state.recordPermissionGranted || !appContainer.audioToTextLyricsGenerator.isAvailable()) return
        if (state.isGeneratingLyrics) return

        _uiState.update { it.copy(isGeneratingLyrics = true) }
        appContainer.audioToTextLyricsGenerator.start { transcript ->
            val position = playerController.position()
            _uiState.update { current ->
                val shouldAppend = current.lyrics.lastOrNull()?.content?.equals(transcript, ignoreCase = true) != true
                if (!shouldAppend) return@update current
                current.copy(
                    lyrics = current.lyrics + LyricLine(
                        timestampMs = position,
                        content = transcript
                    )
                )
            }
        }
    }

    private fun stopLiveLyricsGeneration() {
        appContainer.audioToTextLyricsGenerator.stop()
        _uiState.update { it.copy(isGeneratingLyrics = false) }
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

    fun playPause() = playerController.playPause()
    fun next() = playerController.next()
    fun previous() = playerController.previous()
    fun seekTo(position: Long) = playerController.seekTo(position)

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
        stopLiveLyricsGeneration()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(container) as T
        }
    }
}
