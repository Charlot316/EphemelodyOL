# 谱面权限控制系统实现文档

## 📋 概述

本文档描述了为 EphemelodyOL 音游项目实现的谱面编辑权限控制系统。该系统允许只有特定用户（超级管理员、谱面创作者、受邀协作者）才能编辑谱面。

## 🎯 需求分析

### 权限控制需求
用户可以编辑谱面的条件（满足任一即可）：
1. **超级管理员**：`User.isAdmin == 1`
2. **谱面创作者**：`Song.uploaderId == currentUserId`
3. **受邀协作者**：在 `chart_collaborator` 表中存在记录且 `status == 1`

## 🏗️ 架构设计

### 数据库设计

#### 1. `chart_collaborator` 表（谱面协作者表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| song_id | INT | 歌曲ID |
| user_id | VARCHAR(255) | 协作者用户ID |
| permission_type | TINYINT | 权限类型：1-编辑 2-只读 |
| invited_by | VARCHAR(255) | 邀请人ID |
| status | TINYINT | 状态：0-待接受 1-已接受 2-已拒绝 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

**索引：**
- `uk_song_user`: UNIQUE(song_id, user_id)
- `idx_user_id`: INDEX(user_id)
- `idx_song_id`: INDEX(song_id)

#### 2. `chart_permission_log` 表（权限操作日志表，可选）
用于记录权限相关的操作历史，便于审计。

### 后端实现

#### 核心类结构

```
team.javaee
├── entity/domain
│   └── ChartCollaborator.java          # 协作者实体类
├── mapper
│   └── ChartCollaboratorMapper.java    # 数据访问层
├── service
│   ├── ChartPermissionService.java     # 权限服务接口
│   └── impl
│       └── ChartPermissionServiceImpl.java  # 权限服务实现
└── controller
    └── ChartPermissionController.java  # 权限控制器
```

#### API 接口

**1. 检查编辑权限**
```
GET /chart/permission/check/{songId}
返回: { hasPermission, isCreator, isAdmin }
```

**2. 邀请协作者**
```
POST /chart/permission/invite
参数: songId, targetUsername, permissionType
```

**3. 接受邀请**
```
POST /chart/permission/accept/{collaboratorId}
```

**4. 拒绝邀请**
```
POST /chart/permission/reject/{collaboratorId}
```

**5. 移除协作者**
```
DELETE /chart/permission/remove
参数: songId, targetUserId
```

**6. 获取协作者列表**
```
GET /chart/permission/collaborators/{songId}
```

**7. 获取待处理邀请**
```
GET /chart/permission/invitations/pending
```

## 🔧 技术选型建议

### 后端架构对比

| 架构方案 | 优点 | 缺点 | 适用场景 |
|---------|------|------|---------|
| **WebSocket (当前)** | ✅ 双向实时通信<br>✅ 低延迟<br>✅ 适合协作编辑 | ⚠️ 连接管理复杂<br>⚠️ 扩展性受限 | 小规模协作（<50人） |
| **Netty** | ✅ 高性能<br>✅ 异步非阻塞<br>✅ 灵活的协议支持 | ⚠️ 学习曲线陡峭<br>⚠️ 开发复杂度高 | 高并发场景（>1000连接） |
| **Redis Pub/Sub + WebSocket** | ✅ 易于扩展<br>✅ 支持分布式<br>✅ 消息持久化 | ⚠️ 需要额外组件<br>⚠️ 运维成本增加 | 中大规模应用 |

### 推荐方案：WebSocket + Redis Pub/Sub

**理由：**
1. **渐进式升级**：基于现有 WebSocket 架构，无需重构
2. **水平扩展**：支持多实例部署，突破单机限制
3. **成本可控**：Redis 轻量且易于维护
4. **性能优秀**：满足音游谱面编辑的实时性要求

**架构图：**
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client A   │────▶│  Server 1   │────▶│   Redis     │
└─────────────┘     │  WebSocket  │     │  Pub/Sub    │
                    └─────────────┘     └─────────────┘
                           ▲                    │
                           │                    │
                           │                    ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client B   │────▶│  Server 2   │◀────│  Subscribe  │
└─────────────┘     │  WebSocket  │     │  Channel    │
                    └─────────────┘     └─────────────┘
```

## 📝 部署步骤

### 1. 数据库迁移
```bash
# 执行数据库迁移脚本
mysql -u username -p database_name < src/main/resources/db/migration/V2__chart_permission_system.sql
```

### 2. 后端部署
```bash
# 编译项目
mvn clean package

# 运行应用
java -jar target/rhythm_game.jar
```

### 3. 前端集成
在谱面编辑器页面加载时调用权限检查 API：
```javascript
async function checkEditPermission(songId) {
    const response = await fetch(`/chart/permission/check/${songId}`);
    const data = await response.json();
    
    if (!data.hasPermission) {
        // 跳转到无权限页面或显示提示
        router.push('/no-permission');
    }
}
```

## 🔒 安全考虑

1. **Session 验证**：所有 API 都需要验证用户登录状态
2. **权限二次校验**：在 WebSocket 操作中也需要验证权限
3. **SQL 注入防护**：使用 MyBatis 参数化查询
4. **XSS 防护**：前端输入需要进行转义

## 🚀 未来优化方向

1. **实时协作冲突解决**：实现 CRDT 或 OT 算法
2. **权限细粒度控制**：支持不同操作的不同权限级别
3. **协作历史记录**：记录每个协作者的编辑历史
4. **通知系统**：邀请和权限变更时发送通知

## 📚 参考资料

- [WebSocket 协议规范](https://tools.ietf.org/html/rfc6455)
- [Redis Pub/Sub 文档](https://redis.io/topics/pubsub)
- [CRDT 算法介绍](https://crdt.tech/)
- [OT 算法介绍](https://en.wikipedia.org/wiki/Operational_transformation)

---

**创建时间**: 2026-02-03  
**作者**: Antigravity  
**版本**: 1.0
