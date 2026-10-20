#!/usr/bin/env bash
# =====================================================================
#  配置 Cloudflare Named Tunnel（固定域名，永不变）
#
#  前置条件（只需做一次）：
#    1. 已有免费二级域名，并把 NS 指向 Cloudflare，且 Cloudflare 后台
#       该域名状态为 Active
#    2. 已执行过 cloudflared tunnel login 并在浏览器里授权了该域名
#
#  用法：bash deploy/cloudflare/setup-tunnel.sh soul-xxx.dpdns.org
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DOMAIN="${1:-}"
TUNNEL_NAME="${TUNNEL_NAME:-soul-social}"
CF_BIN="${CF_BIN:-/d/cloudflared-windows-amd64.exe}"
PORT="${PORT:-8090}"
CONF_DIR="$ROOT/deploy/cloudflare"
CONF="$CONF_DIR/config.yml"
CF_HOME="$HOME/.cloudflared"

if [ -z "$DOMAIN" ]; then
  echo "用法：bash deploy/cloudflare/setup-tunnel.sh <你的域名>" >&2
  echo "例：  bash deploy/cloudflare/setup-tunnel.sh soul-abc.dpdns.org" >&2
  exit 1
fi

if [ ! -f "$CF_HOME/cert.pem" ]; then
  echo "!! 还没登录 cloudflared。先执行：" >&2
  echo "   \"$CF_BIN\" tunnel login" >&2
  echo "   然后浏览器里选域名并点 Authorize。" >&2
  exit 1
fi

mkdir -p "$CONF_DIR"

# ---------- 1. 创建/复用隧道 ----------
echo "==> 创建/复用隧道：$TUNNEL_NAME"
if ! "$CF_BIN" tunnel list 2>/dev/null | awk 'NR>1{print $2}' | grep -qx "$TUNNEL_NAME"; then
  "$CF_BIN" tunnel create "$TUNNEL_NAME"
else
  echo "    隧道已存在，复用"
fi

TUNNEL_ID="$("$CF_BIN" tunnel list 2>/dev/null | awk -v n="$TUNNEL_NAME" 'NR>1 && $2==n {print $1}' | head -1)"
if [ -z "$TUNNEL_ID" ]; then
  echo "!! 拿不到隧道 ID，请检查 cloudflared tunnel list 输出" >&2
  exit 1
fi
echo "    隧道 ID：$TUNNEL_ID"

CRED_BASH="$CF_HOME/${TUNNEL_ID}.json"
if [ ! -f "$CRED_BASH" ]; then
  echo "!! 找不到凭据文件：$CRED_BASH" >&2
  exit 1
fi
CRED_WIN="$(cygpath -w "$CRED_BASH" 2>/dev/null || echo "$CRED_BASH")"

# ---------- 2. 写配置 ----------
echo "==> 写入配置：$CONF"
cat > "$CONF" <<EOF
# Cloudflare Named Tunnel 配置（由 setup-tunnel.sh 生成）
# 固定域名，重启不变，无需重新打包 APK
tunnel: ${TUNNEL_ID}
credentials-file: ${CRED_WIN}

# 走 HTTP/2（TCP 443）。默认的 QUIC(UDP 7844) 在国内网络下
# 容易被 QoS 干扰，实测会持续 "no recent network activity" 直到
# 隧道被 Cloudflare 服务端回收。
protocol: http2

ingress:
  - hostname: ${DOMAIN}
    service: http://localhost:${PORT}
    originRequest:
      connectTimeout: 30s
  - service: http_status:404
EOF

# ---------- 3. 路由 DNS ----------
echo "==> 把 $DOMAIN 路由到隧道 ..."
ROUTE_OUT="$("$CF_BIN" tunnel route dns --overwrite-dns "$TUNNEL_NAME" "$DOMAIN" 2>&1)" && {
  echo "$ROUTE_OUT"
} || {
  echo "$ROUTE_OUT" >&2
  echo "" >&2
  echo "!! DNS 路由失败。" >&2
  echo "   最常见原因：域名还没在 Cloudflare 生效（状态不是 Active）。" >&2
  echo "   到 Cloudflare 后台确认域名状态，NS 生效后再重跑本脚本。" >&2
  exit 1
}

echo ""
echo "================================================================"
echo "  ✅ 配置完成"
echo ""
echo "  隧道名：  $TUNNEL_NAME"
echo "  隧道 ID： $TUNNEL_ID"
echo "  固定域名：https://$DOMAIN"
echo "  回源地址：http://localhost:$PORT"
echo ""
echo "  下一步："
echo "    1) 前台试跑：bash deploy/cloudflare/run-tunnel.sh"
echo "    2) 确认可用后装成计划任务："
echo "       右键「以管理员身份运行」deploy\\windows\\install-tasks.cmd"
echo "================================================================"
