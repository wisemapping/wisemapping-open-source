-- WiseMapping 6.5 Migration Script for PostgreSQL
-- This script moves spam columns from MINDMAP to MINDMAP_SPAM_INFO table

-- Create the new MINDMAP_SPAM_INFO table
BEGIN;
  CREATE TABLE IF NOT EXISTS MINDMAP_SPAM_INFO (
    mindmap_id            INTEGER      NOT NULL PRIMARY KEY,
    spam_detected         BOOL         NOT NULL DEFAULT FALSE,
    spam_description      TEXT,
    spam_detection_version INTEGER     NOT NULL DEFAULT 0,
    spam_type_code        CHAR(1),
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
  );
COMMIT;

-- Move data from MINDMAP to MINDMAP_SPAM_INFO
BEGIN;
  INSERT INTO MINDMAP_SPAM_INFO (mindmap_id, spam_detected, spam_description, spam_detection_version, created_at, updated_at)
  SELECT id, spam_detected, spam_description, spam_detection_version, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  FROM MINDMAP
  WHERE spam_detected = TRUE OR spam_description IS NOT NULL OR spam_detection_version > 0;
COMMIT;

-- Create indexes for better performance
BEGIN;
  CREATE INDEX idx_mindmap_spam_info_detected ON MINDMAP_SPAM_INFO(spam_detected);
  CREATE INDEX idx_mindmap_spam_info_version ON MINDMAP_SPAM_INFO(spam_detection_version);
COMMIT;

-- Remove spam columns from MINDMAP table
BEGIN;
  ALTER TABLE MINDMAP DROP COLUMN IF EXISTS spam_detected;
  ALTER TABLE MINDMAP DROP COLUMN IF EXISTS spam_description;
  ALTER TABLE MINDMAP DROP COLUMN IF EXISTS spam_detection_version;
COMMIT;

-- Drop old indexes
BEGIN;
  DROP INDEX IF EXISTS idx_mindmap_spam_detected;
  DROP INDEX IF EXISTS idx_mindmap_spam_detection_version;
COMMIT;
