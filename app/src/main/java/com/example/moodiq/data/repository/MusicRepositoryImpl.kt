package com.example.moodiq.data.repository

import com.example.moodiq.data.local.dao.FavoriteDao
import com.example.moodiq.data.local.dao.PlayHistoryDao
import com.example.moodiq.data.local.dao.SongDao
import com.example.moodiq.data.local.entity.FavoriteSongEntity
import com.example.moodiq.data.local.entity.PlayHistoryEntity
import com.example.moodiq.data.local.entity.SongEntity
import com.example.moodiq.data.media.LyricsParser
import com.example.moodiq.data.media.MediaStoreDataSource
import com.example.moodiq.domain.model.ListeningInsight
import com.example.moodiq.domain.model.LyricLine
import com.example.moodiq.domain.model.RecommendationResult
import com.example.moodiq.domain.model.SmartPlaylist
import com.example.moodiq.domain.model.Song
import com.example.moodiq.domain.model.SongStat
import com.example.moodiq.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class MusicRepositoryImpl(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val lyricsParser: LyricsParser,
    private val songDao: SongDao,
    private val favoriteDao: FavoriteDao,
    private val playHistoryDao: PlayHistoryDao
) : MusicRepository {

    override suspend fun scanLocalSongs(): List<Song> {
        val songs = mediaStoreDataSource.fetchSongs()
        songDao.upsertAll(songs.map {
            SongEntity(
                id = it.id,
                title = it.title,
                artist = it.artist,
                album = it.album,
                duration = it.duration,
                path = it.path,
                albumId = it.albumId,
                lastScannedAt = System.currentTimeMillis()
            )
        })
        return songs
    }

    override fun observeSongs(): Flow<List<Song>> = songDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun observeFavorites(): Flow<List<Song>> = combine(
        songDao.observeAll(),
        favoriteDao.observeFavoriteIds()
    ) { songs, favoriteIds ->
        val favoriteSet = favoriteIds.toSet()
        songs.filter { favoriteSet.contains(it.id) }.map { it.toDomain() }
    }

    override suspend fun toggleFavorite(songId: Long) {
        if (favoriteDao.isFavorite(songId)) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteSongEntity(songId = songId))
        }
    }

    override suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)

    override fun loadLyrics(song: Song): List<LyricLine> = lyricsParser.loadLyrics(song.path)

    override suspend fun trackPlay(songId: Long, durationPlayed: Long, skipped: Boolean) {
        playHistoryDao.insert(
            PlayHistoryEntity(
                songId = songId,
                timestamp = System.currentTimeMillis(),
                durationPlayed = durationPlayed,
                skipped = skipped
            )
        )
    }

    override suspend fun getRecommendations(): RecommendationResult {
        val songs = songDao.getAll().map { it.toDomain() }
        if (songs.isEmpty()) return RecommendationResult(emptyList(), emptyList(), "No Data")

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val nightMode = hour >= 21 || hour <= 5
        val favorites = favoriteDao.getFavoriteIds().toSet()

        val sorted = songs.sortedByDescending { song ->
            val skipRate = playHistoryDao.skipRate(song.id) ?: 0.0
            val favoriteBoost = if (favorites.contains(song.id)) 2.0 else 0.0
            val nightBoost = if (nightMode && song.title.contains("night", true)) 1.5 else 0.0
            favoriteBoost + nightBoost - skipRate
        }

        val moodTag = when {
            nightMode -> "Chill Night"
            sorted.firstOrNull()?.artist?.contains("lofi", true) == true -> "Focus"
            else -> "Everyday Mix"
        }

        return RecommendationResult(
            smartQueue = sorted.take(20),
            suggestions = sorted.drop(5).take(10),
            moodTag = moodTag
        )
    }

    override suspend fun getInsights(): ListeningInsight {
        val mostPlayed = playHistoryDao.mostPlayed(10)
        val songsMap = songDao.getByIds(mostPlayed.map { it.songId }).associateBy { it.id }
        val mostPlayedStats = mostPlayed.mapNotNull { item ->
            songsMap[item.songId]?.let { song -> SongStat(song.id, song.title, song.artist, item.count) }
        }

        val favoriteArtist = playHistoryDao.favoriteArtist()?.artist ?: "Unknown"
        val byHour = playHistoryDao.byHour().associate { (it.hour.toIntOrNull() ?: 0) to it.count }

        val recent = playHistoryDao.recent(10)
        val recentSongs = songDao.getByIds(recent.map { it.songId }).associateBy { it.id }
        val recentStats = recent.mapNotNull { entry ->
            recentSongs[entry.songId]?.let { SongStat(it.id, it.title, it.artist, 1) }
        }

        val moodInsight = when {
            byHour.filterKeys { it >= 21 || it <= 5 }.values.sum() > 5 -> "You enjoy calm night sessions."
            (mostPlayedStats.firstOrNull()?.playCount ?: 0) > 8 -> "You love replaying your top hits."
            else -> "Balanced listening across the day."
        }

        return ListeningInsight(
            mostPlayed = mostPlayedStats,
            favoriteArtist = favoriteArtist,
            timePattern = byHour,
            recentlyPlayed = recentStats,
            moodInsight = moodInsight
        )
    }

    override suspend fun getDynamicPlaylists(): List<SmartPlaylist> {
        val songs = songDao.getAll().map { it.toDomain() }
        val favoriteIds = favoriteDao.getFavoriteIds().toSet()
        val recommendations = getRecommendations()
        val insights = getInsights()
        val recent = playHistoryDao.recent(20).map { it.songId }
        val mostPlayedSongs = insights.mostPlayed.mapNotNull { stat -> songs.find { it.id == stat.songId } }

        return listOf(
            SmartPlaylist("Favorites", songs.filter { favoriteIds.contains(it.id) }),
            SmartPlaylist("Most Played", mostPlayedSongs),
            SmartPlaylist("Recently Played", recent.mapNotNull { id -> songs.find { it.id == id } }.distinctBy { it.id }),
            SmartPlaylist("Night Chill", recommendations.smartQueue.take(20))
        )
    }

    private fun SongEntity.toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        path = path,
        albumId = albumId
    )
}
