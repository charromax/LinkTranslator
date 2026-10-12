# Spec Delta

## Purpose

Provides tested Android artifacts for collaborators and a secure path from
tester prereleases to stable GitHub releases that can later feed Google Play.

## ADDED Requirements

### Requirement: Pull request verification
The system SHALL run Android unit tests and build a debug APK for every pull request targeting `master`, and SHALL expose a successful CI status check suitable for branch protection.

#### Scenario: Pull request passes verification
- **WHEN** a pull request targeting `master` has passing unit tests and a successful debug build
- **THEN** the workflow reports success and uploads the debug APK as a downloadable workflow artifact

#### Scenario: Pull request fails verification
- **WHEN** unit tests or the debug build fail
- **THEN** the workflow reports failure and does not publish a successful build artifact

### Requirement: Tester prerelease delivery
The system SHALL create a signed GitHub prerelease with APK and AAB artifacts after a successful push to `master`.

#### Scenario: Master branch update succeeds
- **WHEN** a commit reaches `master` and the release build and tests succeed
- **THEN** the system creates or updates a uniquely versioned prerelease containing signed APK and AAB artifacts

#### Scenario: Master branch release fails
- **WHEN** signing, testing, or release assembly fails for a `master` update
- **THEN** the system marks the workflow failed and MUST NOT create a prerelease for that update

### Requirement: Stable tagged release delivery
The system SHALL create a signed stable GitHub Release with APK and AAB artifacts for each pushed `vX.Y.Z` tag.

#### Scenario: Semantic version tag succeeds
- **WHEN** a tag matching `vX.Y.Z` is pushed and its release build and tests succeed
- **THEN** the system creates a non-prerelease GitHub Release using that version and attaches signed APK and AAB artifacts

#### Scenario: Invalid or unconfigured tag release
- **WHEN** a pushed tag does not match `vX.Y.Z`, or required signing secrets are unavailable
- **THEN** the system MUST NOT publish a stable release and SHALL report the failure or non-release outcome in the workflow

### Requirement: Secret-backed signing
The system MUST obtain release-signing material only from GitHub Actions secrets and MUST NOT store keystores, aliases, or passwords in the repository or release artifacts.

#### Scenario: Release signing is configured
- **WHEN** the required signing secrets are available to a trusted release workflow
- **THEN** the workflow signs the release APK and AAB with the configured upload key

#### Scenario: Pull request from an untrusted context
- **WHEN** a pull request workflow executes without release secrets
- **THEN** it runs verification only and MUST NOT attempt to sign or publish a release
