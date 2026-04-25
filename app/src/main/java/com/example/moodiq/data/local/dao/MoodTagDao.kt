package com.example.moodiq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodiq.data.local.entity.MoodTagEntity

@Dao
interface MoodTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: MoodTagEntity)

    @Query("SELECT * FROM mood_tags WHERE songId = :songId")
    suspend fun bySong(songId: Long): List<MoodTagEntity>
}
