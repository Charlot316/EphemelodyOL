USE rhythm_game;
-- Rename R, G, B to r, g, b in track table for consistency
ALTER TABLE track CHANGE COLUMN R r INT NOT NULL COMMENT 'R',
    CHANGE COLUMN G g INT NOT NULL COMMENT 'G',
    CHANGE COLUMN B b INT NOT NULL COMMENT 'B';
-- Verify changes
SHOW COLUMNS
FROM track;