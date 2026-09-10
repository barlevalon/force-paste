#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"

if (( $# > 0 )); then
    echo "Usage: $0" >&2
    exit 2
fi

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"

if [[ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]]; then
    echo "Installing Android command-line tools from Google..."
    tools_tmp=$(mktemp -d)
    trap 'rm -rf -- "$tools_tmp"' EXIT
    curl --fail --location --show-error \
        https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
        --output "$tools_tmp/tools.zip"
    # Official archive SHA-1 from https://dl.google.com/android/repository/repository2-1.xml
    # Package cmdline-tools;12.0, Linux archive 11076708. Google publishes SHA-1 here.
    echo "d313adb7aedccf6cf0cfca51ec180f0059f5f8f8  $tools_tmp/tools.zip" | sha1sum --check --status
    unzip -q "$tools_tmp/tools.zip" -d "$tools_tmp"
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    if [[ -e "$ANDROID_HOME/cmdline-tools/latest" ]]; then
        echo "Incomplete tools at $ANDROID_HOME/cmdline-tools/latest; repair or move them before retrying." >&2
        exit 1
    fi
    mv "$tools_tmp/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
fi

export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

if [[ ! -f "$ANDROID_HOME/platforms/android-34/android.jar" || \
      ! -x "$ANDROID_HOME/build-tools/36.0.0/aapt2" || \
      ! -x "$ANDROID_HOME/platform-tools/adb" ]]; then
    echo "Review Android SDK licenses when prompted. Components install into $ANDROID_HOME."
    mise exec -- sdkmanager --sdk_root="$ANDROID_HOME" --licenses
    mise exec -- sdkmanager --sdk_root="$ANDROID_HOME" --install \
        "platform-tools" "platforms;android-34" "build-tools;36.0.0"
fi

mise exec -- gradle assembleDebug --console=plain
mkdir -p dist
cp app/build/outputs/apk/debug/app-debug.apk dist/force-paste.apk
printf '\nDebug APK: %s/dist/force-paste.apk\n' "$PWD"
