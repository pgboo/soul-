#!/usr/bin/env bash
# =====================================================================
#  把演示库 soul_social 的数据复制到测试库 soul_social_test
#  用途：每轮内测开始前，把测试环境重置成「有用户、有动态、有聊天记录」
#        的干净状态，测试者一进去就有东西可点，而不是面对空广场。
#  用法：bash deploy/seed-test-db.sh
#  注意：会覆盖测试库现有全部数据（演示库不受影响）。
#        想要一个完全空白、只有表结构的测试库，改用 deploy/init-test-db.sh
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC_DB="${SRC_DB:-soul_social}"
DST_DB="${DST_DB:-soul_social_test}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"
MYSQL_OPTS="-u${MYSQL_USER} -p${MYSQL_PASSWORD} --default-character-set=utf8mb4"

echo "==> 确认测试库存在"
mysql ${MYSQL_OPTS} -e \
  "CREATE DATABASE IF NOT EXISTS \`${DST_DB}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo "==> 先给当前测试库做一次备份（保险）"
bash "$ROOT/deploy/backup-db.sh" "$DST_DB" || echo "   （备份跳过）"

echo ""
echo "==> 复制 ${SRC_DB} → ${DST_DB}"
# mysqldump 默认带 DROP TABLE IF EXISTS，导入即整表重建，无需手动清库
mysqldump ${MYSQL_OPTS} --single-transaction --routines --triggers "${SRC_DB}" \
  | mysql ${MYSQL_OPTS} "${DST_DB}"

echo ""
echo "==> 同步上传的图片文件"
# 关键：数据库里的头像/动态图存的是 /upload/xxx 路径，指向的是 upload/ 目录。
# 测试实例读的是 upload-test/（见 application-test.yml），不同步就会出现「数据在、图裂了」。
SRC_UPLOAD="$ROOT/backend/upload"
DST_UPLOAD="$ROOT/backend/upload-test"
if [ -d "$SRC_UPLOAD" ]; then
  mkdir -p "$DST_UPLOAD"
  cp -rn "$SRC_UPLOAD"/. "$DST_UPLOAD"/ 2>/dev/null || true
  echo "    upload-test/ 现有 $(find "$DST_UPLOAD" -type f | wc -l) 个文件"
else
  echo "    （跳过：$SRC_UPLOAD 不存在）"
fi

echo ""
echo "==> 校验测试库数据量"
mysql ${MYSQL_OPTS} -N -B "${DST_DB}" -e "
SELECT CONCAT('   用户:        ', COUNT(*)) FROM user;
SELECT CONCAT('   用户资料:    ', COUNT(*)) FROM user_profile;
SELECT CONCAT('   动态:        ', COUNT(*)) FROM dynamic_content;
SELECT CONCAT('   动态图片:    ', COUNT(*)) FROM dynamic_media;
SELECT CONCAT('   IM 会话:     ', COUNT(*)) FROM im_conversation;
SELECT CONCAT('   IM 消息:     ', COUNT(*)) FROM im_message;
SELECT CONCAT('   标签:        ', COUNT(*)) FROM tag;
SELECT CONCAT('   话题:        ', COUNT(*)) FROM dynamic_topic;
"

echo ""
echo "✅ 测试库已填充演示数据。启动：bash deploy/run.sh test"
