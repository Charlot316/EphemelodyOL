# 🚀 EphemelodyOL 后端部署指南

## 📋 前置要求

- ✅ Java 8 或更高版本
- ✅ Maven 3.6+
- ✅ MySQL 5.7+ 或 8.0+
- ✅ 已配置好的 `.env` 文件（项目根目录）

## 🔧 环境配置

### 1. 检查 `.env` 文件

确保项目根目录下的 `.env` 文件包含以下配置：

```bash
# MySQL Database Configuration
DB_URL="jdbc:mysql://localhost:3306/rhythm_game?useSSL=false&useUnicode=true&characterEncoding=utf-8&severTimezone=GMT%2B8&allowPublicKeyRetrieval=true"
DB_USERNAME="root"
DB_PASSWORD="Ephemelody@2026"
```

### 2. 数据库准备

确保 MySQL 服务正在运行，并且 `rhythm_game` 数据库已创建：

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS rhythm_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出
exit;
```

## 🎯 快速启动

### 方式一：使用启动脚本（推荐）

```bash
cd back/rhythm_game
./start.sh
```

启动脚本会自动：
1. ✅ 加载环境变量
2. ✅ 询问是否执行数据库迁移
3. ✅ 编译项目
4. ✅ 启动应用

### 方式二：手动启动

#### 步骤 1: 执行数据库迁移

```bash
# 在 back/rhythm_game 目录下
mysql -u root -p < db_migration.sql
```

输入密码后，脚本会创建以下新表：
- `chart_collaborator` - 谱面协作者表
- `chart_permission_log` - 权限操作日志表

**注意：** 此脚本使用 `CREATE TABLE IF NOT EXISTS`，不会影响现有数据。

#### 步骤 2: 编译项目

```bash
cd back/rhythm_game

# 加载环境变量
export $(cat ../../.env | grep -v '^#' | xargs)

# 编译（跳过测试）
mvn clean package -DskipTests
```

#### 步骤 3: 启动应用

```bash
# 使用环境变量启动
java -jar target/*.jar \
  -Dspring.datasource.url=$DB_URL \
  -Dspring.datasource.username=$DB_USERNAME \
  -Dspring.datasource.password=$DB_PASSWORD
```

## 📊 验证部署

### 1. 检查应用是否启动

访问健康检查端点（如果有）：
```bash
curl http://localhost:8080/actuator/health
```

### 2. 验证数据库表

```bash
mysql -u root -p rhythm_game

# 查看新创建的表
SHOW TABLES LIKE 'chart_%';

# 查看表结构
DESC chart_collaborator;
DESC chart_permission_log;
```

### 3. 测试权限 API

```bash
# 检查权限（需要先登录）
curl -X GET http://localhost:8080/chart/permission/check/1 \
  -H "Cookie: JSESSIONID=your_session_id"
```

## 🔍 故障排查

### 问题 1: 数据库连接失败

**症状：** 启动时报错 `Communications link failure`

**解决方案：**
1. 检查 MySQL 服务是否运行：`brew services list | grep mysql`
2. 验证 `.env` 中的数据库配置
3. 测试连接：`mysql -u root -p -h localhost`

### 问题 2: 端口被占用

**症状：** `Port 8080 is already in use`

**解决方案：**
```bash
# 查找占用端口的进程
lsof -i :8080

# 杀死进程
kill -9 <PID>
```

### 问题 3: Maven 编译失败

**症状：** 依赖下载失败或编译错误

**解决方案：**
```bash
# 清理 Maven 缓存
mvn clean

# 强制更新依赖
mvn clean install -U

# 如果还有问题，删除本地仓库缓存
rm -rf ~/.m2/repository
```

## 📝 日志查看

### 应用日志

默认情况下，日志会输出到控制台。如需查看历史日志：

```bash
# 如果使用 nohup 后台运行
tail -f nohup.out

# 如果配置了日志文件
tail -f logs/spring.log
```

### 数据库日志

```bash
# MySQL 错误日志
tail -f /usr/local/var/mysql/*.err

# 慢查询日志（如果启用）
tail -f /usr/local/var/mysql/*-slow.log
```

## 🔄 更新部署

当代码更新后：

```bash
# 停止当前运行的应用（Ctrl+C 或 kill）

# 重新运行启动脚本
./start.sh
```

## 🛡️ 生产环境建议

### 1. 使用 systemd 服务（Linux）

创建 `/etc/systemd/system/ephemelody.service`：

```ini
[Unit]
Description=EphemelodyOL Backend Service
After=mysql.service

[Service]
Type=simple
User=your_user
WorkingDirectory=/path/to/back/rhythm_game
EnvironmentFile=/path/to/.env
ExecStart=/usr/bin/java -jar target/rhythm_game.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl enable ephemelody
sudo systemctl start ephemelody
sudo systemctl status ephemelody
```

### 2. 使用 Docker（推荐）

创建 `Dockerfile`：

```dockerfile
FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

构建和运行：
```bash
docker build -t ephemelody-backend .
docker run -d -p 8080:8080 --env-file ../../.env ephemelody-backend
```

### 3. 配置反向代理（Nginx）

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 📞 支持

如遇到问题，请检查：
1. 📖 [完整文档](../docs/CHART_PERMISSION_SYSTEM.md)
2. 🐛 [Issue Tracker](https://github.com/your-repo/issues)
3. 💬 联系开发团队

---

**最后更新**: 2026-02-03  
**版本**: 1.0.0
