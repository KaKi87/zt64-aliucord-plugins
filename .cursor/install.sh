#!/usr/bin/env bash
# Idempotent Cloud Agent bootstrap for the Aliucord plugins build.
# Installs the Android SDK (if missing), points Gradle at it, and warms the
# Gradle dependency cache by building the plugins once.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

ANDROID_SDK_DIR="${ANDROID_SDK_ROOT:-$HOME/android-sdk}"
CMDLINE_TOOLS_VERSION="11076708"
COMPILE_SDK="android-36"
BUILD_TOOLS="36.0.0"

echo "==> Ensuring Android SDK at $ANDROID_SDK_DIR"
if [ ! -x "$ANDROID_SDK_DIR/cmdline-tools/latest/bin/sdkmanager" ]; then
    echo "==> Downloading Android command-line tools ($CMDLINE_TOOLS_VERSION)"
    tmp_dir="$(mktemp -d)"
    curl -fsSL -o "$tmp_dir/cmdline-tools.zip" \
        "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip"
    mkdir -p "$ANDROID_SDK_DIR/cmdline-tools"
    rm -rf "$ANDROID_SDK_DIR/cmdline-tools/latest"
    unzip -q "$tmp_dir/cmdline-tools.zip" -d "$ANDROID_SDK_DIR/cmdline-tools"
    mv "$ANDROID_SDK_DIR/cmdline-tools/cmdline-tools" "$ANDROID_SDK_DIR/cmdline-tools/latest"
    rm -rf "$tmp_dir"
else
    echo "==> Android command-line tools already present"
fi

export ANDROID_HOME="$ANDROID_SDK_DIR"
export ANDROID_SDK_ROOT="$ANDROID_SDK_DIR"
export PATH="$ANDROID_SDK_DIR/cmdline-tools/latest/bin:$PATH"

echo "==> Accepting SDK licenses and installing platform/build-tools"
yes | sdkmanager --licenses >/dev/null 2>&1 || true
sdkmanager "platform-tools" "platforms;$COMPILE_SDK" "build-tools;$BUILD_TOOLS" >/dev/null

echo "==> Writing local.properties"
echo "sdk.dir=$ANDROID_SDK_DIR" > "$REPO_ROOT/local.properties"

echo "==> Warming Gradle cache by building all plugins"
./gradlew --no-daemon make generateUpdaterJson

echo "==> Environment setup complete"
