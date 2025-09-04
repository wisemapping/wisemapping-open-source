-- Migration script to add spam_description column to MINDMAP table
-- Version: 6.1 -> 6.2

-- MySQL
ALTER TABLE MINDMAP ADD COLUMN spam_description TEXT CHARACTER SET UTF8MB4;

-- PostgreSQL  
-- ALTER TABLE MINDMAP ADD COLUMN spam_description TEXT;

-- HSQLDB
-- ALTER TABLE MINDMAP ADD COLUMN spam_description LONGVARCHAR;