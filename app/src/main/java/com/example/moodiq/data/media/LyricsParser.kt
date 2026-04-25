package com.example.moodiq.data.media

import android.media.MediaMetadataRetriever
import com.example.moodiq.domain.model.LyricLine
import java.io.File
import kotlin.math.max

class LyricsParser {
    private val pattern = Regex("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?](.*)")

    fun loadLyrics(songPath: String, title: String, artist: String, durationMs: Long): List<LyricLine> {
        val songFile = File(songPath)
        val lrcFile = File(songFile.parentFile, "${songFile.nameWithoutExtension}.lrc")
        if (lrcFile.exists()) return parseLrc(lrcFile)

        val txtFile = File(songFile.parentFile, "${songFile.nameWithoutExtension}.txt")
        if (txtFile.exists()) {
            return txtFile.readLines().mapIndexed { idx, text ->
                LyricLine(timestampMs = idx * 5_000L, content = text)
            }
        }

        extractEmbeddedLyrics(songPath)?.let { embedded ->
            val lines = embedded.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
            if (lines.isNotEmpty()) {
                return toTimedLyrics(lines, durationMs)
            }
        }

        return buildAutoKaraokeLines(title, artist, durationMs)
    }

    private fun parseLrc(file: File): List<LyricLine> {
        return file.readLines().mapNotNull { line ->
            val match = pattern.find(line) ?: return@mapNotNull null
            val minutes = match.groupValues[1].toLongOrNull() ?: 0L
            val seconds = match.groupValues[2].toLongOrNull() ?: 0L
            val millis = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
            val text = match.groupValues[4].ifBlank { "..." }
            LyricLine((minutes * 60_000) + (seconds * 1_000) + millis, text)
        }.sortedBy { it.timestampMs }
    }

    private fun extractEmbeddedLyrics(songPath: String): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(songPath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LYRIC)
        } catch (_: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun toTimedLyrics(lines: List<String>, durationMs: Long): List<LyricLine> {
        val safeDuration = max(durationMs, 20_000L)
        val step = (safeDuration / max(lines.size, 1)).coerceAtLeast(2_000L)
        return lines.mapIndexed { idx, line ->
            LyricLine(timestampMs = idx * step, content = line)
        }
    }

    private fun buildAutoKaraokeLines(title: String, artist: String, durationMs: Long): List<LyricLine> {
        val seed = listOf(
            title,
            "by $artist",
            "Feel the rhythm",
            "Let the melody flow",
            "Sing along"
        ).filter { it.isNotBlank() }

        val repeated = buildList {
            while (size < 12) {
                addAll(seed)
            }
        }.take(12)

        return toTimedLyrics(repeated, durationMs)
    }
}
