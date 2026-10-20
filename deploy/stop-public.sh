#!/usr/bin/env bash
# =====================================================================
#  停止「内测服务 + 公网隧道」
#  用法：bash deploy/stop-public.sh [端口]      # 默认 8090
#
#  说明：start-public.sh 在终端里按 Ctrl+C 也能停。本脚本用于
#        「窗口已经关了 / 进程成了孤儿」的情况。
# =====================================================================
set -uo pipefail

PORT="${1:-${PORT:-8090}}"

echo "==> 停止监听端口 ${PORT} 的后端"
PIDS="$(netstat -ano 2>/dev/null | grep ":${PORT} " | grep LISTENING | awk '{print $5}' | sort -u || true)"
if [ -n "$PIDS" ]; then
  for pid in $PIDS; do
    taskkill //F //PID "$pid" >/dev/null 2>&1 && echo "    已停止 PID $pid" || echo "    停止 PID $pid 失败"
  done
else
  echo "    没有进程在监听 ${PORT}"
fi

echo "==> 停止 cloudflared 隧道"
CF_PIDS="$(tasklist 2>/dev/null | grep -i cloudflared | awk '{print $2}' || true)"
if [ -n "$CF_PIDS" ]; then
  for pid in $CF_PIDS; do
    taskkill //F //PID "$pid" >/dev/null 2>&1 && echo "    已停止 PID $pid" || echo "    停止 PID $pid 失败"
  done
else
  echo "    没有 cloudflared 在运行"
fi

echo ""
echo "✅ 已停止。"
