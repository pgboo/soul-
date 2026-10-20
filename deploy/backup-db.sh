#!/usr/bin/env bash
# =====================================================================
#  数据库备份（内测期建议每天跑一次）
#  用法：bash deploy/backup-db.sh                    # 备份 soul_social
#        bash deploy/backup-db.sh soul_social_test   # 备份测试库
#        bash deploy/backup-db.sh all                # 两个都备份
#  产物：.backup/<库名>_<时间戳>.sql.gz，自动保留最近 14 份
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_DIR="$ROOT/.backup"
KEEP=14
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"

mkdir -p "$BACKUP_DIR"

backup_one() {
  local db="$1"
  local ts out
  ts="$(date +%Y%m%d-%H%M%S)"
  out="$BACKUP_DIR/${db}_${ts}.sql"

  # --single-transaction: 不锁表；--routines/--triggers: 带上存储过程与触发器
  if ! mysqldump -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
        --default-character-set=utf8mb4 \
        --single-transaction --routines --triggers \
        "$db" > "$out" 2>/dev/null; then
    echo "!! 备份失败：$db（库不存在或密码不对）" >&2
    rm -f "$out"
    return 1
  fi

  gzip -f "$out"
  local size
  size="$(du -h "${out}.gz" | cut -f1)"
  echo "   ✅ ${db} → ${out}.gz (${size})"

  # 只保留最近 $KEEP 份
  ls -1t "$BACKUP_DIR"/"${db}"_*.sql.gz 2>/dev/null | tail -n +$((KEEP + 1)) | while read -r old; do
    rm -f "$old"
    echo "   🗑  清理旧备份：$(basename "$old")"
  done
}

TARGET="${1:-soul_social}"
echo "==> 备份到 $BACKUP_DIR"

if [ "$TARGET" = "all" ]; then
  backup_one soul_social || true
  backup_one soul_social_test || true
else
  backup_one "$TARGET"
fi

echo ""
echo "当前备份文件："
ls -1t "$BACKUP_DIR"/*.sql.gz 2>/dev/null | head -5 | sed 's/^/   /' || echo "   （无）"
