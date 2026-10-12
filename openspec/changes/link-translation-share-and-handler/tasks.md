# Tasks

## 1. Dependencies and Manifest

- [x] 1.1 Add `lifecycle-viewmodel-compose` and `datastore-preferences` to `libs.versions.toml` and `app/build.gradle.kts`; verify `./gradlew :app:assembleDebug` succeeds
- [ ] 1.2 Add `INTERNET` permission, `<queries>` for the supported music app packages, and register `TranslateActivity` with `ACTION_SEND` (text/plain) and `ACTION_VIEW` (`https://open.spotify.com/track/*`) filters; verify the build passes and the app appears in the share sheet on a device or emulator

## 2. Platform Model and Link Parsing

- [x] 2.1 Create the platform table (id, package, display name, search URL template) for Spotify, YouTube Music, Deezer, Tidal and Apple Music; verify unit tests cover lookup by key and by host
- [x] 2.2 Implement `LinkParser` extracting a supported URL and source platform from shared text; verify unit tests for a bare URL, a URL within text, and text with no link (link-intake scenarios)
- [x] 2.3 Implement `SearchUrlBuilder`; verify unit tests for URL encoding of title and artist per platform

## 3. Track Identification

- [x] 3.1 Define the `LinkResolver` interface and `TrackInfo` (title, artist); verify it compiles against a fake used in tests
- [x] 3.2 Implement `SpotifyResolver` reading Open Graph tags with `HttpURLConnection` off the main thread; verify unit tests for title and artist, entity unescaping and a missing title
- [x] 3.3 Replace Odesli (retired API) with the Spotify page plus the song.link fallback; verify design, specs and README match the implementation

## 3b. Direct Lookup

- [x] 3b.1 Implement `TrackFinder` and `refine()` with search fallback; verify unit tests for success, null and exception cases
- [x] 3b.2 Implement `WebTrackFinder` for Deezer, Apple Music and YouTube Music with title and artist matching; verify unit tests on sample responses, mismatch and normalization
- [ ] 3b.3 Verify on a device that a Spotify link opens the exact track in YouTube Music, Deezer or Apple Music, and a search for Tidal

## 4. Target Selection and Launch

- [ ] 4.1 Implement `installedPlatforms()` (`Device.kt`); verify on a device that only installed apps are detected
- [ ] 4.2 Persist the preferred target with DataStore (`Device.kt`); verify through the selector unit tests and an on-device save, read and clear
- [x] 4.3 Implement selection inside `translate()` (resolved links, minus source, intersect installed, apply preference); verify unit tests for the single target, multiple targets, no targets, source exclusion, preferred target and stale preference scenarios
- [ ] 4.4 Implement `openUrl()` with a package-targeted `ACTION_VIEW` and browser fallback; verify manually that a YouTube Music link opens in the app

## 5. Use Case and UI

- [x] 5.1 Implement `translate()` wiring parser output, resolver, selection and search fallback; verify unit tests for success, no match fallback and unreachable service
- [ ] 5.2 Implement `TranslateViewModel` with `UiState` (Loading, NoLink, Picker, Error, Launch); verify each state manually on a device
- [ ] 5.3 Implement `TranslateActivity` with Compose loading, picker (with "always use this app"), error with retry, and the no-link message; verify with a Compose UI test or manual run of each state
- [ ] 5.4 Implement the `MainActivity` setup guide (enable link handling, share usage) and clear-preference action; verify manually on first launch and after clearing
- [x] 5.5 Wire dependencies directly in the ViewModel (no `AppContainer`, to keep things simple); verify the app builds

## 6. Integration Checks

- [ ] 6.1 Share a Spotify track link from another app on a device with YouTube Music installed; verify it opens the same track
- [ ] 6.2 Set the app as the `open.spotify.com` handler and tap a Spotify link; verify it translates and launches without extra input
- [x] 6.3 Run `./gradlew :app:testDebugUnitTest :app:assembleDebug` and `openspec validate link-translation-share-and-handler --strict`; verify both pass

## Workflow follow-up

- Archive the change after review.
