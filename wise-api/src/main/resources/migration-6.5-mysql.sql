-- WiseMapping 6.5 Migration Script for MySQL
-- This script moves spam columns from MINDMAP to MINDMAP_SPAM_INFO table

-- Create the new MINDMAP_SPAM_INFO table
BEGIN;
  CREATE TABLE IF NOT EXISTS MINDMAP_SPAM_INFO (
    mindmap_id            INTEGER            NOT NULL PRIMARY KEY,
    spam_detected         BOOL               NOT NULL DEFAULT 0,
    spam_description      TEXT               CHARACTER SET UTF8MB4,
    spam_detection_version INTEGER           NOT NULL DEFAULT 0,
    spam_type_code        VARCHAR(50),
    created_at            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
  )
    CHARACTER SET UTF8MB4;
COMMIT;

-- Move data from MINDMAP to MINDMAP_SPAM_INFO
BEGIN;
  INSERT INTO MINDMAP_SPAM_INFO (mindmap_id, spam_detected, spam_description, spam_detection_version, created_at, updated_at)
  SELECT id, spam_detected, spam_description, spam_detection_version, NOW(), NOW()
  FROM MINDMAP
  WHERE spam_detected = 1 OR spam_description IS NOT NULL OR spam_detection_version > 0;
COMMIT;

-- Create indexes for better performance
BEGIN;
  CREATE INDEX idx_mindmap_spam_info_detected ON MINDMAP_SPAM_INFO(spam_detected);
  CREATE INDEX idx_mindmap_spam_info_version ON MINDMAP_SPAM_INFO(spam_detection_version);
COMMIT;

-- Remove spam columns from MINDMAP table
BEGIN;
  ALTER TABLE MINDMAP DROP COLUMN spam_detected;
  ALTER TABLE MINDMAP DROP COLUMN spam_description;
  ALTER TABLE MINDMAP DROP COLUMN spam_detection_version;
COMMIT;

-- Drop old indexes
BEGIN;
  DROP INDEX IF EXISTS idx_mindmap_spam_detected;
  DROP INDEX IF EXISTS idx_mindmap_spam_detection_version;
COMMIT;
