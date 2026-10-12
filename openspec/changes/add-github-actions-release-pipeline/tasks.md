# Tasks

## 1. Release build configuration

- [x] 1.1 Add environment-driven Gradle version-code, version-name, and release-signing inputs with safe local defaults; verify `./gradlew :app:testDebugUnitTest :app:assembleDebug` succeeds without release secrets.
- [x] 1.2 Configure the release build to produce signed APK and AAB outputs only when CI supplies signing inputs; verify the configuration fails explicitly rather than producing an unsigned release when a release workflow lacks required inputs.

## 2. Pull request verification

- [x] 2.1 Add a GitHub Actions CI workflow for pull requests to and pushes on `master` that runs `:app:testDebugUnitTest :app:assembleDebug`; verify the workflow YAML is valid and its check name is stable for branch protection.
- [x] 2.2 Upload the debug APK only after a successful CI build, with a clear artifact name and retention period; verify a successful workflow run exposes a downloadable APK artifact and a failed test run does not.

## 3. Signed release delivery

- [x] 3.1 Add a least-privilege release workflow that receives signing values only from GitHub secrets, decodes the keystore in the runner, and removes temporary material; verify the workflow never exposes secrets in logs or repository files.
- [x] 3.2 Publish a signed prerelease tagged from each successful `master` commit, attaching APK and AAB files; verify a merged test PR produces a uniquely tagged GitHub prerelease.
- [x] 3.3 Publish signed non-prerelease GitHub Releases only for pushed `vX.Y.Z` tags, attaching APK and AAB files; verify a test semantic-version tag creates the expected stable release.

## 4. Release process documentation and skill

- [x] 4.1 Document upload-keystore creation and offline backup, required GitHub secret names, Actions permissions, branch-protection setup on `master`, tester installation, and the deferred Play Console handoff; verify the documented commands and settings names match the workflows.
- [x] 4.2 Add a repository release-readiness skill that requires completed OpenSpec tasks and passing local verification before preparing a PR, and explains merge/tag release triggers without merging or tagging autonomously; verify the skill’s instructions cover prerelease and stable-release paths.
- [x] 4.3 Link the release process from README.md; verify contributors can discover the CI/tester-release workflow from the project landing page.

## 5. Owner-managed GitHub setup and end-to-end verification

- [x] 5.1 Create and securely back up the upload keystore, then add `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEY_ALIAS`, `ANDROID_KEYSTORE_PASSWORD`, and `ANDROID_KEY_PASSWORD` as GitHub Actions secrets; verify no secret or keystore is tracked by Git.
- [x] 5.2 After the first CI run, protect `master` by requiring pull requests and the CI verification check before merging; verify GitHub blocks a failing pull request.
- [ ] 5.3 Verify the full release path with a merged test PR and a test `vX.Y.Z` tag; verify prerelease/stable artifacts install and their release metadata points to the correct commit.

## Workflow follow-up

- Add Play Console publishing in a separate change after the app record and service-account policy are ready.
- Archive the change after review.
