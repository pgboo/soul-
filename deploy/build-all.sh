#!/usr/bin/env bash
# =====================================================================
#  一键构建「单端口部署包」
#  前端构建 → 后端打包（H5 产物自动打进 jar 的 BOOT-INF/classes/static/）
#  产物：backend/target/soul-social-backend-1.0.0.jar
#  这个 jar 单独跑起来，8080 就同时提供页面 + /api + /upload，
#  内网穿透只需映射一个端口。
#  用法：bash deploy/build-all.sh
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$ROOT/backend/target/soul-social-backend-1.0.0.jar"
JAVA_BIN="${JAVA_BIN:-/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot/bin/java.exe}"

echo "==> [1/3] 构建 H5 前端"
cd "$ROOT/frontend-h5"
npm run build

if [ ! -f "$ROOT/frontend-h5/dist/index.html" ]; then
  echo "!! 前端构建失败：dist/index.html 不存在" >&2
  exit 1
fi
echo "    dist 产物：$(ls "$ROOT/frontend-h5/dist" | tr '\n' ' ')"

echo ""
echo "==> [2/3] 打包后端（自动把 dist 复制进 jar 的 static/）"
cd "$ROOT/backend"
MVN_PROJECT_DIR="$(cd "$ROOT/backend" && pwd -W)" bash "$ROOT/deploy/_mvn.sh" -B -DskipTests clean package

echo ""
echo "==> [3/3] 校验产物"
ls -lh "$JAR"

JAR_TOOL="$(dirname "$JAVA_BIN")/jar.exe"
if [ -x "$JAR_TOOL" ]; then
  N=$("$JAR_TOOL" tf "$JAR" | grep -c "BOOT-INF/classes/static/" || true)
  echo "    jar 内 static/ 文件数：$N"
  if [ "$N" -lt 2 ]; then
    echo "!! 警告：jar 里几乎没有静态资源，前端可能没打进去" >&2
    exit 1
  fi
  echo "    index.html 是否在包内：$("$JAR_TOOL" tf "$JAR" | grep -c "static/index.html" || true)"
fi

echo ""
echo "✅ 构建完成：$JAR"
echo "   启动：bash deploy/run.sh test"
