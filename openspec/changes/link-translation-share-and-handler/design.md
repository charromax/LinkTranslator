# Design

## Context

The app is an unmodified Compose template (single `MainActivity`, no permissions, minSdk 24). See proposal.md for motivation. Android 11+ package visibility hides other apps unless declared in the manifest.

## Goals / Non-Goals

**Goals:**
- One translation pipeline shared by the share target and link handler.
- Adding a platform requires only a table entry.

**Non-Goals:**
- Background services, clipboard or notification monitoring.
- Own backend or Spotify/YouTube API credentials.

## Decisions

- **Trampoline activity (`TranslateActivity`)**: a single activity receives `ACTION_SEND` (text/plain) and `ACTION_VIEW` (`https://open.spotify.com/track/*`), runs the pipeline and finishes. Alternative: separate activities per entry; rejected as duplicative.
- **Track identification**: read `og:title` (track) and `og:description` (artist is the first segment) from the public Spotify track page; no credentials. The Odesli API was retired (401/410), so cross-platform link resolution is replaced by opening a search on the target app. Fallback for non-Spotify sources or unidentified tracks: `https://song.link/<url>`, which the Songlink landing page supports without an API. Alternatives: Odesli with an allowlisted key (not obtainable), Spotify Web API (needs embedded secret), scraping the song.link page (no YouTube Music link in its data).
- **Direct lookup (`TrackFinder`)**: only for the chosen platform, search by title and artist and accept the first result whose normalized title and artist match. Deezer (`api.deezer.com/search/track`) and Apple Music (iTunes Search API) are public; YouTube Music uses its unofficial `youtubei` search (songs filter, `videoId` from list items). Tidal has no keyless API and uses search only. Any failure, timeout (5 s) or non-match keeps the search URL. Alternative: look up every installed platform before the picker; rejected to avoid extra requests.
- **Platform table**: a static mapping of platform id to Android package, display name and search URL template (Spotify, YouTube Music, Deezer, Tidal, Apple Music).
- **Installed-app detection**: `PackageManager` lookups for the table's packages, enabled by a `<queries>` block in the manifest. Alternative: `QUERY_ALL_PACKAGES`; rejected for Play policy.
- **Launching**: `ACTION_VIEW` on the platform URL with the target package set, with browser fallback if the app cannot handle it.
- **Networking**: `HttpURLConnection` on a coroutine and a regex over the Open Graph tags, avoiding new dependencies for one request.
- **UI**: a minimal Compose picker/progress/error screen shown only when needed; the single-target path finishes immediately.

- **Layering (single module, package-level)**:
  - `ui`: `TranslateActivity`, `MainActivity` (setup guide and preference reset), `TranslateViewModel` exposing `UiState` (Loading, NoLink, Picker, Error, Launch).
  - Core (pure Kotlin): `Platform` table with `parseLink`, and `translate()` (resolve, exclude source, intersect installed, apply preference, fall back to song.link). `LinkResolver` is the only interface, so `translate()` is unit-tested with fakes; `SpotifyResolver` is its implementation.
  - Android glue (`Device.kt`): installed-app detection, `openUrl` launcher and DataStore preference.
  - Alternative: Gradle modules or more interfaces per layer; rejected as premature.
- **No DI**: the ViewModel creates its few collaborators directly; no Hilt or container.
- **Execution scope**: translation runs in the ViewModel scope tied to the activity lifecycle. No foreground service or WorkManager, as the user watches the work for a few seconds. Process death mid-request simply requires re-sharing the link.
- **Preferred target**: stored with Jetpack DataStore (Preferences) as the chosen platform key. The picker offers "always use this app"; on later links, an eligible stored target skips the picker. A stale preference (app uninstalled) falls through to normal selection. `MainActivity` exposes a way to clear it.
- **Compose and ViewModel** for all UI, matching the existing template.

## Risks / Trade-offs

- [Spotify retains verified-link handling on Android 12+] → Setup guidance screen and share target as the primary path.
- [Spotify page markup changes or blocks the request] → Isolated in `SpotifyResolver`; failure shows retry and an "Open on song.link" action.
- [Search opens results, not the exact track] → Direct lookup first; search only when lookup fails or for Tidal.
- [YouTube Music endpoint is unofficial] → Failures fall back to search; parsing is isolated in `WebTrackFinder`.
- [A wrong direct match] → Require title and artist to match before accepting a result.
- [Package list gets stale] → Table-driven, easy to extend.
- [Activity destroyed mid-translation] → Acceptable; the user re-shares the link.
- [Stored preference becomes invalid] → Validate against installed apps on every use and fall back to the picker.
