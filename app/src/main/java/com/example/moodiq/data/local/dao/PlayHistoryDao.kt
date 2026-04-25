package com.example.moodiq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.moodiq.data.local.entity.PlayHistoryEntity

@Dao
interface PlayHistoryDao {
    @Insert
    suspend fun insert(entry: PlayHistoryEntity)

    @Query("SELECT * FROM play_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<PlayHistoryEntity>

    @Query("SELECT songId, COUNT(*) as count FROM play_history GROUP BY songId ORDER BY count DESC LIMIT :limit")
    suspend fun mostPlayed(limit: Int): List<SongPlayCount>

    @Query("SELECT artist, SUM(play_count) as total_count FROM (SELECT songs.artist as artist, COUNT(*) as play_count FROM play_history INNER JOIN songs ON play_history.songId = songs.id GROUP BY songId) GROUP BY artist ORDER BY total_count DESC LIMIT 1")
    suspend fun favoriteArtist(): ArtistAggregate?

    @Query("SELECT strftime('%H', datetime(timestamp / 1000, 'unixepoch')) as hour, COUNT(*) as count FROM play_history GROUP BY hour")
    suspend fun byHour(): List<HourAggregate>

    @Query("SELECT AVG(CASE WHEN skipped THEN 1.0 ELSE 0 END) FROM play_history WHERE songId = :songId")
    suspend fun skipRate(songId: Long): Double?
}

data class SongPlayCount(
    val songId: Long,
    val count: Int
)

data class ArtistAggregate(
    val artist: String,
    val total_count: Int
)

data class HourAggregate(
    val hour: String,
    val count: Int
)
