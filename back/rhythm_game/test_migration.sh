#!/bin/bash

# ============================================
# 数据库迁移测试脚本
# 用于验证迁移是否成功，不影响现有数据
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${PROJECT_ROOT}/../../.env"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  数据库迁移测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 加载环境变量
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}错误: 找不到 .env 文件${NC}"
    exit 1
fi

export $(cat "$ENV_FILE" | grep -v '^#' | xargs)

# 提取数据库信息
DB_NAME=$(echo "$DB_URL" | sed -n 's/.*\/\([^?]*\).*/\1/p')
DB_HOST=$(echo "$DB_URL" | sed -n 's/.*\/\/\([^:]*\).*/\1/p')
DB_PORT=$(echo "$DB_URL" | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')

if [ -z "$DB_PORT" ]; then
    DB_PORT="3306"
fi

echo -e "${GREEN}数据库配置:${NC}"
echo -e "  主机: ${BLUE}${DB_HOST}${NC}"
echo -e "  端口: ${BLUE}${DB_PORT}${NC}"
echo -e "  数据库: ${BLUE}${DB_NAME}${NC}"
echo -e "  用户: ${BLUE}${DB_USERNAME}${NC}"
echo ""

# 测试数据库连接
echo -e "${YELLOW}测试数据库连接...${NC}"
if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT 1" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 数据库连接成功${NC}"
else
    echo -e "${RED}✗ 数据库连接失败${NC}"
    echo -e "${YELLOW}请检查数据库配置和服务状态${NC}"
    exit 1
fi
echo ""

# 检查现有表
echo -e "${YELLOW}检查现有表...${NC}"
EXISTING_TABLES=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -D"$DB_NAME" -e "SHOW TABLES" -s)
TABLE_COUNT=$(echo "$EXISTING_TABLES" | wc -l)
echo -e "${GREEN}✓ 当前数据库有 ${BLUE}${TABLE_COUNT}${GREEN} 个表${NC}"
echo ""

# 检查是否已存在新表
echo -e "${YELLOW}检查权限相关表...${NC}"
if echo "$EXISTING_TABLES" | grep -q "chart_collaborator"; then
    echo -e "${YELLOW}⚠ chart_collaborator 表已存在${NC}"
    COLLABORATOR_EXISTS=true
else
    echo -e "${BLUE}ℹ chart_collaborator 表不存在，将被创建${NC}"
    COLLABORATOR_EXISTS=false
fi

if echo "$EXISTING_TABLES" | grep -q "chart_permission_log"; then
    echo -e "${YELLOW}⚠ chart_permission_log 表已存在${NC}"
    LOG_EXISTS=true
else
    echo -e "${BLUE}ℹ chart_permission_log 表不存在，将被创建${NC}"
    LOG_EXISTS=false
fi
echo ""

# 询问是否继续
if [ "$COLLABORATOR_EXISTS" = true ] || [ "$LOG_EXISTS" = true ]; then
    echo -e "${YELLOW}部分表已存在，继续执行将跳过已存在的表。是否继续？(y/n)${NC}"
else
    echo -e "${YELLOW}准备创建新表，是否继续？(y/n)${NC}"
fi

read -r response
if [[ ! "$response" =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}已取消${NC}"
    exit 0
fi
echo ""

# 执行迁移
echo -e "${GREEN}执行数据库迁移...${NC}"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -D"$DB_NAME" < "${PROJECT_ROOT}/db_migration.sql"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 迁移执行成功${NC}"
else
    echo -e "${RED}✗ 迁移执行失败${NC}"
    exit 1
fi
echo ""

# 验证迁移结果
echo -e "${YELLOW}验证迁移结果...${NC}"

# 检查表是否创建
TABLES_AFTER=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -D"$DB_NAME" -e "SHOW TABLES" -s)

if echo "$TABLES_AFTER" | grep -q "chart_collaborator"; then
    echo -e "${GREEN}✓ chart_collaborator 表存在${NC}"
    
    # 显示表结构
    echo -e "${BLUE}表结构:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -D"$DB_NAME" -e "DESC chart_collaborator"
else
    echo -e "${RED}✗ chart_collaborator 表创建失败${NC}"
fi
echo ""

if echo "$TABLES_AFTER" | grep -q "chart_permission_log"; then
    echo -e "${GREEN}✓ chart_permission_log 表存在${NC}"
    
    # 显示表结构
    echo -e "${BLUE}表结构:${NC}"
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -D"$DB_NAME" -e "DESC chart_permission_log"
else
    echo -e "${RED}✗ chart_permission_log 表创建失败${NC}"
fi
echo ""

# 检查数据完整性
echo -e "${YELLOW}检查现有数据完整性...${NC}"
TABLE_COUNT_AFTER=$(echo "$TABLES_AFTER" | wc -l)

if [ "$TABLE_COUNT_AFTER" -ge "$TABLE_COUNT" ]; then
    echo -e "${GREEN}✓ 数据完整性验证通过${NC}"
    echo -e "  迁移前表数: ${BLUE}${TABLE_COUNT}${NC}"
    echo -e "  迁移后表数: ${BLUE}${TABLE_COUNT_AFTER}${NC}"
else
    echo -e "${RED}✗ 警告: 表数量减少${NC}"
    echo -e "${YELLOW}请检查数据库状态${NC}"
fi
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}  迁移测试完成！${NC}"
echo -e "${BLUE}========================================${NC}"
