# Tester and Release Delivery

Polyglot uses GitHub Actions to verify changes and publish signed Android
artifacts. Google Play publishing is intentionally deferred until the Play
Console app record and service-account policy are ready.

## CI verification

The `Android CI / verification` check runs for every pull request to `master`
and every push to `master`. It runs:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Successful runs retain a `polyglot-debug-apk` artifact for 14 days. This is a
debug-only tester build; it is not a GitHub Release and is not signed with the
upload key.

After the first successful run, configure GitHub branch protection for
`master` in **Settings > Branches > Add branch protection rule**:

1. Set the branch name pattern to `master`.
2. Enable **Require a pull request before merging**.
3. Enable **Require status checks to pass before merging** and select
   `Android CI / verification`.
4. Save the rule.

## Signing setup

Before enabling a merge or tag release, create one Android upload keystore on
a trusted local machine and make an encrypted offline backup. Keep the
keystore and its passwords together; losing the upload key prevents signed
updates with that key.

Run the following locally, choosing a private path *outside this repository*
and replacing `polyglot-upload` with your chosen alias:

```sh
keytool -genkeypair -v \
  -keystore "$HOME/.android/polyglot-upload.jks" \
  -alias polyglot-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Base64-encode the keystore locally without placing it in the repository:

```sh
base64 < "$HOME/.android/polyglot-upload.jks" | tr -d '\n'
```

In **Settings > Secrets and variables > Actions**, add these repository
secrets using the generated values:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Single-line base64 value of the upload keystore |
| `ANDROID_KEY_ALIAS` | The alias supplied to `keytool` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_PASSWORD` | Key password |

Do not commit a keystore, decoded keystore, passwords, or base64 output. The
release workflow decodes the keystore only in its temporary runner directory
and removes it after publishing.

The workflow needs GitHub Actions to have **Read and write permissions** under
**Settings > Actions > General > Workflow permissions**, or an equivalent
repository policy that permits its `contents: write` permission. This is
required to create GitHub Releases.

## Release paths

The trusted `Android release` workflow runs only after a push to `master` or
a tag beginning with `v`.

- A successful push to `master` publishes a signed prerelease tagged
  `ci-<short-sha>`. It contains an installable APK and an AAB retained for
  future Play preparation.
- A successful push of an exact `vX.Y.Z` tag publishes a signed stable GitHub
  Release with the version name `X.Y.Z`, APK, and AAB. A non-semantic `v*` tag
  fails before any release is created.

Testers download the APK from the prerelease or stable GitHub Release, approve
Android's install-from-unknown-sources prompt for their download source if
needed, then install or update Polyglot. The AAB is not directly installable;
keep it for the eventual Play Console delivery process.
