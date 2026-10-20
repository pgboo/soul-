#!/usr/bin/env bash
# =====================================================================
# 一键打包 Android APK
#
#   用法：bash deploy/build-apk.sh <后端公网地址>
#   例：  bash deploy/build-apk.sh https://soul.abc123.trycloudflare.com
#
#   产物：dist-apk/soul-social-<时间戳>.apk
#
#   重要：APK 会把后端地址编译进去。地址变了必须重新执行本脚本，
#         否则 App 内所有接口和图片都会失败。
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
H5="$ROOT/frontend-h5"
ANDROID_DIR="$H5/android"
OUT_DIR="$ROOT/dist-apk"
SDK_WIN="${ANDROID_SDK_WIN:-C:\\Users\\WWb\\AppData\\Local\\Android\\Sdk}"
JAVA_UNIX="${JAVA_HOME_UNIX:-/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot}"

API_BASE="${1:-}"
if [ -z "$API_BASE" ]; then
  echo "错误：必须传入后端公网地址（APK 内所有接口都指向它）。"
  echo ""
  echo "用法：bash deploy/build-apk.sh https://你的域名"
  echo "例：  bash deploy/build-apk.sh https://soul.abc123.trycloudflare.com"
  exit 1
fi
API_BASE="${API_BASE%/}"

if [ ! -f "$ANDROID_DIR/gradlew" ]; then
  echo "错误：未找到 Android 工程。请先执行："
  echo "  cd frontend-h5 && npx cap add android"
  exit 1
fi

# Gradle wrapper 是 POSIX 脚本，JAVA_HOME 必须是 Unix 风格路径
export JAVA_HOME="$JAVA_UNIX"

# SDK 位置通过 local.properties 传给 AGP（比环境变量可靠）
cat > "$ANDROID_DIR/local.properties" <<EOF
sdk.dir=$(printf '%s' "$SDK_WIN" | sed 's/\\/\\\\/g; s/:/\\:/g')
EOF

echo "==> 后端地址 : $API_BASE"
echo "==> SDK 目录 : $SDK_WIN"
echo ""

echo "==> [1/4] 构建 H5（注入后端地址）..."
cd "$H5"
VITE_API_BASE="$API_BASE" CODEBUDDY_SAFE_DELETE_ENABLED=0 npm run build

if ! grep -rq "$API_BASE" dist/assets/*.js 2>/dev/null; then
  echo "错误：后端地址未成功注入构建产物，请检查 VITE_API_BASE 是否生效。"
  exit 1
fi
echo "    已确认地址写入产物"

echo "==> [2/4] 同步资源到 Android 工程 ..."
CODEBUDDY_SAFE_DELETE_ENABLED=0 npx cap sync android

echo "==> [3/4] Gradle 构建 release APK（首次会下载 Gradle，约 200MB）..."
cd "$ANDROID_DIR"
./gradlew assembleRelease --no-daemon

echo "==> [4/4] 收集产物 ..."
APK_SRC="$ANDROID_DIR/app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_SRC" ]; then
  echo "错误：未生成 APK，请查看上面的 Gradle 输出。"
  exit 1
fi
mkdir -p "$OUT_DIR"
STAMP="$(date +%Y%m%d-%H%M)"
APK_DST="$OUT_DIR/soul-social-$STAMP.apk"
cp "$APK_SRC" "$APK_DST"

# 记录本次 APK 绑定的后端地址，便于日后排查
printf '构建时间: %s\n后端地址: %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$API_BASE" > "$OUT_DIR/soul-social-$STAMP.txt"

echo ""
echo "==> 完成"
ls -lh "$APK_DST"
echo ""
echo "把这个文件发给测试者即可（微信里不能直接点，需用浏览器打开）。"
