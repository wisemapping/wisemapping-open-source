-- Migration script to add spam_detected column to MINDMAP table
-- Version: 6.0 -> 6.1

-- MySQL
ALTER TABLE MINDMAP ADD COLUMN spam_detected BOOL NOT NULL DEFAULT 0;

-- PostgreSQL  
-- ALTER TABLE MINDMAP ADD COLUMN spam_detected BOOL NOT NULL DEFAULT FALSE;
