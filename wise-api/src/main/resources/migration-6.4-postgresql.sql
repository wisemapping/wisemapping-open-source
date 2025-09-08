-- WiseMapping 6.4 Migration Script for PostgreSQL
-- This script adds spam_detection_version column to MINDMAP table

-- Add spam_detection_version column to MINDMAP table
BEGIN;
  ALTER TABLE MINDMAP ADD COLUMN spam_detection_version INTEGER NOT NULL DEFAULT 0;
COMMIT;

-- Create index for better performance
BEGIN;
  CREATE INDEX idx_mindmap_spam_detection_version ON MINDMAP(spam_detection_version);
COMMIT;

-- Update existing data
BEGIN;
  -- Set spam_detection_version to 0 for all existing mindmaps
  UPDATE MINDMAP SET spam_detection_version = 0 WHERE spam_detection_version IS NULL;
COMMIT;
