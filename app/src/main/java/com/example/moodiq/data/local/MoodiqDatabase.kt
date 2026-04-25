package com.example.moodiq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moodiq.data.local.dao.FavoriteDao
import com.example.moodiq.data.local.dao.MoodTagDao
import com.example.moodiq.data.local.dao.PlayHistoryDao
import com.example.moodiq.data.local.dao.SongDao
import com.example.moodiq.data.local.entity.FavoriteSongEntity
import com.example.moodiq.data.local.entity.MoodTagEntity
import com.example.moodiq.data.local.entity.PlayHistoryEntity
import com.example.moodiq.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class, FavoriteSongEntity::class, PlayHistoryEntity::class, MoodTagEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MoodiqDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun moodTagDao(): MoodTagDao
}
