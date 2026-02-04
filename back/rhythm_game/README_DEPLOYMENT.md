# 🚀 EphemelodyOL 后端部署指南

## 📋 前置要求

- ✅ Java 17+ (推荐 Java 17 LTS 或 Java 18)
- ✅ Maven 3.6+
- ✅ MySQL 5.7+ 或 8.0+
- ✅ Cloudflare R2 对象存储 (或其他 S3 兼容存储)
- ✅ 系统依赖: `ffmpeg` 和 `webp` (cwebp) 工具
- ✅ 已配置好的 `.env` 文件（项目根目录）

## 🔧 环境配置

### 1. 检查 `.env` 文件

确保项目根目录下的 `.env` 文件包含以下配置（**请勿将包含真实密钥的文件提交到 Git**）：

```bash
# MySQL Database Configuration
DB_URL="jdbc:mysql://localhost:3306/rhythm_game?useSSL=false&useUnicode=true&characterEncoding=utf-8&severTimezone=GMT%2B8&allowPublicKeyRetrieval=true"
DB_USERNAME="your_db_username"
DB_PASSWORD="your_db_password"

# Cloudflare R2 Configuration (S3 Compatible)
R2_ENDPOINT="https://<ACCOUNT_ID>.r2.cloudflarestorage.com"
R2_ACCESS_KEY_ID="your_access_key_id"
R2_SECRET_ACCESS_KEY="your_secret_access_key"
R2_BUCKET_NAME="your_bucket_name"
R2_PUBLIC_URL="https://your-public-url.com"
```

### 2. 数据库准备

确保 MySQL 服务正在运行，并且 `rhythm_game` 数据库已创建。
> 注意：项目已迁移至 UUID 主键，旧的整数 ID 数据将不再兼容。请确保使用最新的数据库结构。

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS rhythm_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出
exit;
```

## 🎯 快速启动

### 方式一：本地 Maven 启动

1.  **安装系统依赖**
    *   macOS: `brew install ffmpeg webp`
    *   Ubuntu/Debian: `sudo apt-get install ffmpeg webp`

2.  **启动应用**
    在 `back/rhythm_game` 目录下：

```bash
# 加载环境变量并启动
export $(cat ../../.env | grep -v '^#' | xargs) && mvn spring-boot:run
```

### 方式二：Docker 部署（推荐）

项目根目录已包含 `Dockerfile`，支持一键构建运行。

1.  **构建镜像**

    在项目根目录 (`EphemelodyOL/`) 执行：

    ```bash
    docker build -t ephemelody-backend .
    ```

    > 注意：Dockerfile 已包含 ffmpeg 和 webp 依赖的安装。

2.  **运行容器**

    ```bash
    docker run -d \
      --name ephemelody-backend \
      -p 8090:8090 \
      --env-file .env \
      ephemelody-backend
    ```

3.  **查看日志**

    ```bash
    docker logs -f ephemelody-backend
    ```

## 🔄 资源迁移说明

本项目已全面迁移至 **Cloudflare R2** 云存储，并不再依赖本地 `data` 目录。
*   所有静态资源（封面、背景、音频、谱面 JSON）均存储在 R2 Bucket 中。
*   上传/删除谱面会自动同步 R2 上的文件。
*   数据库中的 `song_id` 已变更为 **UUID** 字符串格式。

如果是从旧版本升级，请确保执行了 R2 迁移脚本（通过 API `/api/admin/migration/r2`）。

## 🛡️ 生产环境建议

### Nginx 反向代理配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # API 接口转发
    location /api/ {
        proxy_pass http://localhost:8090/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket 转发 (多人游戏必须)
    location /api/ws/ {
        proxy_pass http://localhost:8090/api/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 3600s; # 延长超时时间以保持连接
    }
}
```

## 📞 支持

如遇到问题，请检查后端日志输出或联系开发负责人。
