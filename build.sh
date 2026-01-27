#!/bin/bash
set -e
cd "$(dirname "$0")"

ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"

# Install Android SDK if missing
if [[ ! -d "$ANDROID_HOME/cmdline-tools/latest" ]]; then
    echo "Installing Android SDK..."
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    curl -sL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /tmp/tools.zip
    unzip -q /tmp/tools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
    rm /tmp/tools.zip
fi

export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

# Install required SDK components
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager --install "platform-tools" "platforms;android-34" "build-tools;34.0.0" 2>/dev/null

# Build
mise exec -- gradle assembleDebug --console=plain

# Copy APK to serve directory and start tailscale serve
mkdir -p dist
cp app/build/outputs/apk/debug/app-debug.apk dist/force-paste.apk

echo ""
echo "Starting tailscale serve..."
sudo tailscale serve --bg "$(pwd)/dist"
echo ""
sudo tailscale serve status
echo ""
echo "Download APK from: https://$(tailscale status --json | jq -r '.Self.DNSName' | sed 's/\.$//')/force-paste.apk"
