# 🚀 快速开始指南

## 📦 已创建的文件

```
back/rhythm_game/
├── db_migration.sql          # 数据库迁移脚本（只创建新表）
├── start.sh                  # 后端启动脚本（一键启动）
├── test_migration.sh         # 数据库迁移测试脚本
└── README_DEPLOYMENT.md      # 详细部署文档
```

## ⚡ 三步快速启动

### 1️⃣ 测试数据库迁移（可选但推荐）

```bash
cd back/rhythm_game
./test_migration.sh
```

这个脚本会：
- ✅ 测试数据库连接
- ✅ 检查现有表（不会删除任何数据）
- ✅ 显示将要创建的新表
- ✅ 执行迁移并验证结果

### 2️⃣ 启动后端

```bash
cd back/rhythm_game
./start.sh
```

启动脚本会自动：
- ✅ 加载 `.env` 环境变量
- ✅ 询问是否执行数据库迁移
- ✅ 编译项目（Maven）
- ✅ 启动应用

### 3️⃣ 验证部署

访问以下 API 测试：

```bash
# 检查应用是否运行
curl http://localhost:8080

# 测试权限检查 API（需要登录）
curl http://localhost:8080/chart/permission/check/1
```

## 📋 数据库迁移说明

### 新建的表

**1. `chart_collaborator` - 谱面协作者表**
```sql
字段说明：
- id: 主键
- song_id: 歌曲ID
- user_id: 协作者用户ID
- permission_type: 权限类型（1-编辑 2-只读）
- invited_by: 邀请人ID
- status: 状态（0-待接受 1-已接受 2-已拒绝）
```

**2. `chart_permission_log` - 权限操作日志表**
```sql
字段说明：
- id: 主键
- song_id: 歌曲ID
- operator_id: 操作者ID
- action_type: 操作类型（INVITE/REMOVE/ACCEPT/REJECT）
- target_user_id: 目标用户ID
```

### 安全保证

✅ 使用 `CREATE TABLE IF NOT EXISTS` - 不会覆盖已存在的表  
✅ 只创建新表 - 不修改、不删除现有数据  
✅ 独立的表结构 - 不影响现有业务逻辑  

## 🔧 手动执行迁移

如果你想手动控制迁移过程：

```bash
# 1. 加载环境变量
export $(cat ../../.env | grep -v '^#' | xargs)

# 2. 执行迁移
mysql -h localhost -u root -p rhythm_game < db_migration.sql

# 3. 验证
mysql -u root -p rhythm_game -e "SHOW TABLES LIKE 'chart_%';"
```

## 📊 权限控制 API

### 检查编辑权限
```http
GET /chart/permission/check/{songId}
```

### 邀请协作者
```http
POST /chart/permission/invite
Content-Type: application/x-www-form-urlencoded

songId=1&targetUsername=user123&permissionType=1
```

### 接受邀请
```http
POST /chart/permission/accept/{collaboratorId}
```

### 拒绝邀请
```http
POST /chart/permission/reject/{collaboratorId}
```

### 移除协作者
```http
DELETE /chart/permission/remove?songId=1&targetUserId=user123
```

### 获取协作者列表
```http
GET /chart/permission/collaborators/{songId}
```

### 获取待处理邀请
```http
GET /chart/permission/invitations/pending
```

## 🐛 常见问题

### Q: 启动脚本报错 "Permission denied"
```bash
chmod +x start.sh test_migration.sh
```

### Q: 数据库连接失败
检查 `.env` 文件配置：
```bash
cat ../../.env
```

确保 MySQL 服务运行：
```bash
brew services list | grep mysql
# 如果未运行
brew services start mysql
```

### Q: 端口 8080 被占用
```bash
# 查找占用进程
lsof -i :8080

# 杀死进程
kill -9 <PID>
```

### Q: Maven 编译失败
```bash
# 清理并重新编译
mvn clean install -U
```

## 📚 更多文档

- 📖 [详细部署文档](README_DEPLOYMENT.md)
- 📖 [权限系统设计文档](../../docs/CHART_PERMISSION_SYSTEM.md)

## 🎯 下一步

1. ✅ 执行数据库迁移
2. ✅ 启动后端服务
3. ✅ 在前端集成权限检查
4. ✅ 测试协作者邀请功能

---

**提示**: 所有脚本都已设置为可执行，可以直接运行！
