-- 移除 ChangeBackgroundOperation 中的 background 字段
-- 因为可以直接通过 asset_id 关联获取 URL，避免冗余和 localhost 硬盘硬编码问题
-- 创建日期: 2026-02-04
-- 作者: Antigravity
ALTER TABLE `change_background_operation` DROP COLUMN `background`;