#!/bin/bash

# 加载环境变量
export $(cat .env | grep -v '^#' | xargs)

# 0. 清理旧进程
echo "======== Cleaning up old processes ========"
PID=$(lsof -t -i:8090)
if [ -n "$PID" ]; then
    echo "Killing process on port 8090 (PID: $PID)..."
    kill -9 $PID
else
    echo "No process running on port 8090."
fi

# 1. 编译前端
echo "======== Building Frontend (Vue) ========"
cd front
export NODE_OPTIONS=--openssl-legacy-provider
npm install
npm run build
if [ $? -ne 0 ]; then
    echo "Frontend build failed!"
    exit 1
fi
cd ..

# 2. 清理后端静态资源目录
echo "======== Cleaning Backend Static Resource ========"
STATIC_DIR="./back/rhythm_game/src/main/resources/static"
rm -rf "$STATIC_DIR"/*
mkdir -p "$STATIC_DIR"

# 3. 复制前端构建产物到后端
echo "======== Copying Frontend Dist to Backend ========"
cp -r front/dist/* "$STATIC_DIR"

# 4. 编译后端
echo "======== Building Backend ========"
cd back/rhythm_game
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed!"
    exit 1
fi

# 5. 启动服务
echo "======== Starting Service ========"
echo "Application will start at http://localhost:8090"
java -jar target/*.jar \
  -Dspring.datasource.url=$DB_URL \
  -Dspring.datasource.username=$DB_USERNAME \
  -Dspring.datasource.password=$DB_PASSWORD
