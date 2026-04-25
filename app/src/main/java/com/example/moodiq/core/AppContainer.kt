package com.example.moodiq.core

import android.content.Context
import androidx.room.Room
import com.example.moodiq.data.local.MoodiqDatabase
import com.example.moodiq.data.media.LyricsParser
import com.example.moodiq.data.media.MediaStoreDataSource
import com.example.moodiq.data.repository.MusicRepositoryImpl
import com.example.moodiq.domain.repository.MusicRepository
import com.example.moodiq.domain.usecase.GetDynamicPlaylistsUseCase
import com.example.moodiq.domain.usecase.GetInsightsUseCase
import com.example.moodiq.domain.usecase.GetRecommendationsUseCase
import com.example.moodiq.domain.usecase.ObserveFavoritesUseCase
import com.example.moodiq.domain.usecase.ObserveSongsUseCase
import com.example.moodiq.domain.usecase.ScanMusicUseCase
import com.example.moodiq.domain.usecase.ToggleFavoriteUseCase
import com.example.moodiq.player.PlayerController

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val db = Room.databaseBuilder(
        appContext,
        MoodiqDatabase::class.java,
        "moodiq.db"
    ).fallbackToDestructiveMigration().build()

    val playerController = PlayerController(appContext)
    private val repository: MusicRepository = MusicRepositoryImpl(
        mediaStoreDataSource = MediaStoreDataSource(appContext),
        lyricsParser = LyricsParser(),
        songDao = db.songDao(),
        favoriteDao = db.favoriteDao(),
        playHistoryDao = db.playHistoryDao()
    )

    val scanMusicUseCase = ScanMusicUseCase(repository)
    val observeSongsUseCase = ObserveSongsUseCase(repository)
    val observeFavoritesUseCase = ObserveFavoritesUseCase(repository)
    val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)
    val getRecommendationsUseCase = GetRecommendationsUseCase(repository)
    val getInsightsUseCase = GetInsightsUseCase(repository)
    val getDynamicPlaylistsUseCase = GetDynamicPlaylistsUseCase(repository)
    val musicRepository: MusicRepository = repository
}
