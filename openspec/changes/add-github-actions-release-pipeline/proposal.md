# Proposal

## Why

Polyglot needs repeatable, tested builds that friends and testers can download, while establishing a secure signing and release path compatible with a future Google Play launch.

## What Changes

- Add GitHub Actions CI for pull requests and updates to `master` that runs Android unit tests and builds a downloadable debug APK.
- Make the CI check suitable for GitHub branch protection so pull requests cannot merge without passing tests and a build.
- Create signed GitHub prereleases with APK and AAB artifacts after merges to `master`.
- Create signed stable GitHub Releases with APK and AAB artifacts when a `vX.Y.Z` tag is pushed.
- Add Gradle signing and release-version inputs that obtain all sensitive values exclusively from GitHub Actions secrets.
- Document local upload-keystore creation, secure backup, required GitHub secrets, branch-protection setup, tester downloads, and the future Play Console handoff.
- Add a reusable repository skill that guides completed OpenSpec changes through branch, PR, verification, merge, and release preparation.

## Capabilities

### New Capabilities

- `build-delivery`: Automated Android verification and signed artifact delivery for tester prereleases and stable tagged releases.

### Modified Capabilities

None.

## Impact

- New GitHub Actions workflows, repository skill, PR/release documentation, and Gradle release-signing/version configuration.
- GitHub Actions secrets and repository workflow permissions must be configured manually; keystores and credentials must never be committed.
- `master` branch protection must be enabled manually after the CI check has run at least once.
- Release publishing targets GitHub only in this change; Play Console upload remains a later step.
