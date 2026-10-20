#!/usr/bin/env bash
# =====================================================================
#  初始化「测试专用库」：soul_social_test
#  复用 sql/schema.sql（表结构 + 种子数据），自动把库名改成测试库，
#  这样以后 schema.sql 更新了，重跑本脚本就能同步，不会两边不一致。
#  用法：bash deploy/init-test-db.sh
# =====================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DB="${1:-soul_social_test}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"
MYSQL="mysql -u${MYSQL_USER} -p${MYSQL_PASSWORD} --default-character-set=utf8mb4"

echo "==> [1/3] 创建数据库 ${DB}"
${MYSQL} -e "CREATE DATABASE IF NOT EXISTS \`${DB}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo "==> [2/3] 导入表结构 + 种子数据"
echo "    （去掉 schema.sql 里写死的 CREATE DATABASE / USE，改由 --database 指定）"
sed -e '/^CREATE DATABASE IF NOT EXISTS soul_social/,+1d' \
    -e '/^USE soul_social;/d' \
    "${ROOT}/sql/schema.sql" | ${MYSQL} "${DB}"

echo "==> [3/3] 校验"
${MYSQL} -N -B "${DB}" -e \
  "SELECT CONCAT('   表数量: ', COUNT(*)) FROM information_schema.tables WHERE table_schema='${DB}';"
${MYSQL} -N -B "${DB}" -e \
  "SELECT CONCAT('   标签: ', (SELECT COUNT(*) FROM tag), ' 个 / 分类: ', (SELECT COUNT(*) FROM tag_category), ' 个');"
${MYSQL} -N -B "${DB}" -e \
  "SELECT CONCAT('   用户: ', COUNT(*), ' 个') FROM user;"

echo ""
echo "✅ 测试库 ${DB} 就绪。启动：bash deploy/run.sh test"
