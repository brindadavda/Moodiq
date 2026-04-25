package com.example.moodiq.domain.model

data class ListeningInsight(
    val mostPlayed: List<SongStat>,
    val favoriteArtist: String,
    val timePattern: Map<Int, Int>,
    val recentlyPlayed: List<SongStat>,
    val moodInsight: String
)

data class SongStat(
    val songId: Long,
    val title: String,
    val artist: String,
    val playCount: Int
)

data class SmartPlaylist(
    val title: String,
    val songs: List<Song>
)
