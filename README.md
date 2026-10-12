# LinkTranslator

An Android app that opens music links in the streaming app you actually use. If a friend sends a Spotify link, LinkTranslator finds the same track on another platform and opens it in an installed app such as YouTube Music.

## How it works

1. A link arrives through the share sheet, or by tapping an `open.spotify.com/track/...` link (once the app is set as the link handler in Android settings).
2. The Odesli (song.link) API resolves the track to equivalent links on other platforms.
3. The app detects which supported music apps are installed (Spotify, YouTube Music, Deezer, Tidal, Apple Music), excluding the source platform.
4. One match opens directly, several show a picker (with an "always use this app" option), and none opens the link in the browser. If there is no direct link, it falls back to a search URL on the chosen platform.

## Tech stack

- Kotlin 2.2
- Jetpack Compose with Material 3
- AndroidX Lifecycle ViewModel
- Jetpack DataStore (preferred app)
- `HttpURLConnection` and `org.json` for the Odesli API
- Gradle (Kotlin DSL) with a version catalog and Android Gradle Plugin 9
- compileSdk 37, minSdk 24
- JUnit 4 for unit tests
- [OpenSpec](https://github.com/Fission-AI/OpenSpec) for specs and change planning (`openspec/`)

## Build and test

```
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
