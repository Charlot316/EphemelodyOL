-- ============================================
-- 谱面权限控制系统数据库迁移脚本
-- 数据库: rhythm_game
-- 创建日期: 2026-02-03
-- 作者: Antigravity
-- 
-- 注意: 此脚本只创建新表，不会影响现有数据
-- ============================================
USE rhythm_game;
-- 检查并创建谱面协作者表
CREATE TABLE IF NOT EXISTS `chart_collaborator` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `song_id` INT NOT NULL COMMENT '歌曲ID',
    `user_id` VARCHAR(255) NOT NULL COMMENT '协作者用户ID',
    `permission_type` TINYINT NOT NULL DEFAULT 1 COMMENT '权限类型：1-编辑权限 2-只读权限',
    `invited_by` VARCHAR(255) COMMENT '邀请人ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-待接受 1-已接受 2-已拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_song_user` (`song_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_song_id` (`song_id`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '谱面协作者表';
-- 检查并创建权限操作日志表（可选，用于审计）
CREATE TABLE IF NOT EXISTS `chart_permission_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `song_id` INT NOT NULL COMMENT '歌曲ID',
    `operator_id` VARCHAR(255) NOT NULL COMMENT '操作者ID',
    `action_type` VARCHAR(50) NOT NULL COMMENT '操作类型：INVITE/REMOVE/ACCEPT/REJECT',
    `target_user_id` VARCHAR(255) COMMENT '目标用户ID',
    `details` TEXT COMMENT '操作详情（JSON格式）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_song_id` (`song_id`),
    INDEX `idx_operator_id` (`operator_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_action_type` (`action_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限操作日志表';
-- 验证表是否创建成功
SELECT TABLE_NAME,
    TABLE_ROWS,
    CREATE_TIME,
    TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'rhythm_game'
    AND TABLE_NAME IN ('chart_collaborator', 'chart_permission_log');
-- 显示表结构
SHOW CREATE TABLE chart_collaborator;
SHOW CREATE TABLE chart_permission_log;