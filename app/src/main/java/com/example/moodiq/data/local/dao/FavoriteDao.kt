package com.example.moodiq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.moodiq.data.local.entity.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    @Query("SELECT songId FROM favorite_songs ORDER BY favoritedAt DESC")
    fun observeFavoriteIds(): Flow<List<Long>>

    @Query("SELECT songId FROM favorite_songs ORDER BY favoritedAt DESC")
    suspend fun getFavoriteIds(): List<Long>
}
