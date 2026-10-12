---
name: release-readiness
description: Prepare a completed OpenSpec change for review and explain Polyglot prerelease or stable release expectations. Use when a user asks to prepare a release PR or check release readiness.
allowed-tools: Bash(openspec:*,git:*,./gradlew), Read
---

Prepare Polyglot changes for a reviewed release path without merging, creating
secrets, or pushing a stable tag.

1. Identify the requested OpenSpec change. Run `openspec instructions apply
   --change "<name>" --json`; stop if any tracked task is incomplete. If the
   change needs manual owner setup or end-to-end verification, name it clearly
   in the PR preparation notes instead of marking it complete.
2. Check the relevant diff and run:

   ```sh
   ./gradlew :app:testDebugUnitTest :app:assembleDebug
   ```

   Stop if verification fails.
3. Prepare the branch and PR only after verification succeeds. Summarize user
   impact, the test command, and any manual follow-up. Do not merge the PR.
4. Explain delivery expectations:
   - A merged PR that reaches `master` triggers a signed tester prerelease
     tagged `ci-<short-sha>`, provided the four Android signing secrets and
     release permissions are configured.
   - A maintainer may intentionally push an exact `vX.Y.Z` tag after merge to
     trigger a signed stable GitHub Release.
5. Do not create or request secret values in chat, create an upload keystore,
   alter branch protection, merge a PR, or push a stable tag. Refer the user
   to [`docs/releases.md`](../../../docs/releases.md) for the owner-managed
   setup and release procedure.
