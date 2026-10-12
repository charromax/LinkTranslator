# Design

## Context

See proposal.md for motivation. The project currently uses `LinkTranslator` in its Gradle root name, Android label, theme names, source/test package declarations, namespace, and application ID. The app has not been distributed, so the application ID may change without an installed-user migration.

## Goals / Non-Goals

**Goals:**

- Make Polyglot the consistent name for user-facing, build-time, and source-level identity.
- Establish `com.charr0max.polyglot` as the Android namespace and application ID.
- Preserve the existing link translation behavior while applying the supplied Polyglot launcher icon and color palette.
- Clearly separate repository-controlled changes from GitHub and future distribution-console work.

**Non-Goals:**

- Supporting updates from `com.charr0max.linktranslator`.
- Renaming third-party package visibility entries or supported-service URLs.

## Decisions

- **Rename both namespace and application ID to `com.charr0max.polyglot`.** This creates a coherent source and installed-app identity before first release. Retaining the old application ID was rejected because it leaves public Android identity inconsistent with the product name.
- **Move Kotlin production and unit-test packages/directories together.** Package declarations and file paths remain aligned, avoiding mixed old/new namespaces. A declaration-only rename was rejected because it leaves misleading repository structure.
- **Rename internal resource/style identifiers that contain `LinkTranslator`.** Manifest references change with them, while resource file paths and launcher asset files remain intact until new artwork arrives. Keeping old style names was rejected because they are part of the project’s maintainable identity.
- **Use the supplied adaptive icon and palette.** The manifest uses the supplied Polyglot icon resources. `phthalo_green`, `pine_wood`, and `retro_off_white` are defined in XML and Compose, with explicit Material light and dark schemes. Dynamic system colors are opt-in rather than the default so the Polyglot palette is consistently visible on Android 12+.
- **Treat GitHub and store setup as external follow-up.** Repository name changes happen through GitHub, and the new application ID must be used when first creating an app record in a distribution console. Neither can be made reliable by a local source rename alone.

## Risks / Trade-offs

- [A stale old package reference survives the rename] -> Search the repository for both `LinkTranslator` and `com.charr0max.linktranslator`, then compile and run all unit tests.
- [Android Studio retains old run/debug configuration state] -> Re-sync Gradle and recreate a run configuration if its module or launch target is stale.
- [Installed debug builds appear as a separate app] -> Uninstall the old debug package during local verification; this is expected after the application ID change.
- [GitHub repository URL remains LinkTranslator] -> Rename the repository separately and update the local `origin` URL before publishing or sharing it.

## Migration Plan

1. Apply the source, Gradle, resource, documentation, and package-path renames.
2. Build and test under the new application ID; install as a clean app and re-verify sharing and Spotify link handling.
3. Rename the GitHub repository and update the local remote URL.
4. When distributing for the first time, register `com.charr0max.polyglot` in the chosen distribution console.

Rollback before distribution is a revert of the rename change. After publishing under the new ID, any later identity change requires a separate migration decision.
