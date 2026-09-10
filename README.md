# Force Paste

**[Download the latest release APK](https://github.com/barlevalon/force-paste/releases/latest)** · **Android 7.0+**

A small, temporary keyboard that inserts clipboard text through Android’s keyboard input API instead of the Paste command. It may work around some paste-menu restrictions; apps can still reject, filter, or change the text.

## Install

1. Open the release link on your Android device. Under **Assets**, download `force-paste-1.0.0.apk` (or the APK for the latest version), not the source-code archive.
2. Open the downloaded APK. If Android asks, allow **Install unknown apps** for the browser or file manager you used. On Android 7, allow **Unknown sources** in Security settings instead. Names vary by device.
3. Tap **Install**, then open **Force Paste**. Turn off the installation permission afterward if no longer needed.

**Only install and enable keyboards you trust.** Android warns that keyboards can collect input because they can receive input-field context. Download from this repository’s releases, not an unverified mirror; avoid testing with passwords or other sensitive text.

**Already using a debug build?** The public release uses a different signing key, so Android will not install it over a debug build. First switch to your usual keyboard, uninstall the old Force Paste app, then install the release. **Uninstalling removes app data.** Enable Force Paste again afterward. Future updates signed with the same release key can install over the release without uninstalling.

## Paste, cancel, and switch back

1. Open Force Paste and tap **Enable Keyboard**, then enable **Force Paste** in Android’s keyboard settings. Keep your usual keyboard enabled.
2. Copy text, tap the destination field, and select **Force Paste** using Android’s keyboard switcher (often in the navigation bar or a keyboard notification).
3. Tap **PASTE CLIPBOARD**. While inserting, the button changes to **PASTING — TAP TO CANCEL**. Tap again to stop. Hiding the keyboard, switching keyboards, or changing fields also stops insertion. **Text already sent stays in the field.**
4. Check the result before retrying to avoid duplicates. Select your usual keyboard when finished; Force Paste is paste-only, not a replacement typing keyboard.

## Recover or remove

**No keyboard switcher?** Open Android Settings and find **Languages & input / On-screen keyboard** (names vary by device). Select your usual keyboard as the default/current keyboard. You can disable Force Paste from the keyboard-management screen.

**Nothing pasted, or only part arrived?** Copy plain text, tap the intended field again, and check its existing contents before retrying. Files and images are not supported, and some apps refuse keyboard input too.

To uninstall, first select your usual keyboard, then open **Settings → Apps → Force Paste → Uninstall**.

## Privacy and limitations

- Clipboard text is read only when you tap **PASTE CLIPBOARD**. A snapshot is kept during insertion and sent to the selected field; the receiving app gets that text.
- The app has no clipboard history, analytics, clipboard persistence, or network calls, and requests no Internet permission. This describes this project’s implementation, not arbitrary APKs, Android, or receiving apps.
- Only the first clipboard item’s inline text is used, without formatting. Files, images, and links stored only as URI items are not converted.
- Text is sent gradually. Emoji and accented text may behave differently between apps. A success message means text was sent, not that the app kept it unchanged. Always check the result.
- This is not physical key simulation or a guarantee of bypassing restrictions or being undetectable.

## Report a problem

[Open an issue](https://github.com/barlevalon/force-paste/issues) with your Android version/device, Force Paste version, receiving app/version, steps, and expected versus actual behavior. Use a short **invented, non-sensitive** sample such as `A😀B`. Remove passwords, tokens, account details, real clipboard contents, and unrelated text from screenshots or logs before sharing.

## Build or contribute

See [DEVELOPMENT.md](DEVELOPMENT.md) for source setup, debug builds, tests, and release signing.
