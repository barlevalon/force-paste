# Force Paste development

For downloading, installing, and using the app, start with the [README](README.md).

## Build from source (Linux)

Prerequisites: Git, Bash, curl, unzip, coreutils (including `sha1sum`), and [mise](https://mise.jdx.dev/getting-started.html). Internet access is needed for tools and dependencies. `.mise.toml` selects Java 21 and Gradle 9.3; there is no Gradle wrapper.

```bash
git clone https://github.com/barlevalon/force-paste.git
cd force-paste
# Review .mise.toml before trusting the project configuration.
mise trust
mise install
./build.sh
```

Run commands from the repository root. `build.sh` stays a **debug build**: it creates `app/build/outputs/apk/debug/app-debug.apk` and copies it to `dist/force-paste.apk`. Debug APKs are not public release artifacts. Different machines can use different debug signing keys.

The script uses `$ANDROID_HOME` if set, otherwise `~/Android/Sdk`; mise uses the same location. To select another SDK:

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./build.sh
```

On first use, the script downloads Google’s Linux command-line tools into a private temporary directory, checks the pinned SHA-1 from Google’s repository metadata, and removes temporary files on exit. SHA-1 is the digest Google provides here, not a modern collision-resistant signature. Missing platform tools, Android SDK platform 34, and build tools 36.0.0 are installed after you review and accept Google’s SDK licenses. Complete SDK installations skip that step.

## Test and install a debug build

With SDK components installed:

```bash
mise exec -- gradle assembleDebug testDebugUnitTest --console=plain
```

Unit tests cover Unicode code-point boundaries, cancellation, and failed or thrown editor commits. They do not validate Android keyboard lifecycle or receiving-app behavior. On a test device, also check enable/select, text insertion, cancellation, field changes, hide/switch, switch-back, and recovery with invented text.

For USB installation, enable USB debugging on a test device, connect it, and approve the computer’s authorization prompt:

```bash
mise exec -- sh -c '"$ANDROID_HOME/platform-tools/adb" devices'
mise exec -- sh -c '"$ANDROID_HOME/platform-tools/adb" install -r app/build/outputs/apk/debug/app-debug.apk'
```

Alternatively, transfer and open the debug APK on the device; see the [installation steps](README.md#install). Release and debug signatures differ: installing one over the other fails. Switch to your usual keyboard and uninstall the previous app first only if you accept losing its app data. Re-enable Force Paste after installing again.

To remove a test installation after switching keyboards:

```bash
mise exec -- sh -c '"$ANDROID_HOME/platform-tools/adb" uninstall com.forcepaste'
```

## Build and sign a release

Use the installed SDK and mise tools above. The release version lives in `app/build.gradle.kts`: `versionName = "1.0.0"`, `versionCode = 2`. Increase the code for subsequent releases. No Gradle signing configuration or CI publication is required.

```bash
mise exec -- gradle assembleRelease testDebugUnitTest lintRelease --console=plain
```

The unit tests use the debug variant (the project has no `testReleaseUnitTest` task); lint checks the release variant.

Output: `app/build/outputs/apk/release/app-release-unsigned.apk`. It is **not installable until signed**. Release builds are non-debuggable; do not distribute the debug APK instead.

The maintainer’s signing key is stored **outside the checkout** at `~/.local/share/force-paste/signing/release.p12`, alias `force-paste`. Its password file is `~/.local/share/force-paste/signing/password`. These must already exist: keep the directory private (mode `700`) and both files readable only by the owner (mode `600`), with a secure backup. Reuse the same key for updates; losing it prevents signing compatible updates.

Never commit signing material, put password values in command-line arguments, or print them in logs. The following uses password-file references, not literal passwords. It assumes the key and key-store passwords are the same and the SDK is at `$ANDROID_HOME` or `~/Android/Sdk`:

```bash
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
build_tools="$ANDROID_HOME/build-tools/36.0.0"
signing="$HOME/.local/share/force-paste/signing"
mkdir -p dist

# Check Gradle's alignment before signing; do not modify the APK after signing.
"$build_tools/zipalign" -c -P 16 -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk

mise exec -- "$build_tools/apksigner" sign \
  --ks "$signing/release.p12" --ks-type PKCS12 --ks-key-alias force-paste \
  --ks-pass "file:$signing/password" \
  --out dist/force-paste-1.0.0.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

mise exec -- "$build_tools/apksigner" verify --verbose --print-certs \
  dist/force-paste-1.0.0.apk
mise exec -- "$ANDROID_HOME/cmdline-tools/latest/bin/apkanalyzer" manifest debuggable \
  dist/force-paste-1.0.0.apk
sha256sum dist/force-paste-1.0.0.apk
```

Verification must succeed and `manifest debuggable` must print `false`. Record the certificate fingerprint and APK checksum with the release evidence. Test the signed APK on an emulator or test device, including installation and keyboard recovery, before publishing. Signature verification proves an APK matches its signing key, not that its contents are safe.

Build and sign from the source commit used for the release tag, and upload the exact APK that passed validation.

Publish only after review and explicit approval: use tag `v1.0.0` at the reviewed source commit and attach `dist/force-paste-1.0.0.apk` to its GitHub release with user-facing notes. Do not publish signing files. If a release is broken, withdraw the affected asset and direct users to switch keyboards or uninstall; ship a fix with a higher version code and the same signing key rather than relying on an Android downgrade.

## Source map and behavior

```text
app/src/main/
├── java/com/forcepaste/
│   ├── SettingsActivity.kt  # enable-keyboard onboarding
│   ├── ForcePasteIME.kt     # clipboard snapshot, original input target, lifecycle
│   └── PasteText.kt         # cancellable code-point insertion loop
├── res/values/strings.xml   # onboarding and feedback text
└── AndroidManifest.xml     # keyboard service and launcher; no Internet permission
app/src/test/java/com/forcepaste/PasteTextTest.kt
```

Insertion calls `InputConnection.commitText()` once per Unicode code point, with a 5 ms delay between calls. Surrogate pairs stay together; combining sequences and joined emoji may span calls. The first clipboard item must contain inline text: no URI coercion or provider reads. Hiding the input view, finishing or restarting input, changing fields, and destroying the service cancel pending work. A missing connection or failed commit stops insertion; text already sent is neither undone nor retried.
