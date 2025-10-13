-- Migration script for 6.5->6.6
-- 1. Change spam_type_code from VARCHAR(50) to CHAR(1)
-- 2. Fix allow_send_email column type from CHAR(1) to BOOL

-- First, update existing spam data to use single character codes
UPDATE MINDMAP_SPAM_INFO 
SET spam_type_code = CASE 
    WHEN spam_type_code = 'CI' THEN 'C'
    WHEN spam_type_code = 'FN' THEN 'F'
    WHEN spam_type_code = 'UB' THEN 'U'
    WHEN spam_type_code = 'KP' THEN 'K'
    WHEN spam_type_code = 'UN' THEN 'X'
    ELSE 'X'
END
WHERE spam_type_code IS NOT NULL;

-- Alter the spam_type_code column to CHAR(1)
ALTER TABLE MINDMAP_SPAM_INFO 
MODIFY COLUMN spam_type_code CHAR(1);

-- Fix allow_send_email column type from CHAR(1) to BOOL (TINYINT(1))
-- This conversion automatically handles '1'/'0' to 1/0
ALTER TABLE ACCOUNT 
MODIFY COLUMN allow_send_email BOOL NOT NULL DEFAULT 0;
