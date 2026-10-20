#!/usr/bin/env bash
# =====================================================================
#  一键启动「内测服务 + 公网隧道」
#
#  为什么要有这个脚本：
#  由 AI 会话启动的后台服务是绑在会话上的，会话一结束就可能被回收，
#  测试者会突然连不上（报 network error）。请在【你自己的终端】里跑本
#  脚本，进程归你所有，只要这个终端窗口不关、电脑不关机，服务就一直活着。
#
#  用法：bash deploy/start-public.sh
#        bash deploy/start-public.sh 8090      # 指定端口
#
#  停止：在终端里按 Ctrl+C（会同时停掉后端和隧道）
#        或另开终端执行：bash deploy/stop-public.sh
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PORT="${1:-${PORT:-8090}}"
PROFILE="${PROFILE:-test}"
CF_BIN="${CF_BIN:-/d/cloudflared-windows-amd64.exe}"
LOG_DIR="$ROOT/logs"
BACKEND_LOG="$LOG_DIR/public-backend.log"
TUNNEL_LOG="$LOG_DIR/public-tunnel.log"

mkdir -p "$LOG_DIR"

cleanup() {
  echo ""
  echo "==> 正在停止 ..."
  [ -n "${TUNNEL_PID:-}" ] && kill "$TUNNEL_PID" 2>/dev/null || true
  [ -n "${BACKEND_PID:-}" ] && kill "$BACKEND_PID" 2>/dev/null || true
  echo "    已停止。"
}
trap cleanup EXIT INT TERM

# ---------- 1. 后端 ----------
if netstat -ano 2>/dev/null | grep -q ":${PORT} .*LISTENING"; then
  echo "==> 端口 ${PORT} 已在监听，跳过后端启动（如需重启请先停掉旧进程）"
else
  echo "==> 启动后端  profile=${PROFILE}  port=${PORT}"
  bash "$ROOT/deploy/run.sh" "$PROFILE" "$PORT" > "$BACKEND_LOG" 2>&1 &
  BACKEND_PID=$!
  echo "    日志：$BACKEND_LOG"
  printf "    等待启动 "
  for _ in $(seq 1 30); do
    sleep 1
    printf "."
    if netstat -ano 2>/dev/null | grep -q ":${PORT} .*LISTENING"; then
      printf " 就绪\n"
      break
    fi
  done
fi

# ---------- 2. 隧道 ----------
if [ ! -x "$CF_BIN" ] && [ ! -f "$CF_BIN" ]; then
  echo "!! 找不到 cloudflared：$CF_BIN" >&2
  echo "   下载：https://github.com/cloudflare/cloudflared/releases" >&2
  echo "   或用环境变量指定：CF_BIN=/path/to/cloudflared bash deploy/start-public.sh" >&2
  exit 1
fi

echo ""
echo "==> 启动公网隧道 -> http://localhost:${PORT}"
# --protocol http2：强制走 TCP 443。默认的 QUIC(UDP 7844) 在国内网络下
# 容易被 QoS 干扰，实测会持续 "no recent network activity" 直到隧道被
# Cloudflare 服务端回收；TCP 443 稳得多。
# --edge-ip-version 4：强制只连 IPv4 边缘节点。
#   本机有运营商 IPv6 地址但**没有 IPv6 国际路由**，cloudflared 会优先
#   选中 IPv6 边缘节点然后全部 dial 失败
#   （"dial tcp [2606:...]:7844: unreachable host"），表现为 530。
#   加这个参数后实测 5/5 稳定 200。
"$CF_BIN" tunnel --url "http://localhost:${PORT}" --no-autoupdate \
  --protocol http2 --edge-ip-version 4 > "$TUNNEL_LOG" 2>&1 &
TUNNEL_PID=$!

echo "    等待分配公网域名 ..."
URL=""
for _ in $(seq 1 40); do
  sleep 1
  URL="$(tr -d '\r' < "$TUNNEL_LOG" | grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' | head -1 || true)"
  [ -n "$URL" ] && break
done

echo ""
echo "================================================================"
if [ -n "$URL" ]; then
  echo "  ✅ 内测地址：$URL"
  echo ""
  echo "  ⚠️  这个域名是临时的：电脑关机/重启、网络切换、或本脚本重启"
  echo "     都会换一个新域名。届时："
  echo "       • 网页链接：直接把新地址发给测试者"
  echo "       • 手机 App：让测试者在登录页点「连不上服务器？点这里改地址」，"
  echo "                   把新地址粘进去即可，**不用重新装包**"
  echo ""
  echo "     （如果确实要出一个内置新地址的包，再执行："
  echo "       bash deploy/build-apk.sh $URL ）"
else
  echo "  ⚠️  没抓到域名，请查看日志：$TUNNEL_LOG"
fi
echo "================================================================"
echo ""
echo "保持本窗口打开。按 Ctrl+C 停止服务。"

wait
