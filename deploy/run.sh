#!/usr/bin/env bash
# =====================================================================
#  启动单端口服务
#  用法：bash deploy/run.sh [profile] [port]
#        bash deploy/run.sh              # 默认 test + 8090（对外内测，独立测试库）
#        bash deploy/run.sh test 8090    # 同上，显式写法
#        bash deploy/run.sh dev 8080     # 正式演示库，跑在 8080
#  说明：测试实例默认用 8090，避免和 8080 的开发后端抢端口。
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROFILE="${1:-test}"
PORT="${2:-8090}"
JAR="$ROOT/backend/target/soul-social-backend-1.0.0.jar"
JAVA_BIN="${JAVA_BIN:-/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot/bin/java.exe}"

if [ ! -f "$JAR" ]; then
  echo "!! 未找到 $JAR" >&2
  echo "   请先执行：bash deploy/build-all.sh" >&2
  exit 1
fi

echo "==> 启动 soul-social  profile=${PROFILE}  port=${PORT}"
echo "    页面 + 接口同源：http://localhost:${PORT}/"
echo "    管理后台不在这个 jar 里，对外测试请勿暴露 5174"
echo ""

cd "$ROOT/backend"
exec "$JAVA_BIN" -jar "$JAR" \
  --spring.profiles.active="${PROFILE}" \
  --server.port="${PORT}"
