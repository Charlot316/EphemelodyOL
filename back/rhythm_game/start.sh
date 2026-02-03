#!/bin/bash

# ============================================
# EphemelodyOL 后端启动脚本
# 更新日期: 2026-02-03
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${PROJECT_ROOT}/../../.env"

# 参数处理
RUN_MIGRATION=false
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -m|--migrate) RUN_MIGRATION=true ;;
    esac
    shift
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  EphemelodyOL 后端启动程序${NC}"
echo -e "${BLUE}========================================${NC}"

# 加载环境变量
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}错误: 找不到 .env 文件 (路径: $ENV_FILE)${NC}"
    exit 1
fi

export $(cat "$ENV_FILE" | grep -v '^#' | xargs)

# 执行数据库迁移 (仅当指定了 -m 参数时)
if [ "$RUN_MIGRATION" = true ]; then
    if [ -f "${PROJECT_ROOT}/db_migration.sql" ]; then
        echo -e "${YELLOW}正在执行数据库更新...${NC}"
        
        DB_NAME=$(echo "$DB_URL" | sed -n 's/.*\/\([^?]*\).*/\1/p')
        DB_HOST=$(echo "$DB_URL" | sed -n 's/.*\/\/\([^:]*\).*/\1/p')
        DB_PORT=$(echo "$DB_URL" | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')
        [ -z "$DB_PORT" ] && DB_PORT="3306"
        
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -D"$DB_NAME" < "${PROJECT_ROOT}/db_migration.sql"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ 数据库已是最新状态${NC}"
        else
            echo -e "${RED}✗ 数据库更新失败${NC}"
            exit 1
        fi
    fi
fi

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到 Maven${NC}"
    exit 1
fi

# 编译项目
echo -e "${BLUE}正在快速编译并启动...${NC}"
cd "$PROJECT_ROOT"
mvn clean package -DskipTests

# 获取 JAR
JAR_FILE=$(find "${PROJECT_ROOT}/target" -name "*.jar" -not -name "*-sources.jar" | head -n 1)

# 启动
java -Xms512m -Xmx1024m \
     -Dspring.datasource.url="$DB_URL" \
     -Dspring.datasource.username="$DB_USERNAME" \
     -Dspring.datasource.password="$DB_PASSWORD" \
     -jar "$JAR_FILE"
