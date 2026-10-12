# Spec Delta

## Purpose

Defines how the system detects installed music apps and chooses which one opens the translated link, so users only see targets they can actually use.

## ADDED Requirements

### Requirement: Installed app detection
The system SHALL detect which supported music apps are installed and offer only those as targets.

#### Scenario: Subset installed
- **WHEN** only YouTube Music and Deezer are installed among the supported apps
- **THEN** only those two are offered as targets

### Requirement: Source platform exclusion
The system SHALL NOT offer the platform the link originated from as a target.

#### Scenario: Spotify source link
- **WHEN** the source link is from Spotify
- **THEN** Spotify is not listed among the targets

### Requirement: Target selection behavior
The system SHALL launch automatically when exactly one target is available, show a picker when several are available, and open the song.link page in the browser when none is installed.

#### Scenario: Single target
- **WHEN** exactly one eligible app is installed
- **THEN** the system opens the search in that app without a picker

#### Scenario: Multiple targets
- **WHEN** two or more eligible apps are installed
- **THEN** the system shows a picker listing them

#### Scenario: No targets
- **WHEN** no eligible app is installed
- **THEN** the system opens the song.link page in the browser

### Requirement: Preferred target
The system SHALL let the user remember a preferred target app and SHALL use it automatically while it remains eligible.

#### Scenario: Save preference from picker
- **WHEN** the user picks an app and chooses "always use this app"
- **THEN** the system stores that app as the preferred target

#### Scenario: Preference applied
- **WHEN** a preferred target is stored and is installed
- **THEN** the system opens it directly without showing the picker

#### Scenario: Stale preference
- **WHEN** the preferred app is no longer installed
- **THEN** the system falls back to normal target selection

#### Scenario: Clear preference
- **WHEN** the user clears the preference from the main screen
- **THEN** later translations use normal target selection
