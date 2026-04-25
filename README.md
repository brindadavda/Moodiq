# Moodiq — AI-Powered Local Music Player

Moodiq is an offline-first local music player built with Kotlin + Jetpack Compose using MVVM + Clean Architecture.

## Project structure

- `app/src/main/java/com/example/moodiq/core`
  - App container and application bootstrap.
- `data/`
  - `media/` MediaStore fetcher + lyrics parser (`.lrc` + `.txt`).
  - `local/` Room database, entities, DAOs.
  - `repository/` repository implementation.
- `domain/`
  - Models, repository contracts, use cases.
- `player/`
  - ExoPlayer controller + MediaSession service.
- `ui/`
  - Compose screens, navigation, components, theme, and ViewModel.
- `workers/`
  - Smart notification scheduler via WorkManager.

## Features included

- Local music scan using MediaStore.
- ExoPlayer playback with queue support.
- Lyrics support:
  - Preferred: `.lrc` synchronized parsing.
  - Fallback: `.txt` static lyrics.
- Favorites with Room persistence.
- Play history tracking.
- Rule-based recommendation engine (night chill, repeat/favorites, skip penalty).
- Insights dashboard (most played, favorite artist, patterns, mood insight).
- Dynamic playlists (Favorites, Most Played, Recently Played, Night Chill).
- WorkManager-based smart reminders.

## How to run

1. Open project in Android Studio (latest stable).
2. Let Gradle sync.
3. Run on device/emulator (API 24+).
4. Grant storage/audio permissions:
   - Android 13+: `READ_MEDIA_AUDIO`
   - Android 12 and below: `READ_EXTERNAL_STORAGE`
5. Add local songs to device storage.
6. Optional: place lyrics files in same folder as songs:
   - `song_name.lrc` (synced)
   - `song_name.txt` (fallback)

## Notes

- The app is offline-only (no streaming).
- Smart notifications are scheduled periodically every 24 hours.
- Lockscreen/notification transport controls are enabled through Media3 session service.
