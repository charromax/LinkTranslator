# Proposal

## Why

The app needs a distinct product identity before its first distribution. Renaming it to Polyglot now allows its Android identity, project metadata, and public documentation to be consistent without breaking existing users.

## What Changes

- Rename the user-facing app and project branding from LinkTranslator to Polyglot.
- **BREAKING** Change the Android namespace and application ID from `com.charr0max.linktranslator` to `com.charr0max.polyglot`, including source and test package paths.
- Rename theme/resource identifiers and repository documentation that expose the previous name.
- Keep the existing launcher artwork and colors until the user supplies replacement assets and a palette.
- Document required manual follow-up outside the repository: rename the GitHub repository and create/register the new Android application ID when preparing a distribution channel.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This is an identity migration with no runtime feature-requirement change.

## Impact

- Android Gradle configuration, Kotlin package declarations/directories, manifest component resolution, themes, resources, tests, and README.
- Android treats the new application ID as a separate app; no update migration is required because the current app has not been distributed.
- GitHub repository and future store-console records require manual renaming or registration outside the codebase.
