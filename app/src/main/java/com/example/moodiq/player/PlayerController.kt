package com.example.moodiq.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.moodiq.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerController(context: Context) {
    private val exoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = Player.REPEAT_MODE_OFF
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private var queue: List<Song> = emptyList()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songId = mediaItem?.mediaId?.toLongOrNull() ?: return
                _currentSong.value = queue.find { it.id == songId }
            }
        })
    }

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        queue = songs
        exoPlayer.setMediaItems(songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(Uri.parse(song.path))
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build()
                )
                .build()
        }, startIndex, 0L)
        exoPlayer.prepare()
        _currentSong.value = songs.getOrNull(startIndex)
    }

    fun play() = exoPlayer.play()
    fun pause() = exoPlayer.pause()
    fun playPause() = if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    fun next() = exoPlayer.seekToNextMediaItem()
    fun previous() = exoPlayer.seekToPreviousMediaItem()
    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    fun duration(): Long = exoPlayer.duration.coerceAtLeast(0L)
    fun position(): Long = exoPlayer.currentPosition
    fun hasMediaItems(): Boolean = exoPlayer.mediaItemCount > 0

    fun release() = exoPlayer.release()
    fun player(): ExoPlayer = exoPlayer
}
