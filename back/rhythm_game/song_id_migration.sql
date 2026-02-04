-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create a temporary mapping for existing songs with matching collation
CREATE TABLE IF NOT EXISTS song_id_mapping (
    old_id VARCHAR(255) COLLATE utf8_general_ci,
    new_id VARCHAR(255) COLLATE utf8_general_ci
) CHARACTER SET utf8 COLLATE utf8_general_ci;

-- Clean up any previous partial run data
TRUNCATE TABLE song_id_mapping;

-- Populate mapping. id is already varchar(255) but might contain old integer strings
INSERT INTO song_id_mapping (old_id, new_id)
SELECT id, UUID() FROM song WHERE id NOT LIKE '%-%' OR LENGTH(id) < 30;

-- 2. Update song table
UPDATE song s JOIN song_id_mapping m ON s.id = m.old_id SET s.id = m.new_id;

-- 3. Update all other tables
-- best_record
ALTER TABLE best_record MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE best_record t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- change_background_operation
ALTER TABLE change_background_operation MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE change_background_operation t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- change_color_operation
ALTER TABLE change_color_operation MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE change_color_operation t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- change_width_operation
ALTER TABLE change_width_operation MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE change_width_operation t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- chart_collaborator
ALTER TABLE chart_collaborator MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE chart_collaborator t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- chart_permission_log
ALTER TABLE chart_permission_log MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE chart_permission_log t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- move_operation
ALTER TABLE move_operation MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE move_operation t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- note
ALTER TABLE note MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE note t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- recent_record
ALTER TABLE recent_record MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE recent_record t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- song_asset
ALTER TABLE song_asset MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE song_asset t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- track
ALTER TABLE track MODIFY COLUMN song_id VARCHAR(255) COLLATE utf8_general_ci;
UPDATE track t JOIN song_id_mapping m ON CAST(t.song_id AS CHAR) = m.old_id SET t.song_id = m.new_id;

-- 4. Cleanup
DROP TABLE song_id_mapping;
SET FOREIGN_KEY_CHECKS = 1;
