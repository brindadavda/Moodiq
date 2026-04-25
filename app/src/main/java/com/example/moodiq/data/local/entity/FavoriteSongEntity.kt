package com.example.moodiq.data.local.entity

import androidx.room.Entity

@Entity(tableName = "favorite_songs", primaryKeys = ["songId"])
data class FavoriteSongEntity(
    val songId: Long,
    val favoritedAt: Long = System.currentTimeMillis()
)
