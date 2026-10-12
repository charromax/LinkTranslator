# Proposal

## Why

When a friend sends a Spotify link, users on other music platforms (e.g. YouTube Music) cannot open the same song without manually searching for it. LinkTranslator should translate music links between platforms and open them in the app the user actually has installed.

## What Changes

- Accept music links via the Android share sheet (primary entry point).
- Handle taps on `open.spotify.com` links through an intent filter (requires the user to select the app as the link handler).
- Identify the track (title and artist) from the public Spotify track page; the Odesli API was retired and is not used.
- Detect which supported music apps are installed and only offer those as targets.
- Launch the target app directly when there is one candidate, show a picker when there are several, and open the link on song.link in the browser when none is installed.
- Let the user remember a preferred target app so later links open instantly.
- Try a direct track lookup on the chosen platform (Deezer, Apple Music, YouTube Music); otherwise open the target app on a search for the track title and artist; fall back to `song.link/<url>` when the track can't be identified.

Out of scope: clipboard monitoring, notification listener, accessibility service, and non-Spotify source link handlers.

## Capabilities

### New Capabilities
- `link-intake`: Receiving music links via the share sheet and `open.spotify.com` link taps.
- `link-translation`: Identifying a source track and building a target-platform search, with song.link fallback.
- `target-app-selection`: Detecting installed music apps and choosing/launching the target.

### Modified Capabilities

## Impact

- `AndroidManifest.xml`: `INTERNET` permission, `<queries>` for package visibility, share and VIEW intent filters.
- New translation activity, Spotify page resolver, installed-app detector and platform mapping table.
- New dependencies: Jetpack DataStore (preferred target) and lifecycle ViewModel for Compose.
- Network dependency on the public Spotify track page (unofficial; markup may change) and the song.link landing page.
