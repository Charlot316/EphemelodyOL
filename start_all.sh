#!/bin/bash

# ============================================
# EphemelodyOL 全栈一键启动脚本
# 更新日期: 2026-02-04
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}================================================================${NC}"
echo -e "${BLUE}              EphemelodyOL 全栈启动程序${NC}"
echo -e "${BLUE}================================================================${NC}"

# 捕获退出信号以同时关闭前后端
cleanup() {
    echo -e "\n${YELLOW}正在停止服务...${NC}"
    # 尝试优雅地终止进程
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null
    fi
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
    fi
    echo -e "${GREEN}服务已停止。${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# 1. 启动后端
echo -e "${YELLOW}[1/2] 正在准备后端服务...${NC}"
if [ -f "$ROOT_DIR/back/rhythm_game/start.sh" ]; then
    cd "$ROOT_DIR/back/rhythm_game"
    # 后端脚本会进行编译并最终执行 java -jar，这里我们在后台运行
    # 使用 stdbuf 或类似的工具可能更好，但简单的重定向也可以
    ./start.sh > "$ROOT_DIR/back/backend.log" 2>&1 &
    BACKEND_PID=$!
    echo -e "${GREEN}✓ 后端已在后台启动 (PID: $BACKEND_PID)${NC}"
    echo -e "${BLUE}   日志文件: back/backend.log${NC}"
else
    echo -e "${RED}错误: 找不到后端启动脚本 back/rhythm_game/start.sh${NC}"
    exit 1
fi

# 2. 启动前端
echo -e "${YELLOW}[2/2] 正在启动前端服务...${NC}"
if [ -d "$ROOT_DIR/front" ]; then
    cd "$ROOT_DIR/front"
    # 导出必要的环境变量
    export NODE_OPTIONS=--openssl-legacy-provider
    # 启动前台，直接将输出显示在终端，或者后台运行
    npm run serve -- --port 8080 &
    FRONTEND_PID=$!
    echo -e "${GREEN}✓ 前端已在后台启动 (PID: $FRONTEND_PID)${NC}"
else
    echo -e "${RED}错误: 找不到前端目录 front/${NC}"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

echo -e "${BLUE}================================================================${NC}"
echo -e "${GREEN}全栈服务启动中!${NC}"
echo -e "${YELLOW}前端地址: http://localhost:8080${NC}"
echo -e "${YELLOW}后端地址: http://localhost:8090/api${NC}"
echo -e "${BLUE}您可以按 Ctrl+C 来停止所有服务。${NC}"
echo -e "${BLUE}提示: 如果后端卡在编译阶段，可以查看 back/backend.log${NC}"
echo -e "${BLUE}================================================================${NC}"

# 等待所有子进程
wait
