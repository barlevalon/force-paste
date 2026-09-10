# Force Paste

A small, temporary Android keyboard that inserts clipboard text through Android’s keyboard input API instead of the Paste command. This may work around some paste-menu restrictions; receiving apps can still reject, filter, or change the text. **Android 7.0+ required.**

## Build and install (Linux)

This guide builds from source and produces a **debug APK**, not a signed release distribution. Only install APKs from sources you trust. Debug builds made on different machines may use different signing keys and cannot necessarily update one another.

Prerequisites: Git, Bash, curl, unzip, coreutils (including `sha1sum`), and [mise](https://mise.jdx.dev/getting-started.html). Internet access is needed for tools and dependencies. The project’s `.mise.toml` selects Java 21 and Gradle 9.3; there is no Gradle wrapper.

```bash
git clone https://github.com/barlevalon/force-paste.git
cd force-paste
# Review .mise.toml before trusting the project configuration.
mise trust
mise install
./build.sh
```

Run these commands from the repository root. `build.sh` uses `$ANDROID_HOME` if set, otherwise `~/Android/Sdk`; mise uses the same location. To use another SDK, set it before building:

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./build.sh
```

On first use, the script downloads Google’s Linux command-line tools into a private temporary directory, checks the pinned SHA-1 published in Google’s repository metadata, and cleans up the temporary files on exit. SHA-1 is the digest provided by that metadata, not a modern collision-resistant signature. Missing platform tools, Android SDK platform 34, and build tools 36.0.0 are installed after you review and accept Google’s SDK licenses. Existing complete SDK installations skip that step.

The build creates `app/build/outputs/apk/debug/app-debug.apk` and copies it to `dist/force-paste.apk`. It does not run sudo or share files by default. With SDK components already installed, build and test directly:

```bash
mise exec -- gradle assembleDebug testDebugUnitTest --console=plain
```

To install over USB, enable USB debugging on your Android device, connect it, and approve the computer’s authorization prompt:

```bash
mise exec -- sh -c '"$ANDROID_HOME/platform-tools/adb" devices'
mise exec -- sh -c '"$ANDROID_HOME/platform-tools/adb" install -r app/build/outputs/apk/debug/app-debug.apk'
```

Alternatively, transfer `app/build/outputs/apk/debug/app-debug.apk` to your device and open it. Android may ask you to allow installation from that file manager or browser; turn that permission off afterward if no longer needed. A signature mismatch requires using the original signing key or uninstalling the old app first; uninstalling removes its app data.

Optional tailnet sharing: `./build.sh --share` explicitly enables persistent **Tailscale Serve** for just the APK and prints its status. It requires an installed, connected Tailscale client and permission to configure Serve; the script never elevates privileges. Access follows your tailnet policy, not public Internet Funnel. Stop this Serve configuration with `tailscale serve --https=443 off` (this also affects other handlers on that listener; check `tailscale serve status` first).

## Paste and return to your normal keyboard

1. Open Force Paste and tap **Enable Keyboard**, then enable **Force Paste** in Android’s keyboard settings.
2. Copy text, tap the destination field, and select **Force Paste** using Android’s keyboard switcher (often in the navigation bar or a keyboard notification).
3. Tap **PASTE CLIPBOARD**. The button changes to **PASTING — TAP TO CANCEL**. Tap again to stop; hiding the keyboard, switching keyboards, changing fields, or restarting the input session also cancels insertion. Text already sent stays in the field.
4. Check the result before retrying to avoid duplicates. Select your usual keyboard when finished; Force Paste is paste-only, not a replacement typing keyboard.

**No keyboard switcher?** Open Android Settings and find **Languages & input / On-screen keyboard** (names vary by device). Select your usual keyboard as the default/current keyboard. You can disable Force Paste from the keyboard-management screen; keep your usual keyboard enabled.

To remove Force Paste, first select your usual keyboard, then open **Settings → Apps → Force Paste → Uninstall**, or run:

```bash
mise exec -- sh -c '"$ANDROID_HOME/platform-tools/adb" uninstall com.forcepaste'
```

## Compatibility and keyboard trust

- Only the first clipboard item’s inline text is used. Files, images, and URI-only items are not loaded or converted; copy actual text instead. Formatting is not preserved.
- Text is sent one Unicode code point at a time through `InputConnection.commitText()`, with a short delay between calls. Surrogate pairs stay together, but combining sequences and joined emoji may span calls. This is not physical key simulation or a guarantee of acceptance or undetectability.
- A missing connection or reported commit failure stops insertion with feedback. Even a successful API call cannot prove the app kept the text unchanged; always check the destination.
- Enabling any keyboard is a sensitive trust decision: keyboards can receive input-field context. This implementation reads the clipboard only when you tap **PASTE CLIPBOARD**, keeps a snapshot during insertion, and sends it to the selected field’s original connection. The receiving app gets that text.
- The app source has no clipboard history, persistence, analytics, or network calls, and its manifest requests no Internet permission. This describes the source, not a guarantee about arbitrary APKs, Android, or the receiving app. Avoid testing with passwords or other sensitive clipboard contents.

## Report a problem

Open an issue with Android version/device, Force Paste build or source commit, receiving app/version, steps, and expected versus actual behavior. Use a short **invented, non-sensitive** sample (for example `A😀B` for Unicode problems). Remove passwords, tokens, account details, real clipboard contents, and unrelated text from screenshots or logs before sharing.
