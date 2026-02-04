#!/bin/bash

# ============================================
# EphemelodyOL 后端启动脚本
# 更新日期: 2026-02-04 (R2 版)
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

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  EphemelodyOL 后端启动程序 (R2 Sync)${NC}"
echo -e "${BLUE}========================================${NC}"

# 1. 检查环境变量
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}错误: 找不到 .env 文件 (路径: $ENV_FILE)${NC}"
    exit 1
fi

echo -e "${YELLOW}正在加载环境变量...${NC}"
# 使用 export 加载环境变量以便 Java 读取
set -a
source "$ENV_FILE"
set +a

# 2. 检查系统依赖 (ffmpeg & webp)
echo -e "${YELLOW}正在检查系统依赖...${NC}"
MISSING_DEPS=0

if ! command -v ffmpeg &> /dev/null; then
    echo -e "${RED}✗ 警告: 未找到 ffmpeg (视频压缩将不可用)${NC}"
    MISSING_DEPS=1
else
    echo -e "${GREEN}✓ 找到 ffmpeg${NC}"
fi

if ! command -v cwebp &> /dev/null; then
    echo -e "${RED}✗ 警告: 未找到 cwebp (图片转换 WebP 将不可用)${NC}"
    MISSING_DEPS=1
else
    echo -e "${GREEN}✓ 找到 cwebp${NC}"
fi

if [ $MISSING_DEPS -eq 1 ]; then
    echo -e "${YELLOW}提示: 如果你在 macOS 上，请运行: brew install ffmpeg webp${NC}"
    echo -e "${YELLOW}提示: 如果你在 Linux 上，请运行: apt-get install -y ffmpeg webp${NC}"
    echo -e "${YELLOW}或者更简单的方法，直接使用我在根目录为你创建的 Dockerfile 进行部署。${NC}"
fi

# 3. 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到 Maven，请安装后再试。${NC}"
    exit 1
fi

# 4. 编译并启动
echo -e "${BLUE}正在快速编译并启动...${NC}"
cd "$PROJECT_ROOT"
mvn clean package -DskipTests

# 获取 JAR 文件路径
JAR_FILE=$(find "${PROJECT_ROOT}/target" -name "*.jar" -not -name "*-sources.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}错误: 未找到生成的 JAR 文件${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 启动中...${NC}"
# 直接启动，Spring Boot 会自动读取已 Export 的环境变量
java -Xms512m -Xmx1024m -jar "$JAR_FILE"
