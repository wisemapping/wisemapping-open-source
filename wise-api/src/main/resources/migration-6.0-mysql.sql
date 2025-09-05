-- WiseMapping 6.0 Migration Script for MySQL
-- This script consolidates all database changes for version 6.0
-- Uses a try-continue approach to handle errors gracefully

-- Add spam detection fields to MINDMAP table
BEGIN;
  -- Add spam_detected column
  ALTER TABLE MINDMAP ADD COLUMN spam_detected BOOL NOT NULL DEFAULT 0;
COMMIT;

BEGIN;
  -- Add spam_description column
  ALTER TABLE MINDMAP ADD COLUMN spam_description TEXT CHARACTER SET UTF8MB4;
COMMIT;

-- Add account suspension fields to ACCOUNT table
BEGIN;
  -- Add suspended flag
  ALTER TABLE ACCOUNT ADD COLUMN suspended BOOL NOT NULL DEFAULT 0;
COMMIT;

BEGIN;
  -- Add suspended_date (when suspension was applied)
  ALTER TABLE ACCOUNT ADD COLUMN suspended_date DATETIME;
COMMIT;

BEGIN;
  -- Add suspension_reason (A=Abuse, T=Terms, S=Security, M=Manual, O=Other)
  ALTER TABLE ACCOUNT ADD COLUMN suspension_reason CHAR(1);
COMMIT;

BEGIN;
  -- Add suspension_end_date (when suspension expires)
  ALTER TABLE ACCOUNT ADD COLUMN suspension_end_date DATETIME;
COMMIT;

-- Create indexes for better performance
BEGIN;
  CREATE INDEX idx_mindmap_spam_detected ON MINDMAP(spam_detected);
COMMIT;

BEGIN;
  CREATE INDEX idx_account_suspended ON ACCOUNT(suspended);
COMMIT;

BEGIN;
  CREATE INDEX idx_account_suspended_date ON ACCOUNT(suspended_date);
COMMIT;

-- Update existing data if needed
BEGIN;
  -- Set spam_detected to FALSE for all existing mindmaps if not already set
  UPDATE MINDMAP SET spam_detected = 0 WHERE spam_detected IS NULL;
COMMIT;

BEGIN;
  -- Set suspended to FALSE for all existing accounts if not already set
  UPDATE ACCOUNT SET suspended = 0 WHERE suspended IS NULL;
COMMIT;