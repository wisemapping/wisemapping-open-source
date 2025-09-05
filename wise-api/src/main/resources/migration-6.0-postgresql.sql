-- WiseMapping 6.0 Migration Script for PostgreSQL
-- This script consolidates all database changes for version 6.0
-- Uses a try-continue approach to handle errors gracefully

-- Add spam detection fields to MINDMAP table
DO $$
BEGIN
  -- Add spam_detected column
  BEGIN
    ALTER TABLE MINDMAP ADD COLUMN spam_detected BOOL NOT NULL DEFAULT FALSE;
  EXCEPTION
    WHEN duplicate_column THEN
      RAISE NOTICE 'Column spam_detected already exists in MINDMAP table.';
  END;
END$$;

DO $$
BEGIN
  -- Add spam_description column
  BEGIN
    ALTER TABLE MINDMAP ADD COLUMN spam_description TEXT;
  EXCEPTION
    WHEN duplicate_column THEN
      RAISE NOTICE 'Column spam_description already exists in MINDMAP table.';
  END;
END$$;

-- Add account suspension fields to ACCOUNT table
DO $$
BEGIN
  -- Add suspended flag
  BEGIN
    ALTER TABLE ACCOUNT ADD COLUMN suspended BOOL NOT NULL DEFAULT FALSE;
  EXCEPTION
    WHEN duplicate_column THEN
      RAISE NOTICE 'Column suspended already exists in ACCOUNT table.';
  END;
END$$;

DO $$
BEGIN
  -- Add suspended_date (when suspension was applied)
  BEGIN
    ALTER TABLE ACCOUNT ADD COLUMN suspended_date TIMESTAMP;
  EXCEPTION
    WHEN duplicate_column THEN
      RAISE NOTICE 'Column suspended_date already exists in ACCOUNT table.';
  END;
END$$;

DO $$
BEGIN
  -- Add suspension_reason (A=Abuse, T=Terms, S=Security, M=Manual, O=Other)
  BEGIN
    ALTER TABLE ACCOUNT ADD COLUMN suspension_reason CHAR(1);
  EXCEPTION
    WHEN duplicate_column THEN
      RAISE NOTICE 'Column suspension_reason already exists in ACCOUNT table.';
  END;
END$$;

DO $$
BEGIN
  -- Add suspension_end_date (when suspension expires)
  BEGIN
    ALTER TABLE ACCOUNT ADD COLUMN suspension_end_date TIMESTAMP;
  EXCEPTION
    WHEN duplicate_column THEN
      RAISE NOTICE 'Column suspension_end_date already exists in ACCOUNT table.';
  END;
END$$;

-- Create indexes for better performance
DO $$
BEGIN
  -- Create index for spam detection
  BEGIN
    CREATE INDEX idx_mindmap_spam_detected ON MINDMAP(spam_detected);
  EXCEPTION
    WHEN duplicate_table THEN
      RAISE NOTICE 'Index idx_mindmap_spam_detected already exists.';
  END;
END$$;

DO $$
BEGIN
  -- Create index for account suspension
  BEGIN
    CREATE INDEX idx_account_suspended ON ACCOUNT(suspended);
  EXCEPTION
    WHEN duplicate_table THEN
      RAISE NOTICE 'Index idx_account_suspended already exists.';
  END;
END$$;

DO $$
BEGIN
  -- Create index for suspension date
  BEGIN
    CREATE INDEX idx_account_suspended_date ON ACCOUNT(suspended_date);
  EXCEPTION
    WHEN duplicate_table THEN
      RAISE NOTICE 'Index idx_account_suspended_date already exists.';
  END;
END$$;

-- Update existing data if needed
DO $$
BEGIN
  -- Set spam_detected to FALSE for all existing mindmaps if not already set
  BEGIN
    UPDATE MINDMAP SET spam_detected = FALSE WHERE spam_detected IS NULL;
    RAISE NOTICE 'Updated existing mindmaps with spam_detected = FALSE';
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'Could not update existing mindmap spam_detected values: %', SQLERRM;
  END;
END$$;

DO $$
BEGIN
  -- Set suspended to FALSE for all existing accounts if not already set
  BEGIN
    UPDATE ACCOUNT SET suspended = FALSE WHERE suspended IS NULL;
    RAISE NOTICE 'Updated existing accounts with suspended = FALSE';
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'Could not update existing account suspended values: %', SQLERRM;
  END;
END$$;