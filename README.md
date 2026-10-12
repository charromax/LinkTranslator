# Polyglot

An Android app that opens music links in the streaming app you actually use. If a friend sends a Spotify link, Polyglot searches for the same track on another platform and opens it in an installed app such as YouTube Music.

## How it works

1. A link arrives through the share sheet, or by tapping an `open.spotify.com/track/...` link (once the app is set as the link handler in Android settings).
2. The app reads the track title and artist from the public Spotify track page (the Odesli API has been retired).
3. The app detects which supported music apps are installed (Spotify, YouTube Music, Deezer, Tidal, Apple Music), excluding the source platform.
4. For the chosen app it tries to find the exact track (Deezer, Apple Music and YouTube Music; the YouTube Music lookup is unofficial) and otherwise opens a search for it. One match opens that app, several show a picker (with an "always use this app" option), and none opens `song.link/<url>` in the browser. The same fallback applies when the track can't be identified or the link isn't from Spotify.

## Tech stack

- Kotlin 2.2
- Jetpack Compose with Material 3
- AndroidX Lifecycle ViewModel
- Jetpack DataStore (preferred app)
- `HttpURLConnection` and `org.json` for Spotify metadata and track lookups (Deezer, iTunes Search, YouTube Music)
- Gradle (Kotlin DSL) with a version catalog and Android Gradle Plugin 9
- compileSdk 37, minSdk 24
- JUnit 4 for unit tests
- [OpenSpec](https://github.com/Fission-AI/OpenSpec) for specs and change planning (`openspec/`)

## Build and test

```
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Tester builds and releases

Pull requests are verified with unit tests and a debug APK build. Signed tester
prereleases are published after merges to `master`; stable GitHub Releases are
created only from intentional `vX.Y.Z` tags. See the
[tester and release delivery guide](docs/releases.md) for the signing setup,
branch-protection check, tester installation, and future Play Console handoff.
