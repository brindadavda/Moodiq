package com.example.moodiq.data.media

import com.example.moodiq.domain.model.LyricLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.max

class LyricsParser {
    private val pattern = Regex("\\[(\\d{2}):(\\d{2})(?:[.:](\\d{2,3}))?](.*)")

    suspend fun loadLyrics(songPath: String, title: String, artist: String, durationMs: Long): List<LyricLine> =
        withContext(Dispatchers.IO) {
            val songFile = File(songPath)
            val lrcFile = File(songFile.parentFile, "${songFile.nameWithoutExtension}.lrc")
            if (lrcFile.exists()) return@withContext parseLrc(lrcFile)

            val txtFile = File(songFile.parentFile, "${songFile.nameWithoutExtension}.txt")
            if (txtFile.exists()) {
                return@withContext txtFile.readLines().mapIndexed { idx, text ->
                    LyricLine(timestampMs = idx * 5_000L, content = text)
                }
            }

            val remote = fetchLyricsFromLrcLib(title = title, artist = artist, durationMs = durationMs)
            if (remote != null) {
                val (synced, plain) = remote
                if (!synced.isNullOrBlank()) {
                    lrcFile.writeText(synced)
                    return@withContext parseLrcLines(synced.lines())
                }
                if (!plain.isNullOrBlank()) {
                    return@withContext toTimedLyrics(
                        lines = plain.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList(),
                        durationMs = durationMs
                    )
                }
            }

            return@withContext buildAutoKaraokeLines(title, artist, durationMs)
        }

    private fun parseLrc(file: File): List<LyricLine> {
        return parseLrcLines(file.readLines())
    }

    private fun parseLrcLines(lines: List<String>): List<LyricLine> {
        return lines.mapNotNull { line ->
            val match = pattern.find(line) ?: return@mapNotNull null
            val minutes = match.groupValues[1].toLongOrNull() ?: 0L
            val seconds = match.groupValues[2].toLongOrNull() ?: 0L
            val millis = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
            val text = match.groupValues[4].ifBlank { "..." }
            LyricLine((minutes * 60_000) + (seconds * 1_000) + millis, text)
        }.sortedBy { it.timestampMs }
    }

    private fun fetchLyricsFromLrcLib(title: String, artist: String, durationMs: Long): Pair<String?, String?>? {
        return runCatching {
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedDuration = (durationMs / 1_000L).coerceAtLeast(1L)
            val url =
                "https://lrclib.net/api/get?track_name=$encodedTitle&artist_name=$encodedArtist&duration=$encodedDuration"
            val connection = java.net.URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 7_000
            connection.readTimeout = 7_000
            connection.setRequestProperty("User-Agent", "Moodiq/1.0")
            connection.setRequestProperty("Accept", "application/json")

            connection.inputStream.bufferedReader().use { reader ->
                val body = reader.readText()
                val json = JSONObject(body)
                val synced = json.optString("syncedLyrics").takeIf { it.isNotBlank() && it != "null" }
                val plain = json.optString("plainLyrics").takeIf { it.isNotBlank() && it != "null" }
                synced to plain
            }
        }.getOrNull()
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
