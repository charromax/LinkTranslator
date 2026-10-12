# Spec Delta

## Purpose

Defines how music links enter LinkTranslator, via the Android share sheet and via taps on Spotify links, so users can translate a link without manual copying.

## ADDED Requirements

### Requirement: Share sheet intake
The system SHALL appear as a share target for plain-text content and SHALL extract a supported music link from the shared text.

#### Scenario: Share a Spotify link
- **WHEN** the user shares text containing an `open.spotify.com/track/...` URL to the app
- **THEN** the system starts translating that URL

#### Scenario: Shared text has no supported link
- **WHEN** the user shares text containing no supported music URL
- **THEN** the system informs the user that no music link was found and does not start a translation

### Requirement: Spotify link handler
The system SHALL be registrable as a handler for `open.spotify.com` track links so tapping one can start a translation.

#### Scenario: Tap a Spotify link with the app set as handler
- **WHEN** the user taps an `open.spotify.com/track/...` link and the app is the selected handler
- **THEN** the system starts translating that URL without requiring further input

### Requirement: Handler setup guidance
The system SHALL explain to the user that link handling must be enabled manually in Android settings.

#### Scenario: First launch from the launcher
- **WHEN** the user opens the app from the launcher
- **THEN** the system shows how to enable the app as the Spotify link handler and how to use the share sheet
