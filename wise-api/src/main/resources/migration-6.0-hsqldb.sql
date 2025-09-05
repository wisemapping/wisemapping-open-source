-- WiseMapping 6.0 Migration Script for HSQLDB
-- This script consolidates all database changes for version 6.0
-- Uses a try-continue approach to handle errors gracefully

-- Note: HSQLDB doesn't support IF NOT EXISTS for ALTER TABLE ADD COLUMN
-- We use a more basic approach with error handling comments

-- Add spam detection fields to MINDMAP table
-- Add spam_detected column (ignore error if exists)
ALTER TABLE MINDMAP ADD COLUMN spam_detected BOOLEAN NOT NULL DEFAULT FALSE;
-- Expected error: Column already exists

-- Add spam_description column (ignore error if exists)  
ALTER TABLE MINDMAP ADD COLUMN spam_description LONGVARCHAR;
-- Expected error: Column already exists

-- Add account suspension fields to ACCOUNT table
-- Add suspended flag (ignore error if exists)
ALTER TABLE ACCOUNT ADD COLUMN suspended BOOLEAN NOT NULL DEFAULT FALSE;
-- Expected error: Column already exists

-- Add suspended_date (when suspension was applied) (ignore error if exists)
ALTER TABLE ACCOUNT ADD COLUMN suspended_date DATETIME;
-- Expected error: Column already exists

-- Add suspension_reason (A=Abuse, T=Terms, S=Security, M=Manual, O=Other) (ignore error if exists)
ALTER TABLE ACCOUNT ADD COLUMN suspension_reason CHAR(1);
-- Expected error: Column already exists

-- Add suspension_end_date (when suspension expires) (ignore error if exists)
ALTER TABLE ACCOUNT ADD COLUMN suspension_end_date DATETIME;
-- Expected error: Column already exists

-- Create indexes for better performance (ignore error if exists)
CREATE INDEX idx_mindmap_spam_detected ON MINDMAP(spam_detected);
-- Expected error: Index already exists

CREATE INDEX idx_account_suspended ON ACCOUNT(suspended);
-- Expected error: Index already exists

CREATE INDEX idx_account_suspended_date ON ACCOUNT(suspended_date);
-- Expected error: Index already exists

-- Update existing data if needed
-- Set spam_detected to FALSE for all existing mindmaps if not already set
UPDATE MINDMAP SET spam_detected = FALSE WHERE spam_detected IS NULL;

-- Set suspended to FALSE for all existing accounts if not already set
UPDATE ACCOUNT SET suspended = FALSE WHERE suspended IS NULL;