-- Migration script to add admin disable tracking column to ACCOUNT table
-- Version: 6.2 -> 6.3

-- MySQL
ALTER TABLE ACCOUNT ADD COLUMN disabled_for_abuse BOOL NOT NULL DEFAULT 0;

-- PostgreSQL  
-- ALTER TABLE ACCOUNT ADD COLUMN disabled_for_abuse BOOL NOT NULL DEFAULT FALSE;

-- HSQLDB
-- ALTER TABLE ACCOUNT ADD COLUMN disabled_for_abuse BOOLEAN NOT NULL DEFAULT FALSE;