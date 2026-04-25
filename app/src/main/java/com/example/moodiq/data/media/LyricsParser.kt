package com.example.moodiq.data.media

import com.example.moodiq.domain.model.LyricLine
import java.io.File

class LyricsParser {
    private val pattern = Regex("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?](.*)")

    fun loadLyrics(songPath: String): List<LyricLine> {
        val songFile = File(songPath)
        val lrcFile = File(songFile.parentFile, "${songFile.nameWithoutExtension}.lrc")
        if (lrcFile.exists()) return parseLrc(lrcFile)

        val txtFile = File(songFile.parentFile, "${songFile.nameWithoutExtension}.txt")
        if (txtFile.exists()) {
            return txtFile.readLines().mapIndexed { idx, text ->
                LyricLine(timestampMs = idx * 5_000L, content = text)
            }
        }
        return emptyList()
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
}
