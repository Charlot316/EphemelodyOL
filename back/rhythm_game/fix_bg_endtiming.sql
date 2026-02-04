-- 修复背景操作的 end_timing
-- 对于每个 song_id，按 start_timing 排序，将下一个操作的 start_timing 作为当前操作的 end_timing

-- 创建临时表存储修复后的数据
CREATE TEMPORARY TABLE temp_bg_fix AS
SELECT 
    t1.id,
    t1.song_id,
    t1.start_timing,
    COALESCE(
        (SELECT MIN(t2.start_timing) 
         FROM change_background_operation t2 
         WHERE t2.song_id = t1.song_id 
         AND t2.start_timing > t1.start_timing),
        -- 如果没有下一个操作，使用一个默认值（比如当前 + 5000ms）
        t1.start_timing + 5000
    ) AS new_end_timing
FROM change_background_operation t1
WHERE t1.end_timing IS NULL;

-- 更新原表
UPDATE change_background_operation cbo
INNER JOIN temp_bg_fix tbf ON cbo.id = tbf.id
SET cbo.end_timing = tbf.new_end_timing;

-- 显示修复结果
SELECT COUNT(*) as fixed_count FROM temp_bg_fix;

-- 清理临时表
DROP TEMPORARY TABLE temp_bg_fix;
