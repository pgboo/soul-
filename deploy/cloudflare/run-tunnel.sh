#!/usr/bin/env bash
# 前台运行 Named Tunnel（调试用；正式请用 Windows 计划任务）
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CF_BIN="${CF_BIN:-/d/cloudflared-windows-amd64.exe}"
CONF="$ROOT/deploy/cloudflare/config.yml"

if [ ! -f "$CONF" ]; then
  echo "!! 未找到 $CONF" >&2
  echo "   请先执行：bash deploy/cloudflare/setup-tunnel.sh <你的域名>" >&2
  exit 1
fi

echo "==> 启动隧道（Ctrl+C 停止）"
# edge-ip-version 4 的原因见 start-public.sh 注释（运营商 IPv6 无国际路由）
exec "$CF_BIN" tunnel --config "$CONF" --no-autoupdate \
  --protocol http2 --edge-ip-version 4 run
