# Force Paste

A minimal Android keyboard that types clipboard contents character-by-character, bypassing paste restrictions in apps that block clipboard access.

## Setup

### 1. Install Android SDK

```bash
# Install mise if not already installed
curl https://mise.run | sh

# Install Java (mise will read .mise.toml)
mise install

# Install Android command-line tools
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

# Add to PATH (add to .bashrc/.zshrc)
export ANDROID_HOME=~/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Accept licenses and install build tools
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 2. Build

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or transfer the APK to your phone and install manually.

## Usage

1. Open the app and tap "Enable Keyboard"
2. Enable "Force Paste" in system keyboard settings
3. When you need to paste in a restricted app:
   - Copy text to clipboard normally
   - Tap the text field
   - Switch keyboard to "Force Paste" (usually via keyboard icon in nav bar)
   - Tap "PASTE CLIPBOARD"
   - Switch back to your normal keyboard

## How it works

Instead of using the system paste function (which apps can block), this keyboard reads the clipboard and "types" each character individually using `InputConnection.commitText()`. This appears as manual typing to the app, bypassing paste restrictions.

Characters are typed with a 5ms delay between them to avoid detection.
