USE rhythm_game;
-- Rename start_time/end_time to start_timing/end_timing in operation tables
ALTER TABLE change_background_operation CHANGE COLUMN start_time start_timing INT NOT NULL COMMENT '操作开始时间',
    CHANGE COLUMN end_time end_timing INT DEFAULT NULL COMMENT '操作结束时间';
ALTER TABLE change_color_operation CHANGE COLUMN start_time start_timing INT NOT NULL COMMENT '操作开始时间',
    CHANGE COLUMN end_time end_timing INT NOT NULL COMMENT '操作结束时间';
ALTER TABLE change_width_operation CHANGE COLUMN start_time start_timing INT NOT NULL COMMENT '操作开始时间',
    CHANGE COLUMN end_time end_timing INT NOT NULL COMMENT '操作结束时间';
ALTER TABLE move_operation CHANGE COLUMN start_time start_timing INT NOT NULL COMMENT '操作开始时间',
    CHANGE COLUMN end_time end_timing INT NOT NULL COMMENT '操作结束时间';
-- Verify changes
SHOW COLUMNS
FROM move_operation;