-- Migration script to change spam_type_code from VARCHAR(50) to CHAR(1)
-- This script converts existing string codes to single character codes

-- First, update existing data to use single character codes
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

-- Alter the column to CHAR(1)
ALTER TABLE MINDMAP_SPAM_INFO 
ALTER COLUMN spam_type_code TYPE CHAR(1);
