-- Migration script for 6.5->6.6
-- 1. Change spam_type_code from VARCHAR(50) to CHAR(1)
-- 2. Fix allow_send_email column type from TEXT to BOOLEAN

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
ALTER COLUMN spam_type_code TYPE CHAR(1);

-- Fix allow_send_email column type from TEXT to BOOLEAN
-- Convert existing TEXT values to BOOLEAN
ALTER TABLE ACCOUNT 
ALTER COLUMN allow_send_email TYPE BOOLEAN 
USING CASE 
    WHEN allow_send_email IN ('1', 't', 'true', 'TRUE', 'y', 'yes', 'YES') THEN TRUE 
    ELSE FALSE 
END;

-- Set proper default for allow_send_email
ALTER TABLE ACCOUNT 
ALTER COLUMN allow_send_email SET DEFAULT FALSE;
