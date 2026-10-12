# Design

## Context

See proposal.md for motivation. The repository has no workflows, release tags,
or signing configuration. The default branch is `master`, and the Android app
currently has static version values with no release keystore configuration.

## Goals / Non-Goals

**Goals:**

- Verify every pull request with the same unit tests and debug build used
  locally.
- Deliver signed APK and AAB artifacts for both tester prereleases and stable
  tagged releases without exposing signing material.
- Make release provenance and version identity clear enough to support a later
  Play Console upload.
- Standardize the agent-assisted PR and release-preparation process.

**Non-Goals:**

- Uploading builds to Google Play, Firebase App Distribution, or another store.
- Automatically merging pull requests or pushing stable version tags.
- Committing a keystore, decoded keystore file, or credential to the repository.
- Running instrumentation tests on an emulator in this initial workflow.

## Decisions

- **Use separate CI and release workflows.** CI runs on `pull_request` and
  pushes to `master`, executes `:app:testDebugUnitTest :app:assembleDebug`,
  and uploads the debug APK. A separate release workflow handles trusted
  `master` and `v*` tag pushes with signing secrets. This keeps untrusted pull
  request execution isolated from release credentials; a single workflow was
  rejected because it makes permission boundaries less clear.
- **Publish a prerelease per successful `master` commit.** Each prerelease gets
  a unique `ci-<short-sha>` tag and signed APK/AAB assets. Stable releases are
  created only for tags matching `vX.Y.Z`. Publishing stable releases on every
  merge was rejected because testers need rapid builds while store candidates
  need intentional versioning.
- **Derive release version inputs in CI.** The workflow supplies a monotonically
  increasing integer from GitHub Actions run numbering as `versionCode`.
  Tagged builds use the tag without its `v` prefix as `versionName`; prereleases
  use an identifiable CI version name. Gradle accepts CI inputs but retains safe
  local defaults. This avoids manually editing version files for every tester
  build; an external version-management service was rejected as unnecessary.
- **Use a user-created upload keystore held only in GitHub secrets.** Required
  secrets are `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`,
  `ANDROID_KEYSTORE_PASSWORD`, and `ANDROID_KEY_PASSWORD`. The release workflow
  decodes the keystore only in the ephemeral runner, configures signing through
  environment-driven Gradle properties, and removes temporary files. Debug
  builds and pull requests never access those values.
- **Use GitHub Releases as the tester delivery channel.** The signed APK is the
  convenient installable tester artifact; the AAB is retained alongside it for
  Play preparation. Workflow artifacts remain the fast feedback channel for
  pull-request debug APKs.
- **Add a repository release-readiness skill.** It checks that an OpenSpec
  change is complete, runs the local verification command, prepares a PR with
  release notes, and explains that a merge triggers a prerelease while a
  maintainer-created `vX.Y.Z` tag triggers a stable release. The skill does not
  bypass review, merge a PR, create secrets, or push a stable tag.
- **Require the CI status through branch protection after its first run.**
  GitHub configuration cannot reliably require a status check before it exists,
  so documentation will direct the maintainer to protect `master` after the CI
  workflow reports its stable check name.

## Risks / Trade-offs

- [Upload keystore is lost] -> Create an encrypted offline backup before adding
  GitHub secrets; losing it prevents future update signing.
- [A secret is accidentally exposed] -> Keep secret setup out of source files,
  scope secrets to trusted release events, and never print decoded material.
- [Prerelease version naming differs from a future stable tag] -> Attach the
  commit SHA to prereleases and use only semantic tags for stable releases.
- [Run-number version codes are not suitable for a later release policy] ->
  Preserve the Gradle input boundary so the policy can change before Play
  publishing without redesigning workflows.
- [Branch protection is omitted] -> Document the manual setup and make CI's
  check name explicit.

## Migration Plan

1. Add CI, release, Gradle signing/version inputs, documentation, and the
   release-readiness skill.
2. Generate and back up an upload keystore locally; add the required secrets
   through GitHub settings without committing them.
3. Trigger a pull request to establish the CI status, then enable branch
   protection on `master`.
4. Merge a test change and verify its prerelease assets; push a test
   `vX.Y.Z` tag only after confirming signing and GitHub Release permissions.

Rollback removes the workflows and signing inputs. Revoke and replace any
secret or keystore suspected of exposure before retrying releases.
