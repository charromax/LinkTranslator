# Spec Delta

## Purpose

Defines how a source music link is turned into a search on another streaming platform, with a song.link fallback when the track cannot be identified.

## ADDED Requirements

### Requirement: Track identification
The system SHALL identify the title and artist of a Spotify track link and use them to build a search URL on the target platform.

#### Scenario: Successful identification
- **WHEN** a valid Spotify track URL is submitted and its title and artist can be read
- **THEN** the system builds a search URL on the target platform from the title and artist

### Requirement: Direct track lookup
The system SHALL try to find the exact track on the chosen platform before falling back to search, and MUST accept a result only if its title and artist match the source track.

#### Scenario: Direct match found
- **WHEN** the chosen platform supports lookup and returns a result matching the title and artist
- **THEN** the system opens that track directly instead of a search

#### Scenario: Lookup fails or does not match
- **WHEN** the lookup errors, times out, finds no matching result, or the platform has no lookup
- **THEN** the system opens the search URL for the track

### Requirement: song.link fallback
The system SHALL fall back to the `song.link` page for the original URL when the track cannot be identified or the source is not Spotify.

#### Scenario: Track not identified
- **WHEN** the title cannot be read, or the source link is not from Spotify
- **THEN** the system opens `https://song.link/<original url>` in the browser

#### Scenario: Service unreachable
- **WHEN** the track page cannot be reached
- **THEN** the system informs the user of the failure and offers to retry or open the link on song.link

### Requirement: Progress feedback
The system SHALL show progress while a translation is in flight and MUST NOT leave the user without feedback on failure.

#### Scenario: Slow network
- **WHEN** a translation takes longer than a moment
- **THEN** the system displays a loading indicator until it completes or fails
