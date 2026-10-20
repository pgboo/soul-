#!/usr/bin/env bash
# =====================================================================
# 安装 Android SDK 命令行工具链（一次性，约 600MB 下载）
#
#   产物目录：C:\Users\<用户名>\AppData\Local\Android\Sdk
#   装完即可运行 deploy/build-apk.sh 打 APK
#
#   用法：bash deploy/setup-android-sdk.sh
# =====================================================================
set -euo pipefail

SDK_ROOT_UNIX="${ANDROID_SDK_ROOT:-/c/Users/WWb/AppData/Local/Android/Sdk}"
SDK_ROOT_WIN="${ANDROID_SDK_ROOT_WIN:-C:\\Users\\WWb\\AppData\\Local\\Android\\Sdk}"
JAVA_HOME_WIN="${JAVA_HOME_WIN:-C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.11.10-hotspot}"
PY="${PY:-C:/Users/WWb/.workbuddy-ai/binaries/python/versions/3.13.12/python.exe}"
CMDLINE_URL="https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
TMP_DIR="$SDK_ROOT_UNIX/.tmp-cmdline"

export JAVA_HOME="$JAVA_HOME_WIN"
export ANDROID_HOME="$SDK_ROOT_WIN"
export ANDROID_SDK_ROOT="$SDK_ROOT_WIN"

echo "==> SDK 目录: $SDK_ROOT_WIN"
mkdir -p "$SDK_ROOT_UNIX"

SDKMGR="$SDK_ROOT_UNIX/cmdline-tools/latest/bin/sdkmanager.bat"

if [ -f "$SDKMGR" ]; then
  echo "==> cmdline-tools 已存在，跳过下载"
else
  echo "==> 下载 commandline-tools ..."
  rm -rf "$TMP_DIR"; mkdir -p "$TMP_DIR"
  curl -fL --connect-timeout 20 -o "$TMP_DIR/cmdline-tools.zip" "$CMDLINE_URL"
  echo "==> 解压 ..."
  "$PY" -c "import sys,zipfile; zipfile.ZipFile(sys.argv[1]).extractall(sys.argv[2])" "$TMP_DIR/cmdline-tools.zip" "$TMP_DIR"
  mkdir -p "$SDK_ROOT_UNIX/cmdline-tools"
  rm -rf "$SDK_ROOT_UNIX/cmdline-tools/latest"
  mv "$TMP_DIR/cmdline-tools" "$SDK_ROOT_UNIX/cmdline-tools/latest"
  rm -rf "$TMP_DIR"
fi

echo "==> 接受 licenses ..."
yes | "$SDKMGR" --licenses > /dev/null 2>&1 || true

echo "==> 安装 platform-tools / platforms;android-36 / build-tools;36.0.0 ..."
# Capacitor 8 要求 compileSdk 36，见 frontend-h5/android/variables.gradle
"$SDKMGR" "platform-tools" "platforms;android-36" "build-tools;36.0.0"

echo ""
echo "==> 完成。SDK 内容:"
ls "$SDK_ROOT_UNIX"
echo ""
echo "构建 APK 前请设置环境变量:"
echo "  export ANDROID_HOME='$SDK_ROOT_WIN'"
