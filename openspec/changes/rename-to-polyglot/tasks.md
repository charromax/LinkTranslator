# Tasks

## 1. Android and source identity

- [x] 1.1 Change the Gradle root project name, Android namespace, and application ID to Polyglot / `com.charr0max.polyglot`; verify `./gradlew :app:assembleDebug` generates an APK under the new identity.
- [x] 1.2 Move production and unit-test Kotlin source directories from `com/charr0max/linktranslator` to `com/charr0max/polyglot`, and update package/import declarations; verify `./gradlew :app:testDebugUnitTest` passes.
- [x] 1.3 Rename theme/resource identifiers and manifest references from LinkTranslator to Polyglot; verify the app installs and launches without missing-resource or component-resolution errors.

## 2. Product-facing branding

- [x] 2.1 Update the launcher label, in-app headings, setup guide, and README to Polyglot; verify no user-facing `LinkTranslator` text remains in the built app or README.
- [x] 2.2 Preserve the existing launcher icon and color resources until supplied replacement assets arrive; verify the debug build still packages launcher resources successfully.
- [x] 2.3 Apply the supplied Polyglot adaptive launcher icon and core palette to Android resources and Compose Material schemes; verify a debug build packages the icon and displays branded colors instead of dynamic system colors by default.

## 3. Rename verification and external follow-up

- [x] 3.1 Search the repository for stale `LinkTranslator` and `com.charr0max.linktranslator` references, retaining only intentional historical OpenSpec records; verify all active source, build, resource, and documentation references use Polyglot.
- [x] 3.2 Install the renamed debug app as a clean package and re-verify sharing a Spotify link plus `open.spotify.com` link handling.
- [x] 3.3 Rename the GitHub repository to `Polyglot`, then update the local `origin` remote URL; verify `git remote -v` and the GitHub repository URL use the new name.

## Workflow follow-up

- When preparing the first distribution, register `com.charr0max.polyglot` in the selected Android distribution console and verify the package ID matches the release build.
- Archive the change after review.
